import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * 发送消息。
 * 发送后，会阻塞到 RabbitMQ 中，可以使用 Management 工具查看：http://localhost:15672/#/queues
 */
public class Send {

    // 定义队列名称，队列必须和 Recv 类的队列名称一致
    private final static String QUEUE_NAME = "my-queue";

    public static void main(String[] args) throws Exception {

        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672); // Java 连接这个端口
        factory.setUsername("user"); // 我设置的 RabbitMQ 的用户名
        factory.setPassword("123"); // 我设置的密码
//        factory.setVirtualHost("/vh01"); // 可以自定义一个 Virtual Host

        try (// 创建连接和 Channel
             Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()
        ) {

            /*
            queueDeclare 的参数：
            1. queue name 队列名称
            2. durable 是否持久化（如果持久化，那么重启服务器后，依旧会被保存）
            3. exclusive 是否排他（独占）
            4. auto delete 是否自动删除
            5. construction arguments 还可以添加其他参数
            =======================================================
            exclusive 排他队列特点：
            1. 只对创建该队列的连接可见
            2. 其他连接不可创建同名的排他队列（但是可以创建同名的非排他队列）
            3. 原始连接关闭时，这个排他队列会被强制删除
             */
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // 需要发送的消息
            String message = "Hello RabbitMQ!";

            /*
            发送消息的方法，其参数：
            1. exchange：如果为空字符串，就会找 default exchange(这里是 AMQP default)
            2. routing key：
                - 先查找有没有相应的 routing key
                - 如果没有找到，就去匹配队列名称
                - 如果还是没有，该消息就会被丢弃
            3. 其他消息相关的配置：一般传递 null 即可
            4. 消息体
             */
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
