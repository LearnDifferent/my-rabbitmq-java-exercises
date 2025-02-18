import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

/**
 * Fanout Exchange 情况下，接收订阅消息的消费者
 * 发布消息的是 EmitLog
 */
public class ReceiveLogs {

    // 交换机名称
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("user");
        factory.setPassword("123");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 交换机的名称和类型
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

        // 这个 channel 声明了 exchange，而 exchange 会自动创建临时的队列给订阅者
        // 所以，订阅者可以这样获取属于自己的临时队列：
        String queue = channel.queueDeclare().getQueue();

        // 绑定队列：队列，交换机名称，routing key
        channel.queueBind(queue, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };

        // 这里第一个参数要传入队列
        channel.basicConsume(queue, true, deliverCallback, consumerTag -> {
        });
    }
}

