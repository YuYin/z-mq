# z-mq

### 介绍

轻量级mq,基于netty网络通信,DiskQueue持久化队列模型

### 特性
- 生产端和消费端和Broker保持单一长连接，心跳保活
- 支持发布订阅模式,支持点对点模式
- 支持消息高性能序列化转换，异步化发送消息
- 支持消息落盘,内存队列(快速投递消息)
- 支持延迟投递消息

### 待实现特性
- 消费端ack机制,自动Ack,手动Ack
- 消费端幂等机制
- HA模式
- 消息监控


###DiskQueue 消息队列存储模型

![img](http://static.oschina.net/uploads/space/2015/0713/001759_I2Ui_582242.png)

### 使用
参考com.z.mq.test包下的测试代码
###参考
参考自zbus
- https://my.oschina.net/xnkl/blog/477690
- https://gitee.com/rushmore/zbus.git



