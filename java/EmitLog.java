import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * Fanout Exchange
 * 发送到交换机的发布者
 * 接收订阅的消费者为 ReceiveLogs 和 ReceiveLogsAnother
 */
public class EmitLog {

    // 交换机的名称
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("user");
        factory.setPassword("123");
        factory.setPort(5672);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 参数：交换机的名称，交换机的类型
            // 类型可以使用字符串，如"fanout"，下面这个枚举也可以：
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

            // String 类型的消息
            String message = "This is a log";

            /*
            basicPublish 是发布的方法，参数为：
            1. exchange name
            2. routing key
            3. props: other properties for the message - routing headers etc
            4. message body：注意转化为 byte 数组
             */
            channel.basicPublish(EXCHANGE_NAME, "", null,
                    message.getBytes(StandardCharsets.UTF_8));

            System.out.println(" [x] Sent '" + message + "'");
        }
    }

}

