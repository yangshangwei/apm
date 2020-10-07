package com.artisan;

import com.artisan.collects.HttpCollect;
import com.artisan.collects.JdbcCommonCollects;
import com.artisan.collects.ServiceCollect;
import com.artisan.filter.JSONFormat;
import com.artisan.intf.ICollect;
import com.artisan.intf.IFilter;
import com.artisan.intf.IOutput;
import com.artisan.output.SimpleOutput;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ApmContext {
    private Instrumentation instrumentation;
    private Properties properties;
    List<ICollect> collects = new ArrayList();
    IFilter filter;
    IOutput output;

    public ApmContext(Properties properties, Instrumentation instrumentation) {
        if(properties==null){
            throw new RuntimeException("properties 不能为空");
        }
        this.properties = properties;
        this.instrumentation = instrumentation;
        // 注册采集器 IOC
        collects.add(new ServiceCollect(this, instrumentation));
        collects.add(new JdbcCommonCollects(this, instrumentation));
        collects.add(new HttpCollect(this,instrumentation));
        //filter 注册
        filter = new JSONFormat();
        //输出器注册
        output = new SimpleOutput(properties);
    }

    // 递交采集结果
    public void submitCollectResult(Object value) {
        // TODO 基于线程后台执行
        value = filter.doFilter(value);
        output.out(value);
    }

    public String getConfig(String key) {
        return properties.getProperty(key);
    }

    public List<ICollect> getCollects() {
        return collects;
    }
}
