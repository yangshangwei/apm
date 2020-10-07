package com.artisan.output;


import com.artisan.intf.IOutput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;


public class SimpleOutput implements IOutput {
    private FileWriter fileWriter;


    public SimpleOutput(Properties properties) {
        String logValue = properties.getProperty("log");
        System.out.println("logValue:" + logValue);
        try {
            fileWriter =
                    new FileWriter(openFile(logValue), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean out(Object value) {
        try {
            fileWriter.write( value.toString() +"\r\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private File openFile(String rootDir) {
        try {
            if (rootDir == null || rootDir.trim().equals("")) {
                rootDir = System.getProperty("user.dir") + "/logs/";
            }
            File root = new File(rootDir);
            if (!root.exists() || !root.isDirectory()) {
                root.mkdirs();
            }
            File file = new File(root, "apm-agent.log");
            if (file.exists()) {
                file.createNewFile();
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
