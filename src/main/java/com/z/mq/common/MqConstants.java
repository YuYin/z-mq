/**
 * The MIT License (MIT)
 * Copyright (c) 2009-2015 HONG LEIMING
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.z.mq.common;

import java.nio.charset.Charset;


public class MqConstants {

    public static final int magic_number = 0x12345678;
    public static final int version = 0;

    public static final Charset UTF_8 = Charset.forName("UTF-8");


    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";


    public static final String HeartBeat = "heartBeat";   //heartbeat

    public static final String Produce = "produce";   //produce message
    public static final String Consume = "consume";   //consume message
    public static final String Route = "route";     //route back message to sender

    public static final String QueryMQ = "query_mq";
    public static final String CreateMQ = "create_mq"; //create MQ
    public static final String RemoveMQ = "remove_mq"; //remove MQ

    public static final String AddKey = "add_key";
    public static final String RemoveKey = "remove_key";


    public static final String Auth = "auth";
    public static final String Test = "test";
    public static final String Data = "data";




}
