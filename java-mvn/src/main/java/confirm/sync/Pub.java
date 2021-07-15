package confirm.sync;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * Publisher Confirms
 * 这里是普通的确认和批量的确认，两者都是同步的
 */
public class Pub {

    private final static String QUEUE_NAME = "confirm_sync";

    public static void main(String[] args) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("user");
        factory.setPassword("123");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 启动确认模式
            channel.confirmSelect();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Confirm";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));

            /*
            普通的确认，就是等待服务器返回确认的信息
            使用的是 waitForConfirms 这个方法，该方法还可以添加参数，参数为等待超时
             */
//            normalConfirm(channel, message);

            // 批量的确认。只要有一条消息确认失败，会抛出异常（也可以添加等待超时的参数）
            channel.waitForConfirmsOrDie();
            System.out.println(" [x] Sent '" + message + "'");
        }
    }

    private static void normalConfirm(Channel channel, String message) throws InterruptedException {

        boolean success = channel.waitForConfirms();
        if (success) {
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}