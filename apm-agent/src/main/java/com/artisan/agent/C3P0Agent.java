package com.artisan.agent;

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

public class C3P0Agent {

    static String targetClass = "com.mchange.v2.c3p0.ComboPooledDataSource";

    public static void premain(String args , Instrumentation instrumentation){

        instrumentation.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader,
                                    String className,
                                    Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer)   {
                byte[] result = null;
                if (className != null && className.replace("/", ".").equals(targetClass)){
                    ClassPool pool = new ClassPool();
                    pool.insertClassPath(new LoaderClassPath(loader));
                    try {
                        CtClass ctl = pool.get(targetClass);
                        ctl.getConstructor("()V") //构造函数
                                .insertAfter("System.getProperties().put(\"c3p0Source$agent\", $0);"); // $0 this本身
                        result = ctl.toBytecode();
                        new C3P0Agent().openHttpServer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return result;
            }
        });

    }

    private void openHttpServer() throws IOException {
        InetSocketAddress addr = new InetSocketAddress(7777);
        HttpServer server = HttpServer.create(addr, 0);
        server.createContext("/", new HttpHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server is listening on port 7777");
    }


    private class HttpHandler implements com.sun.net.httpserver.HttpHandler {
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
