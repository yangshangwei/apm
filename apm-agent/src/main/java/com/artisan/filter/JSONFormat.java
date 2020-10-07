package com.artisan.filter;

import com.artisan.intf.IFilter;
import com.artisan.common.JsonUtil;

import java.io.Serializable;


public class JSONFormat implements IFilter {
    @Override
    public Object doFilter(Object value) {
        if (value == null)
            return null;
        else if (!(value instanceof Serializable)) {
            return null;
        }
        return JsonUtil.toJson(value);
    }
}
