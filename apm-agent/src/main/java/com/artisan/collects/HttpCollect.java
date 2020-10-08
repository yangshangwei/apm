package com.artisan.collects;

import com.artisan.ApmContext;
import com.artisan.intf.ICollect;
import com.artisan.model.HttpStatistics;
import javassist.*;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Http采集器
 */
public class HttpCollect extends AbstractByteTransformCollect implements ICollect {
    // 采集目标
    // 1.DispatchServlet
    // 2.@control 下的方法
    // 3.采集 javax.servlet.service()  ---- 采用这种通用的方式
    private static final String TARGET_CLASS = "javax.servlet.http.HttpServlet";
    private static final String TARGET_METHOD = "service";
    private static ApmContext context;

    public HttpCollect(ApmContext context, Instrumentation instrumentation) {
        super(instrumentation);
        this.context = context;
    }


    public byte[] buildClass(ClassLoader loader) throws Exception {
        ClassPool pool = new ClassPool();
        pool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = pool.get(TARGET_CLASS);
        CtMethod oldMethod = ctClass.getDeclaredMethod(TARGET_METHOD);
        CtMethod newMethod = CtNewMethod.copy(oldMethod, ctClass, null);
        oldMethod.setName(oldMethod.getName() + "$agent");
        //HttpServlet.service()'

        String beginSrc = "Object stat=com.artisan.collects.HttpCollect.begin($args);";
        String errorSrc = "com.artisan.collects.HttpCollect.error(e,stat);";
        String endSrc = "com.artisan.collects.HttpCollect.end(stat);";
        newMethod.setBody(String.format(voidSource, beginSrc, TARGET_METHOD, errorSrc, endSrc));
        ctClass.addMethod(newMethod);
        return ctClass.toBytecode();
    }

    // url,client IP
    public static HttpStatistics begin(Object args[]) {
        HttpStatistics httpStatistics = new HttpStatistics();
        httpStatistics.setBeginTime(System.currentTimeMillis());
        // 采用适配器的方式 ，避免在tomcat下  agent【appLauncher装载】无法获取到HttpServletRequest【common加载】（classLoader机制）
        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(args[0]);
        httpStatistics.setUrl(adapter.getRequestURI());
        httpStatistics.setClientIp(adapter.getClientIp());
        return httpStatistics;
    }

    public static void end(Object obj) {
        HttpStatistics stat = (HttpStatistics) obj;
        ((HttpStatistics) obj).setUseTime(System.currentTimeMillis() - stat.getBeginTime());
        context.submitCollectResult(stat);
    }

    public static void error(Throwable error, Object obj) {
        HttpStatistics stat = (HttpStatistics) obj;
        stat.setError(error.getMessage());
        System.out.println(stat);
    }

    final static String voidSource = "{\n"
            + "%s"
            + "        try {\n"
            + "            %s$agent($$);\n"
            + "        } catch (Throwable e) {\n"
            + "%s"
            + "            throw e;\n"
            + "        }finally{\n"
            + "%s"
            + "        }\n"
            + "}\n";

    @Override
    public byte[] transform(ClassLoader loader, String className) throws Exception {
        if (!TARGET_CLASS.equals(className)) {
            return null;
        }
        try {
            return buildClass(loader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class HttpServletRequestAdapter {
        private final Object target;
        private final Method _getRequestURI;
        private final Method _getRequestURL;
        private final Method _getParameterMap;
        private final Method _getMethod;
        private final Method _getHeader;
        private final Method _getRemoteAddr;
        private final static String targetClassName = "javax.servlet.http.HttpServletRequest";

        public HttpServletRequestAdapter(Object target) {
            this.target = target;
            try {
                Class<?> targetClass = target.getClass().getClassLoader().loadClass(targetClassName);
                _getRequestURI = targetClass.getMethod("getRequestURI");
                _getParameterMap = targetClass.getMethod("getParameterMap");
                _getMethod = targetClass.getMethod("getMethod");
                _getHeader = targetClass.getMethod("getHeader", String.class);
                _getRemoteAddr = targetClass.getMethod("getRemoteAddr");
                _getRequestURL = targetClass.getMethod("getRequestURL");
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("error :" + e.getMessage() + ". probable cause the target is not belong javax.servlet.http.HttpServletRequest ");
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("error :" + e.getMessage() + ". probable cause the target is not belong javax.servlet.http.HttpServletRequest ");
            }
        }


        public String getRequestURI() {
            try {
                return (String) _getRequestURI.invoke(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getRequestURL() {
            try {
                return _getRequestURL.invoke(target).toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Map<String, String[]> getParameterMap() {
            try {
                return (Map<String, String[]>) _getParameterMap.invoke(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getMethod() {
            try {
                return (String) _getMethod.invoke(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getHeader(String name) {
            try {
                return (String) _getHeader.invoke(target, name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getRemoteAddr() {
            try {
                return (String) _getRemoteAddr.invoke(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getClientIp() {
            String ip = getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = getRemoteAddr();
            }
            return ip;
        }

    }
}
