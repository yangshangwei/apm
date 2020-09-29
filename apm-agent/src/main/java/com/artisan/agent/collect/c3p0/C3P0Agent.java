package com.artisan.agent.collect.c3p0;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.security.ProtectionDomain;
import java.util.concurrent.Executors;

/**
 * @author 小工匠
 * @version 1.0
 * @description: C3P0插桩
 * @date 2020/8/29 9:15
 * @mark: show me the code , change the world
 */
public class C3P0Agent {

    // 要插装的类
    static String targetClass = "com.mchange.v2.c3p0.ComboPooledDataSource";

    public static void premain(String args , Instrumentation instrumentation){
        // 类转换器处理插桩逻辑
        instrumentation.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader,
                                    String className,
                                    Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer)   {
                //  插桩后的对象
                byte[] result = null;
                // 条件判断
                if (className != null && className.replace("/", ".").equals(targetClass)){
                    // 实例化pool , 将当前的ClassLoader设置到pool
                    ClassPool pool = new ClassPool();
                    pool.insertClassPath(new LoaderClassPath(loader));


                    try {
                        // 获取目标对象
                        CtClass ctl = pool.get(targetClass);
                        // 获取构造函数，插桩  将对象放入系统属性中
                        ctl.getConstructor("()V") //构造函数
                                .insertAfter("System.getProperties().put(\"c3p0Source$agent\", $0);"); // $0 this本身
                        // 转成class
                        result = ctl.toBytecode();
                        // 暴漏HTTP服务
                        new C3P0Agent().openHttpServer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return result;
            }
        });

    }

    /**
     * 对外提供Http 服务展示DataSource当前状态
     * @throws IOException
     */
    private void openHttpServer() throws IOException {
        InetSocketAddress addr = new InetSocketAddress(7777);
        HttpServer server = HttpServer.create(addr, 0);
        // 设置上下文
        server.createContext("/", new MyHttpHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }


    private class MyHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/plain;charset=UTF-8");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();
            // 输出c3p0状态
            responseBody.write(C3P0Agent.this.getStatus().getBytes());
            responseBody.flush();
            responseBody.close();
        }
    }


    public String getStatus() {
        Object source2 = System.getProperties().get("c3p0Source$agent");
        if (source2 == null) {
            return "未初始任何c3p0数据源";
        }
        return source2.toString();
    }
}
