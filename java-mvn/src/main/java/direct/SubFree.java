package direct;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.PropertyResourceBundle;
import java.util.concurrent.TimeoutException;


/**
 * Direct Exchange：
 * 普通订阅者，可以查看 routing key 为 free 和 ad 的内容（ad 为广告内容）
 */
public class SubFree {

    private static final String EXCHANGE_NAME = "exchange_direct";

    private static final String FREE_CONTENT = "free";
    private static final String AD_CONTENT = "ad";

    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("user");
        factory.setPassword("123");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        String queue = channel.queueDeclare().getQueue();

        channel.queueBind(queue, EXCHANGE_NAME, FREE_CONTENT);
        channel.queueBind(queue, EXCHANGE_NAME, AD_CONTENT);

        DeliverCallback callback = (consumerTag, deliveryMessage) -> {

            String message = new String(deliveryMessage.getBody(), StandardCharsets.UTF_8);
            System.out.println(deliveryMessage.
                    getEnvelope().getRoutingKey() + "：" + message);
        };

        channel.basicConsume(queue, true, callback, c -> {
        });
    }
}
