package com.artisan.model;

import java.util.ArrayList;
import java.util.List;

public class JdbcStatistics extends BaseStatistics {
	public Long begin;// 时间戳
	public Long end;
	public Long useTime;
	// jdbc url
	public String jdbcUrl;
	// sql 语句
	public String sql;
	// 数据库名称
	public String databaseName;
	
	public String error;
	public String errorType;
	// 是否经过预处理
	public String preman;

	public List<ParamValues> params=new ArrayList();

	public List<ParamValues> getParams() {
		return params;
	}

	public void setParams(List<ParamValues> params) {
		this.params = params;
	}

	public JdbcStatistics() {

	}

	public static class ParamValues{
		public ParamValues(int index, Object value) {
			this.index = index;
			this.value = value;
		}

		public int index;
		public Object value;
	}
}