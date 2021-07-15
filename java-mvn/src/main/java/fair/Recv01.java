package fair;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 公平模式
 * 这个消费者，让它的停顿一下，来达到多个消费者速度不同的目的。
 */
public class Recv01 {

    private final static String QUEUE_NAME = "my-queue";

    public static void main(String[] args) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("user");
        factory.setPassword("123");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 设置 QoS（Quality of Service）来完成 flow control（限流）：
        // 在没有得到确认之前，Server 只能发送固定数量的数据给消费者
        // 设置为 1，表示一次只能发送 1 条消息
        channel.basicQos(1);

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");

            // 阻塞一下，让这个消费者变慢
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 手动确认，参数为：
            // 1. delivery tag 回调消息的标识
            // 2. multiple：true 表示确认所有消息，false 表示只确认消费了的消息
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };

        // 第二个参数 auto acknowledgment 要设置为 false
        // 也就是取消自动确认，改为手动确认
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });

    }
}
