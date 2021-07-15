package tx;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 事务中的生产者
 */
public class Send {

    private static final String QUEUE_NAME = "tx";

    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("user");
        factory.setPassword("123");

        Connection connection = null;
        Channel channel = null;

        try {

            connection = factory.newConnection();
            channel = connection.createChannel();

            // 开启事务
            channel.txSelect();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String msg = "Testing  Transaction";
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes(StandardCharsets.UTF_8));

            // todo 这里故意来个异常：
//            int i = 1 / 0;

            // 发送成功的时候，提交事务
            channel.txCommit();
            // 打印消息：
            System.out.println("Sent message: " + msg);

        } catch (Exception e) {
            assert channel != null;
            // 如果出现异常，就回滚事务：
            channel.txRollback();
            System.err.println("出现异常，已回滚");
        } finally {
            if (channel != null) {
                channel.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
    }
}
