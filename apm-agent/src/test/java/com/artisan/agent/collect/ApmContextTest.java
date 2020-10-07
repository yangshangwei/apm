package com.artisan.agent.collect;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;


public class ApmContextTest {


    public static void main(String[] args) throws Exception {
        ApmContextTest test = new ApmContextTest();
        test.serviceTest();
        test.sqlTest();
    }

    public void serviceTest() {
        TestServiceImpl service = new TestServiceImpl();
        service.getUser("111", "xxxx");
    }

    @Test
    public void sqlTest() throws Exception {
        String name = "com.mysql.jdbc.Driver";
        Class.forName(name);
        Connection conn = DriverManager
                .getConnection(
                        "jdbc:mysql://127.0.0.1:3306/artisan",
                        "root", "artisan");
        PreparedStatement statment = conn
                .prepareStatement("select * from users");
        statment.executeQuery();
        statment.close();
        conn.close();
    }

}
