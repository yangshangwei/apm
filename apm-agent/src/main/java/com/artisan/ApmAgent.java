package com.artisan;


import com.artisan.common.Logs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Properties;


public class ApmAgent {
    public static void premain(String arg, Instrumentation instrumentation) {
        System.out.println("====arg:" + arg);
        Properties properties = new Properties();
        // 装载配置文件
//        if (arg != null && !arg.trim().equals("")) {
            // jvm参数配置
//            try {
//                properties.load(new ByteArrayInputStream(
//                        arg.replaceAll(",", "\n").getBytes()));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        ApmContext context = new ApmContext(properties, instrumentation);


        // jvm
        // 外部文件配置
        // agent.jar所在目錄  conf/apm.conf
        Properties configByJvm = getConfigByJvm(arg);
        Properties configFiles = getConfigFiles();
        //jvm的配置高于config
        configFiles.putAll(configByJvm);

        ApmContext context = new ApmContext(configFiles, instrumentation);
    }


    private static Properties getConfigByJvm(String arg){
        Properties properties = new Properties();
        // 装载配置文件
        if (arg != null && !arg.trim().equals("")) {
            try {
                properties.load(new ByteArrayInputStream(
                        arg.replaceAll(",", "\n").getBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // key=value,key2=value2
        return properties;
    }

    // 读取agent 文件 配置
    private static Properties getConfigFiles() {
        URL u = ApmAgent.class.getProtectionDomain().getCodeSource().getLocation();
        File file = new File(new File(u.getFile()).getParentFile(), "conf/apm.conf");
        if (!file.exists() || file.isDirectory()) {
            return new Properties();
        }
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("log----" + properties.getProperty("log"));
        System.out.println("service.include----" + properties.getProperty("service.include"));

        return properties;
    }


}
