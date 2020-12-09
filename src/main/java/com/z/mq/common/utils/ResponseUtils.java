/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.common.utils;

import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.protocol.ResponsePacket;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/18
 */
public class ResponseUtils {

        public static ResponsePacket buildResponse(MessagePacket request, Object result, String message, String code, boolean success) {
        ResponsePacket response = new ResponsePacket();
        response.setRequestId(request.getRequestId());
        response.setPayload(result);
        response.setMagicNumber(request.getMagicNumber());
        response.setVersion(request.getVersion());
        response.setRequestId(request.getRequestId());
        response.setCode(code);
        response.setMessage(message);
        response.setSuccess(success);
        return response;
    }
}
