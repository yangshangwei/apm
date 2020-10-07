package com.artisan;


import com.artisan.common.Logs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;


public class ApmAgent {
    public static void premain(String arg, Instrumentation instrumentation) {
        Logs.info("arg:" + arg);
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
        ApmContext context = new ApmContext(properties, instrumentation);
    }

}
