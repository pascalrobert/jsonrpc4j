package com.googlecode.jsonrpc4j;

import com.googlecode.jsonrpc4j.JsonRpcParamName;

public interface TestService {
    void execVoid();
    int execInt();
    Long execLong();
    Boolean execBoolean();
    String execString(int param0, long param1);
    String execString(int param0, int param1);
    TestBean execJavaBean();
    TestBean execJavaBean(int[] testicles);
    void execJavaBean2(@JsonRpcParamName("bean") TestBean arg0, @JsonRpcParamName("age") Integer arg1);
}
