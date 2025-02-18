import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

/**
 * 接受消息。
 */
public class Recv {

    // 队列必须和 Send 类的队列名称一致
    private final static String QUEUE_NAME = "my-queue";

    public static void main(String[] args) throws Exception {

        // 参考 Send 类
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("user");
        factory.setPassword("123");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };

        /*
          basicConsume 是最基本的消费消息的方法
          参数：
          1. 队列名称
          2. auto ack 自动确认收到了消息
          3. deliver callback: callback when a message is delivered
          4. cancel callback: callback when the consumer is cancelled
         */
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
