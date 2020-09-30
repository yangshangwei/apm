package com.artisan.agent.entity;

import java.util.List;

/**
 * @author 小工匠
 * @version 1.0
 * @description: TODO
 * @date 2020/9/29 10:27
 * @mark: show me the code , change the world
 */
public class ServiceStatistics  extends  Statistics{

    public String errorMsg;
    public String errorType;
    public String serviceName; //服务名称
    public String simpleName; //服务简称
    public String methodName; //方法名称


    private List<String> parameterNameList;  // 参数名称[集合]
    private List<String> parameterTypeList;  // 参数类型[集合]
    private String returnType;               // 返回类型

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    public List<String> getParameterNameList() {
        return parameterNameList;
    }

    public void setParameterNameList(List<String> parameterNameList) {
        this.parameterNameList = parameterNameList;
    }

    public List<String> getParameterTypeList() {
        return parameterTypeList;
    }

    public void setParameterTypeList(List<String> parameterTypeList) {
        this.parameterTypeList = parameterTypeList;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

//    @Override
//    public String toString() {
//        return "ServiceStatistics{" +
//                "errorMsg='" + errorMsg + '\'' +
//                ", errorType='" + errorType + '\'' +
//                ", serviceName='" + serviceName + '\'' +
//                ", simpleName='" + simpleName + '\'' +
//                ", methodName='" + methodName + '\'' +
//                '}' +super.toString();
//    }


    @Override
    public String toString() {
        return "ServiceStatistics{" +
                "errorMsg='" + errorMsg + '\'' +
                ", errorType='" + errorType + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", simpleName='" + simpleName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterNameList=" + parameterNameList +
                ", parameterTypeList=" + parameterTypeList +
                ", returnType='" + returnType + '\'' +
                '}';
    }
}
    