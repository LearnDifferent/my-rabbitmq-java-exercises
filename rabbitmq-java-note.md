[toc]

>   RabbitMQ 和 Java 相关笔记，代码在 [java-mvn](./java-mvn) 内。

# Work Queues 的模式

>   名词解释
>
>   生产者 Producer：用于发送消息的应用
>
>   消费者 Consumer：用于接收消息的应用
>
>   队列 Queue：用于存放消息

## 场景与问题

Send 和 Recv 类是最基础的使用方法，一个发送到 MQ 中，另一个从 MQ 中读取。

这里衍生出一个问题：如果生产者的速度比较快，消费者的速度比较慢，那么队列很快就满了。

应该怎么解决应用间生产者和消费者速度不同的问题？

## 轮询模式 Round-robin dispatching

可以创建多个接收消息的应用，来解决单个应用接收消息速度过慢的问题。

比如使用 SendMulti 类发送多条消息，round_robin 包下的 Recv01 和 Recv02 会按照其中一个消费者接收第 1、3、5、7 个消息，另一个消费者接收第 2、4、6、8 个消息的顺序来消费。

>   相关代码：round_robin 包下的 Recv01 和 Recv02

## 公平模式 Fair dispatch

在轮询的情况下，如果 Recv01 每次停顿几秒，就会和 Recv02 有速度上的差距。

这样的话，一边接受的是第偶数个，另一边会以不同速度接受第奇数个，总体效率就变低：

-   比如，A 以正常速度接收第 1、3、5、7 个消息，B 以慢 3 秒的速度接收第 2、4、6、8 个消息
-   因为 A 比较快，所以很快消费了 4 个消息，但是 B 依旧要以间隔 3 秒的速度接收 4 个消息
-   比较有效率的做法是，不要以第 1、3、5、7 个和第 2、4、6、8 个消息的分组来接收消息，而是让 A 和 B 能接收第几个就接收第几个，按照自己的速度来
-   这样的话，A 可能就接收 1、3、4、5、7，而 B 接收 2 和 8，这样的话，总体效率得到了提升

实现公平模式的步骤：

1.  限流：让 Server 在每一轮只能发送固定量的信息
2.  手动确认：取消自动确认，改为手动确认，每次确认后才进入下一轮
    1.  手动确认需要 Channel 实例的 `basicAck` 确认的方法
    2.  手动确认需要把 Channel 实例的 `basicConsume` 方法中，第二个参数修改为 false（auto ack 自动确认改为 false）

原理：不要按照一个接一个的顺序，而是按照消费者确认收到的顺序来运行。服务器需要每次发放固定数量的消息，而消费者必须确认消费后，才能从服务器那里获取下一批固定数量的消息。

>   相关代码：fair 包下的 Recv01 和 Recv02

# Publish/Subscribe

## Exchanges 交换机 - 基础概念

