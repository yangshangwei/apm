package com.artisan.collects;


import com.artisan.ApmContext;
import com.artisan.intf.ICollect;
import com.artisan.common.WildcardMatcher;
import com.artisan.model.ServiceStatistics;
import javassist.*;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

/**
 * 服务接口响应性能采集
 */
public class ServiceCollect extends AbstractByteTransformCollect implements ICollect {
    public static ServiceCollect INSTANCE;
    private final ApmContext context;
    private WildcardMatcher excludeMatcher=null; // 排除非哪些类
    private  WildcardMatcher includeMatcher=null;// 包含哪些类
    private String include;
    private String exclude;

    private static final String beginSrc;
    private static final String endSrc;
    private static final String errorSrc;

    static {
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("com.artisan.collects.ServiceCollect instance= ");
        sbuilder.append("com.artisan.collects.ServiceCollect.INSTANCE;\r\n");
        sbuilder.append("com.artisan.model.ServiceStatistics statistic =instance.begin(\"%s\",\"%s\");");
        beginSrc = sbuilder.toString();
        sbuilder = new StringBuilder();
        sbuilder.append("instance.end(statistic);");
        endSrc = sbuilder.toString();
        sbuilder = new StringBuilder();
        sbuilder.append("instance.error(statistic,e);");
        errorSrc = sbuilder.toString();
    }

    // include
    // exclude
    public ServiceCollect(ApmContext context, Instrumentation instrumentation) {
        super(instrumentation);
        this.context = context;
        if(context.getConfig("service.include")!=null){
            includeMatcher=new WildcardMatcher(context.getConfig("service.include"));
        }else{
            System.err.println("[error]未配置 'service.include'参数无法监控service服务方法");
        }
        if(context.getConfig("service.exclude")!=null) {
            excludeMatcher = new WildcardMatcher(context.getConfig("service.exclude"));
        }
        INSTANCE = this;
    }

    public ServiceStatistics begin(String className, String methodName) {
        ServiceStatistics bean = new ServiceStatistics();
        bean.setRecordTime(System.currentTimeMillis());
//        bean.setHostIp();
//        bean.setHostName();
        bean.setBegin(System.currentTimeMillis());
        bean.setServiceName(className);
        bean.setMethodName(methodName);
        bean.setSimpleName(className.substring(className.lastIndexOf(".")));
        bean.setModelType("service");
        return bean;
    }

    public void error(ServiceStatistics bean, Throwable e) {
        bean.setErrorType(e.getClass().getSimpleName());
        bean.setErrorMsg(e.getMessage());
    }

    public void end(ServiceStatistics bean) {
        bean.setEnd(System.currentTimeMillis());
        bean.setUseTime(bean.end - bean.begin);
        context.submitCollectResult(bean);
    }


    @Override
    public byte[] transform(ClassLoader loader, String className) throws CannotCompileException, NotFoundException, IOException {

        if (includeMatcher==null) {
            return null;
        }else if(!includeMatcher.matches(className)){ // 包含指定类
            return null;
        }else if(excludeMatcher!=null && excludeMatcher.matches(className)){ // 排除指定类
            return null;
        }


        CtClass ctclass = toCtClass(loader, className);
        if (ctclass.isInterface()) {// 排除接口
            return null;
        }
        AgentByteBuild byteBuild = new AgentByteBuild(className, loader, ctclass);
        CtMethod[] methods = ctclass.getDeclaredMethods();
        for (CtMethod m : methods) {
            // 屏蔽非公共方法
            if (!Modifier.isPublic(m.getModifiers())) {
                continue;
            }
            // 屏蔽静态方法
            if (Modifier.isStatic(m.getModifiers())) {
                continue;
            }
            // 屏蔽本地方法
            if (Modifier.isNative(m.getModifiers())) {
                continue;
            }
            AgentByteBuild.MethodSrcBuild build = new AgentByteBuild.MethodSrcBuild();
            build.setBeginSrc(String.format(beginSrc, className, m.getName()));
            build.setEndSrc(endSrc);
            build.setErrorSrc(errorSrc);
            byteBuild.updateMethod(m, build);
        }
        return byteBuild.toBytecote();
    }
}
