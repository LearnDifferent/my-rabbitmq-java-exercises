package direct;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Direct Exchange：
 * 发布者，发送的 routing key 有 ad, free, vip
 */
public class Publisher {

    private static final String EXCHANGE_NAME = "exchange_direct";
    private static final String FREE_CONTENT = "free";
    private static final String VIP_CONTENT = "vip";
    private static final String AD_CONTENT = "ad";

    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("user");
        factory.setPassword("123");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

            // 这里批量生成一些消息，并发送出去：
            for (int i = 0; i < 20; i++) {

                if (i % 2 == 0) {
                    // 注意第二个参数，要填写 routing key
                    channel.basicPublish(EXCHANGE_NAME, FREE_CONTENT, null,
                            "This is a free content".getBytes(StandardCharsets.UTF_8));
                    System.out.println("sent free content");
                } else if (1 == i || 9 == i || 11 == i) {
                    channel.basicPublish(EXCHANGE_NAME, VIP_CONTENT, null,
                            "Only VIP".getBytes(StandardCharsets.UTF_8));
                    System.out.println("sent vip content");
                } else {
                    channel.basicPublish(EXCHANGE_NAME, AD_CONTENT, null,
                            "AD".getBytes(StandardCharsets.UTF_8));
                    System.out.println("sent advertisement");
                }
            }
        }
    }

}
