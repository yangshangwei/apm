package com.artisan.utils;

import com.artisan.common.json.JsonWriter;
import com.artisan.intf.IOutput;
import com.artisan.model.JdbcStatistics;
import com.artisan.output.SimpleOutput;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 小工匠
 * @version 1.0
 * @description: TODO
 * @date 2020/10/8 0:30
 * @mark: show me the code , change the world
 */
public class SplitUtils {


    public static List<JdbcStatistics.ParamValues> split2Parts(Object[] args, int partSize) {
        List<JdbcStatistics.ParamValues> list = new ArrayList();
        for (int i = 1; i < args.length; i++) {
            JdbcStatistics.ParamValues paramValues = new JdbcStatistics.ParamValues((Integer) args[i - 1], args[i]);
            list.add(paramValues);
            i++;
        }
        System.out.println("LLLLLLLLLLLLLLL:" + JsonWriter.objectToJson(list));
        return list;
    }

    public static void main(String[] args) {
        Object[] arr = {1, "xx" , 2 , "yy",3,"sss"};
        List<JdbcStatistics.ParamValues> paramValues = split2Parts(arr, 2);
        JdbcStatistics jdbcStat = new JdbcStatistics();
        jdbcStat.setParams(paramValues);

        System.out.println(JsonWriter.objectToJson(jdbcStat));
    }
}
    