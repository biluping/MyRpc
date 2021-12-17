package com.myboy.consumer;

import com.myboy.api.HelloService;
import com.myboy.proxy.RpcProxy;

public class Consumer {
    public static void main(String[] args) {
        HelloService helloService = RpcProxy.create(HelloService.class);

        String msg = helloService.hello("tom");
        System.out.println(msg);
    }
}
