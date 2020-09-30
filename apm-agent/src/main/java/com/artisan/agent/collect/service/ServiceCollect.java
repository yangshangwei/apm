package com.artisan.agent.collect.service;

import com.alibaba.fastjson.JSON;
import com.artisan.agent.collect.AbstractCollect;
import com.artisan.agent.entity.ServiceStatistics;
import javassist.*;

import java.lang.instrument.Instrumentation;

/**
 * @author 小工匠
 * @version 1.0
 * @description: TODO
 * @date 2020/9/29 10:21
 * @mark: show me the code , change the world
 */
public class ServiceCollect   extends AbstractCollect {
    // 监控路径
    private String targetPackage ;

    // 带有返回值的模板
    final static String source = "{\n"
            + "%s"
            + "        Object result=null;\n"
            + "       try {\n"
            + "            result=($w)%s$agent($$);\n"
            + "        } catch (Throwable e) {\n"
            + "%s"
            + "            throw e;\n"
            + "        }finally{\n"
            + "%s"
            + "        }\n"
            + "        return ($r) result;\n"
            + "}\n";

    // 不带返回值的模板
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

    public  ServiceCollect(String targetPackage){
        this.targetPackage = targetPackage;
    }


    public void transform(Instrumentation instrumentation){
        instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {

            if (className == null) {
                return null;
            }
            if (!className.startsWith(targetPackage.replaceAll("\\.", "/"))) {
                return null;
            }
            try {
                return buildCtClass(loader, className.
                        replaceAll("/", ".")).toBytecode();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }


    public CtClass buildCtClass(ClassLoader loader, String className) throws NotFoundException {
        ClassPool pool = new ClassPool();
        pool.insertClassPath(new LoaderClassPath(loader));

        CtClass ctClass = pool.get(className);
        CtMethod[] methods = ctClass.getDeclaredMethods();
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
            try {
                buildMethod(ctClass, m);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ctClass;

    }
    public void buildMethod(CtClass ctClass, CtMethod oldMethod) throws Exception {
        // copy 一个方法
        // 修改源方法名称 $agent
        // 原方法中 插入模板代码
        CtMethod newMethod = CtNewMethod.copy(oldMethod, ctClass, null);
        oldMethod.setName(oldMethod.getName() + "$agent");
        String beginSrc = String.
                format("Object stat=com.artisan.agent.collect.service.ServiceCollect.begin(\"%s\",\"%s\");", ctClass.getName(), oldMethod.getName());
        String errorSrc = "com.artisan.agent.collect.service.ServiceCollect.error(e,stat);";
        String endSrc = "com.artisan.agent.collect.service.ServiceCollect.end(stat);";
        String template = oldMethod.getReturnType().getName().equals("void") ? voidSource : source;
        newMethod.setBody(String.format(template, beginSrc, newMethod.getName(), errorSrc, endSrc));
        ctClass.addMethod(newMethod);
    }


    public static ServiceStatistics begin(String className, String methodName) {
        ServiceStatistics bean = new ServiceStatistics();
        bean.setBeginTime(System.currentTimeMillis());
        bean.setServiceName(className);
        bean.setMethodName(methodName);
        bean.setSimpleName(className.substring(className.lastIndexOf(".")));
        bean.setModelType("service");
//        System.out.println(JSON.toJSONString(bean));




        return bean;
    }

    public static void error(Throwable e, Object obj) {
        ServiceStatistics bean = (ServiceStatistics) obj;
        bean.setErrorType(e.getClass().getSimpleName());
        bean.setErrorMsg(e.getMessage());
    }

    public static void end(Object obj) {
        ServiceStatistics bean = (ServiceStatistics) obj;
        bean.setUseTime(System.currentTimeMillis() - bean.getBeginTime());
        System.out.println(JSON.toJSONString(obj));
    }

    // Object obj= begin (className,methodName)
    // error(err,obj)
    // end(obj)


}
    