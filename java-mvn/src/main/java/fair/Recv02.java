package fair;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

/**
 * 公平模式
 * 这个消费者没有停顿，所以速度比另一个消费者快
 */
public class Recv02 {

    private final static String QUEUE_NAME = "my-queue";

    public static void main(String[] args) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("user");
        factory.setPassword("123");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Quality of Service
        channel.basicQos(1);

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");

            // acknowledge the message
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };

        // Don't forget to set auto ack to false
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });

    }
}
