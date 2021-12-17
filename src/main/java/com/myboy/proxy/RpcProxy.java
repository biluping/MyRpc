package com.myboy.proxy;

import com.myboy.annotation.RpcImpl;
import com.myboy.protocol.RpcProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * 用于创建api的代理类，添加远程调用功能
 */
public class RpcProxy {

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> t) {
        return (T) Proxy.newProxyInstance(t.getClassLoader(), new Class<?>[]{t}, new RpcHandler(t));
    }

    private static class RpcHandler implements InvocationHandler {

        private final Class<?> originClass;

        public RpcHandler(Class<?> originClass) {
            this.originClass = originClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            RpcImpl rpcImpl = originClass.getAnnotation(RpcImpl.class);
            if (rpcImpl == null) {
                throw new NullPointerException("远程调用接口"+ originClass.getName() +"未添加 @RpcImpl 注解");
            }

            RpcProtocol protocol = new RpcProtocol();
            protocol.setClassName(rpcImpl.value());
            protocol.setMethodName(method.getName());
            protocol.setParameterClass(method.getParameterTypes());
            protocol.setParameterValue(args);

            ResponseHandler handler = new ResponseHandler();
            NioEventLoopGroup group = new NioEventLoopGroup(1);
            ChannelFuture future = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())));
                            pipeline.addLast(handler);
                        }
                    }).connect(new InetSocketAddress("localhost", 8080)).sync();

            // 发送消息
            future.channel().writeAndFlush(protocol);

            future.channel().closeFuture().sync();
            group.shutdownGracefully();


            return handler.getRespMsg();
        }
    }
}
