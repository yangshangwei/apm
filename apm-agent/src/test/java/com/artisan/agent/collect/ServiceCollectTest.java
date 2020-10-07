package com.artisan.agent.collect;

import com.artisan.ApmContext;
import com.artisan.collects.ServiceCollect;
import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;


public class ServiceCollectTest {

    private ApmContext context;
    private MockInstrumentation instrumentation;
    private ServiceCollect serviceCollect;

    @Before
    public void init() {
        instrumentation = new MockInstrumentation();
        Properties pro = new Properties();
        pro.put("service.include", "com.artisan.apm1.*&com.artisan.apm.test.*");
        pro.put("service.exclude", "com.artisan.apm.test1.*");
        context = new ApmContext(pro, instrumentation);
        serviceCollect = new ServiceCollect(context, instrumentation);
    }

    @Test
    public void collectTest() throws IOException, CannotCompileException, NotFoundException {
        String name = "com.artisan.apm.test.TestServiceImpl";
        byte[] classBytes = serviceCollect.transform(ServiceCollectTest.class.getClassLoader(), name);
        ClassPool pool = new ClassPool();
        pool.insertClassPath(new ByteArrayClassPath(name, classBytes));
        pool.get(name).toClass();
        new TestServiceImpl().getUser("111", "dddd");
    }

    @Test
    public void batchTest() throws IOException, CannotCompileException, NotFoundException {
        String name = "com.artisan.apm.test.TestServiceImpl";
        byte[] classBytes = serviceCollect.transform(ServiceCollectTest.class.getClassLoader(), name);
        ClassPool pool = new ClassPool();
        pool.insertClassPath(new ByteArrayClassPath(name, classBytes));
        pool.get(name).toClass();
        for (int i = 0; i < 100; i++) {
            new TestServiceImpl().getUser("111", "dddd");
        }
    }


}
