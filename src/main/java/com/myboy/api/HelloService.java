package com.myboy.api;

import com.myboy.annotation.RpcImpl;

@RpcImpl("com.myboy.provider.HelloServiceImpl")
public interface HelloService {

    String hello(String name);
}
