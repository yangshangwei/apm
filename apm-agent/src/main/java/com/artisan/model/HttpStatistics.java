package com.artisan.model;

public class HttpStatistics extends  BaseStatistics {
    private long beginTime;
    private String url;
    private String clientIp;
    private String error;
    private long useTime;

    public HttpStatistics() {
        this.setModelType("http");
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getUseTime() {
        return useTime;
    }

    public void setUseTime(long useTime) {
        this.useTime = useTime;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    @Override
    public String toString() {
        return "HttpStatistics{" +
                "url='" + url + '\'' +
                ", clientIp='" + clientIp + '\'' +
                '}'+super.toString();
    }
}
