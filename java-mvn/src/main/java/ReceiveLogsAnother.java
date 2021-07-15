import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

/**
 * 完全拷贝自 ReceiveLogs，为了测试多个订阅者的情况
 */
public class ReceiveLogsAnother {

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

        // 通过 Channel 的队列声明来获取队列名称
        String queueName = channel.queueDeclare().getQueue();

        // 绑定队列：队列名称，交换机名称，routing key
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };

        // 这里第一个参数要传入队列名称
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }
}
