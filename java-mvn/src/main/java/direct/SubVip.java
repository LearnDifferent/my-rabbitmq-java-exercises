package direct;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Direct Exchange：
 * VIP 订阅者，可以查看 routing key 为 free 和 vip 的内容
 */
public class SubVip {

    private static final String EXCHANGE_NAME = "exchange_direct";

    // routing key（一个订阅者可以有多个 routing key）
    private static final String FREE_CONTENT = "free";
    private static final String VIP_CONTENT = "vip";

    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("user");
        factory.setPassword("123");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Direct Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        // 订阅者从交换机处获取属于自己的临时队列
        String queue = channel.queueDeclare().getQueue();

        // VIP 可以查看免费的内容
        channel.queueBind(queue, EXCHANGE_NAME, FREE_CONTENT);
        // VIP 可以查看 VIP 内容
        channel.queueBind(queue, EXCHANGE_NAME, VIP_CONTENT);

        // Callback 来打印消息：
        DeliverCallback callback = (consumerTag, deliveryMessage) -> {

            String message = new String(deliveryMessage.getBody(), StandardCharsets.UTF_8);
            // 这里打印 routing key 和信息：
            System.out.println(deliveryMessage.
                    getEnvelope().getRoutingKey() + "：" + message);
        };

        channel.basicConsume(queue, true, callback, c -> {
        });
    }
}
