package com.artisan.collects;


import com.artisan.ApmContext;
import com.artisan.common.json.JsonWriter;
import com.artisan.intf.ICollect;
import com.artisan.model.JdbcStatistics;
import com.artisan.utils.SplitUtils;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * jdbc 数据采集
 */
public class JdbcCommonCollects extends AbstractByteTransformCollect implements ICollect {

	public static  JdbcCommonCollects INSTANCE;
	private ApmContext context;

	public JdbcCommonCollects(ApmContext context, Instrumentation instrumentation) {
		super(instrumentation);
		INSTANCE=this;
		this.context=context;
	}


    private final static String[] connection_agent_methods = new String[]{"prepareStatement"};
    private final static String[] prepared_statement_methods = new String[]{"execute", "executeUpdate", "executeQuery"};
    private static final String beginSrc;
    private static final String endSrc;
    private static final String errorSrc;

    static {
        // connect
        beginSrc = "com.artisan.collects.JdbcCommonCollects inst=com.artisan.collects.JdbcCommonCollects.INSTANCE;";
        errorSrc = "inst.error(null,e);";
        endSrc = "result=inst.proxyConnection((java.sql.Connection)result);";
    }

    
    
    public JdbcStatistics begin(String className, String method) {
        JdbcStatistics jdbcStat = new JdbcStatistics();
        jdbcStat.begin=System.currentTimeMillis();
        jdbcStat.setModelType("jdbc");
        return jdbcStat;
    }

    
    
    public void end(JdbcStatistics stat) {
        JdbcStatistics jdbcStat= (JdbcStatistics) stat;
        jdbcStat.end=System.currentTimeMillis();
        jdbcStat.useTime =jdbcStat.end-jdbcStat.begin;
        if (jdbcStat.jdbcUrl != null) {
            jdbcStat.databaseName = getDbName(jdbcStat.jdbcUrl);
        }
        this.context.submitCollectResult(stat);
    }

    public void error(JdbcStatistics stat, Throwable throwable) {
        if (stat != null) {
            stat.error = throwable.getMessage();
            stat.errorType = throwable.getClass().getName();
            if (throwable instanceof InvocationTargetException) {
                stat.errorType = ((InvocationTargetException) throwable).getTargetException().getClass().getName();
                stat.error = ((InvocationTargetException) throwable).getTargetException().getMessage();
            }
        }
    }
    
    public void sendStatistics(JdbcStatistics stat) {
       
    }

    
    public Connection proxyConnection(final Connection connection) {
        Object c = Proxy.newProxyInstance(JdbcCommonCollects.class.getClassLoader()
                , new Class[]{Connection.class}, new ConnectionHandler(connection));
        return (Connection) c;
    }


    public PreparedStatement proxyPreparedStatement(final PreparedStatement statement, JdbcStatistics jdbcStat) {
        Object c = Proxy.newProxyInstance(JdbcCommonCollects.class.getClassLoader()
                , new Class[]{PreparedStatement.class}, new PreparedStatementHandler(statement, jdbcStat));
        return (PreparedStatement) c;
    }

    public byte[] transform(ClassLoader loader, String className) throws Exception {
        // System.out.println("JDBC className:" + className);
        // 如果更換數據庫，需要替換改類  這裡目前僅適配了MySQL
    	 if (!className.equals("com.mysql.cj.jdbc.NonRegisteringDriver")) {
             return null;
         }
    	CtClass ctclass = super.toCtClass(loader, className);
    	AgentByteBuild byteLoade = new AgentByteBuild(className, loader, ctclass);
        CtMethod connectMethod = ctclass.getMethod("connect", "(Ljava/lang/String;Ljava/util/Properties;)Ljava/sql/Connection;");
//      connectMethod.getMethodInfo().getDescriptor();
        AgentByteBuild.MethodSrcBuild build = new AgentByteBuild.MethodSrcBuild();
        build.setBeginSrc(beginSrc);
        build.setErrorSrc(errorSrc);
        build.setEndSrc(endSrc);
        byteLoade.updateMethod(connectMethod, build);
        return byteLoade.toBytecote();
    }


    /**
     * connection 代理处理
     */
    public class ConnectionHandler implements InvocationHandler {
        private final Connection connection;

        private ConnectionHandler(Connection connection) {
            this.connection = connection;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            boolean isTargetMethod = false;
            for (String agentm : connection_agent_methods) {
                if (agentm.equals(method.getName())) {
                    isTargetMethod = true;
                }
            }
            Object result = null;
            JdbcStatistics jdbcStat = null;
            try {
                if (isTargetMethod) { // 获取PreparedStatement 开始统计
                    jdbcStat = (JdbcStatistics) JdbcCommonCollects.this.begin(null, null);
                    jdbcStat.jdbcUrl = connection.getMetaData().getURL();
                    jdbcStat.sql = (String) args[0];
                }
                result = method.invoke(connection, args);
                // 代理 PreparedStatement
                if (isTargetMethod && result instanceof PreparedStatement) {
                    PreparedStatement ps = (PreparedStatement) result;
                    result = proxyPreparedStatement(ps, jdbcStat);
                }
            } catch (Throwable e) {
                JdbcCommonCollects.this.error(jdbcStat, e);
                JdbcCommonCollects.this.end(jdbcStat);
                throw e;
            }
            return result;
        }
    }

    /**
     * PreparedStatement 代理处理
     */
    public class PreparedStatementHandler implements InvocationHandler {
        private final PreparedStatement statement;
        private final JdbcStatistics jdbcStat;

        public PreparedStatementHandler(PreparedStatement statement, JdbcStatistics jdbcStat) {
            this.statement = statement;
            this.jdbcStat = jdbcStat;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
            boolean isTargetMethod = false;
            for (String agentm : prepared_statement_methods) {
                if (agentm.equals(method.getName())) {
                    isTargetMethod = true;
                    break;
                }
            }
            Object result = null;
            try {
                // SQL的參數 輸出  TODO  只能输出一个，后面的会把前面的参数覆盖掉
                if (args != null ){
                    List<JdbcStatistics.ParamValues> paramValues = SplitUtils.split2Parts(args, 2);
                    jdbcStat.setParams(paramValues);
                    //System.out.println("KKKKKKK:" + JsonWriter.objectToJson(jdbcStat));
                }

                result = method.invoke(statement, args);
            } catch (Throwable e) {
                if (isTargetMethod) {
                    JdbcCommonCollects.this.error(jdbcStat, e);
                }
                throw e;
            } finally {
                if (isTargetMethod) {
                    JdbcCommonCollects.this.end(jdbcStat);
                }
            }
            return result;
        }
    }

    
   

    private static String getDbName(String url) {
        int index = url.indexOf("?"); //$NON-NLS-1$
        if (index != -1) {
            String paramString = url.substring(index + 1, url.length());
            url = url.substring(0, index);
        }
        String dbName = url.substring(url.lastIndexOf("/") + 1);
        return dbName;
    }

	
}
