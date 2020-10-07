package com.artisan.agent.collect;

public class TestServiceImpl {
    // 1、拷拷贝目标方法重命名为方法后面加$agent
    // 2、拼装监控语句，并放至原方法



    public void getUser(String userid,String name) {
        // 1
        // 2
        System.out.println("获取用户信息:" + userid);
    }
}
