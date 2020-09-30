package com.artisan.agent;

import com.artisan.agent.collect.service.ServiceCollect;

import java.lang.instrument.Instrumentation;

/**
 * @author 小工匠
 * @version 1.0
 * @description: TODO
 * @date 2020/9/29 10:18
 * @mark: show me the code , change the world
 */
public class ApmAgent {


    public static void premain(String args , Instrumentation instrumentation){

        System.out.println("args------------" + args);
        // 这个地方 args 可以任意填写，格式不限制，只要自己能解析就OK了
        // -javaagent:D:\IdeaProjects\artisan-apm\apm-agent\target\apm-agent-1.0-SNAPSHOT.jar=parm1=abc&parma2=bcde
        // 那么 args 就是 parm1=abc&parma2=bcde
        assert args != null;

        String[] split = args.split("&");
        for (String s : split) {
            // 开启 Service 采集器
            new ServiceCollect(s).transform(instrumentation);
        }



    }
}
    