package com.myboy.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ResponseHandler extends ChannelInboundHandlerAdapter {

    private Object respMsg;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.respMsg = msg;
        ctx.channel().close();
    }

    public Object getRespMsg(){
        return respMsg;
    }
}