下文来自[官网教程](https://www.rabbitmq.com/tutorials/tutorial-three-java.html)：

The core idea in the messaging model in RabbitMQ is that **the producer never sends any messages directly to a queue**. Actually, quite often the producer doesn't even know if a message will be delivered to any queue at all.

Instead, **the producer can only send messages to an *exchange* **:

*   On one side it receives messages from producers and the other side it pushes them to queues
*   The exchange must know exactly what to do with a message it receives
    *   Should it be appended to a particular queue?
    *   Should it be appended to many queues?
    *   Should it get discarded?
*   The rules for that are defined by the ***exchange type***

![](https://www.rabbitmq.com/img/tutorials/exchanges.png)

There are a few exchange types available: 

1.  direct
2.  topic
3.  fanout
4.  headers（不常用）

***

Exchange 交换机的功能：

-   生成 Temporary Queues：
    -   这些队列是 exclusive（独占的，也就是排他队列）
    -   这些队列是临时的，用完后会自动删除
    -   队列名称是随机的，交换机会为每个订阅者（消费者）生成各自的队列
-   Bindings（绑定）队列

## Fanout Exchange：Mindless broadcast

Fanout：展开、散开、广播的含义

任何发送到 Fanout Exchange 的消息都会被转发到与该 Exchange 绑定（Binding）的所有 Queue 上：

-   交换机为每一个订阅者（消费者）自动生成一一对应的随机名称的队列
-   订阅者连接交换机为其生成的队列
-   发布者（生产者）将消息发送给交换机
-   交换机将消息，转发到每个队列中
-   每个队列将消息发送给订阅者
-   如果交换机没有绑定队列，消息就会丢失

>   相关代码：EmitLog、ReceiveLogs 和 ReceiveLogsAnother

***

问题：Fanout Exchange 只会 mindless broadcast（无差别的群发），无法定制

场景：如果某个频道，有免费用户和付费会员，那么 fanout exchange 就无法使用

## Routing：Publish/Subscribe - 使用路由键

### Routing Key：指定发送的队列

发布者（生产者）发送消息的时候，可以携带 routing key，来让 Exchange 根据 routing key 匹配相应的队列。

这需要队列提前 bind a routing key（绑定一个路由键）。

### Direct exchange

发布者发布内容后，交换机根据 routing key 发送给相应的订阅者。

>   相关代码：direct 包

### Topics 主题模式：Publish/Subscribe - 使用主题队列

如果 routing key 越来越多，管理起来就会非常麻烦，所以可以使用通配符来批量管理：

-   `*` (star) can substitute for exactly one word. 星号匹配恰好一个词
-   `#` (hash) can substitute for zero or more words. 井号匹配 0 个或多个词
-   例子：[“audit.#”能够匹配到“audit.irs.corporate”，但是“audit.*” 只会匹配到“audit.irs”](https://www.cnblogs.com/shenyixin/p/9084249.html)

这个没什么好说的，就是加上 `*` 或 `#` 来批量指定 routing key 而已。

需要注意的是：

-   发消息时设置的 routing key，不要使用通配符
-   接收消息设置的 routing key，才使用通配符

>   参考代码：EmitLogTopic 和 ReceiveLogsTopic

# Publisher Confirms 确认模式

## 学习确认模式之前：了解 RabbitMQ 的事务

问题：消费者在完成消费后，可以返回确认消息。如果消费者没有确认消息，还可以让服务器重发。但是，生产者无法知道消息是否发送成功。如果消费者没有发送成功，那么数据就会丢失。

所以，可以使用 Transaction 来解决，但是一般不要使用 Transaction，而要使用确认模式。

>   相关代码：tx 包

## 确认模式

确认模式：

1.  同步确认：等待服务器端发送确认成功的指令
    1.  普通确认：只能一次确认一条。返回值是 boolean，可以精确操作。
    2.  批量确认：能确认多条。只要其中一条没有确认成功，就会抛出异常。
2.  异步确认

一般使用异步确认。

# GitHub

相关链接：

- [RabbitMQ.md](https://github.com/LearnDifferent/my-notes/blob/master/RabbitMQ%E7%AC%94%E8%AE%B0.md)
- [spring-amqp-sample](https://github.com/LearnDifferent/spring-amqp-sample)

# 待补充：
- [ ] RPC：Remote Procedure Call 远程过程调用
- [ ] 异步的确认模式

参考资料：

-   [官网教程](https://www.rabbitmq.com/tutorials/)
-   [【3小时极速搞定RabbitMQ】RabbitMQ最新完整教程IDEA版通俗易懂，比狂神说讲的还深入的RabbitMQ教程](https://www.bilibili.com/video/BV1m44y167T3)
-   [【学相伴】RabbitMQ最新完整教程IDEA版通俗易懂 | KuangStudy | 狂神说 | 学相伴飞哥](https://www.bilibili.com/video/BV1dX4y1V73G)
-   [RabbitMQ三种Exchange模式(fanout,direct,topic)的性能比较](https://www.cnblogs.com/shenyixin/p/9084249.html)



