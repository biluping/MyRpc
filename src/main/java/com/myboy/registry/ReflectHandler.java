package com.myboy.registry;

import com.myboy.protocol.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

public class ReflectHandler extends SimpleChannelInboundHandler<RpcProtocol> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol protocol) throws Exception {
        String className = protocol.getClassName();
        Class<?> rpcClass = Class.forName(className);
        Method method = rpcClass.getMethod(protocol.getMethodName(), protocol.getParameterClass());
        Object invoke = method.invoke(rpcClass.newInstance(), protocol.getParameterValue());
        ctx.writeAndFlush(invoke);
    }
}
