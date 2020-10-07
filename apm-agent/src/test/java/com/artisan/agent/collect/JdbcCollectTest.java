package com.artisan.agent.collect;

import com.artisan.ApmContext;
import com.artisan.collects.JdbcCommonCollects;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

public class JdbcCollectTest {
	private ApmContext context;
	private MockInstrumentation instrumentation;
	private JdbcCommonCollects jdbcCollect;

	@Ignore
	@Before
	public void init() {
		instrumentation = new MockInstrumentation();
		Properties pro=new Properties();
		context = new ApmContext(pro, instrumentation);
		jdbcCollect = new JdbcCommonCollects(context, instrumentation);
	}

	@Ignore
	@Test
	public void buildTest() throws Exception {
		String name = "com.mysql.jdbc.NonRegisteringDriver";
		byte[] classBytes = jdbcCollect.transform(
				ServiceCollectTest.class.getClassLoader(), name);
		ClassPool pool = new ClassPool();
		pool.insertClassPath(new ByteArrayClassPath(name, classBytes));
		pool.get(name).toClass();
		Class.forName(name);
		sqlTest();
	}
	@Test
	public void sqlTest() throws Exception {

		Connection conn = DriverManager
				.getConnection(
						"jdbc:mysql://127.0.0.1/artisan",
						"artisan", "artisan");
		PreparedStatement statment = conn
				.prepareStatement("select * from users");
		statment.executeQuery();
		statment.close();
		conn.close();
	}
	@Test
	public void testBatch() throws Exception {
		buildTest();
		for (int i = 0; i < 130; i++) {
			sqlTest();
		}
	}
}
