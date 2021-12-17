package com.myboy.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * RPC 远程调用协议
 */
@Data
public class RpcProtocol implements Serializable {

    private String className;
    private String methodName;
    private Class<?>[] parameterClass;
    private Object[] parameterValue;
}
