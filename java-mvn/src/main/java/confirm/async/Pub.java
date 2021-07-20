package confirm.async;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import confirm.ConfirmConst;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Publisher Confirms
 * 这里是异步确认
 */
public class Pub {

    public static void main(String[] args) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("user");
        factory.setPassword("123");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 开启确认模式
            channel.confirmSelect();

            // 声明队列
            channel.queueDeclare(ConfirmConst.ASYNC_QUEUE_NAME, false, false, false, null);

            // 定义一个 unconfirmed 集合
            SortedSet<Long> unconfirmedSet = Collections.synchronizedSortedSet(new TreeSet<>());

            // Channel 开启监听
            channel.addConfirmListener(new ConfirmListener() {

                /**
                 * 处理已确认的消息
                 *
                 * @param deliveryTag 已确认消息的序列号
                 * @param multiple true 时候，表示监听到多条确认；false 时，表示监听到单条确认
                 * @throws IOException 可能会有 IO 异常
                 */
                @Override
                public void handleAck(long deliveryTag, boolean multiple) throws IOException {

                    // 打印一下标识
                    System.out.println("Confirm: multiple-" + multiple + "\t deliveryTag-" + deliveryTag);

                    if (multiple) {
                        // 如果是多条确认，表示确认了序列号为 deliveryTag 及比 deliveryTag 数值还小的序列号的消息
                        // 所以需要批量去除该序列号，及比该序列号的数值还小的序列号的消息（注意 headSet 方法的用法）
                        unconfirmedSet.headSet(deliveryTag + 1L).clear();
                        /*
                         SortedSet 的 headSet 方法的用法：
                         假设 SortedSet 内有 [2, 3, 4, 5, 6, 7, 8, 9]
                         如果使用 headSet(7)，就会返回 [2, 3, 4, 5, 6]
                         */
                    } else {
                        // 如果是单条确认，直接移除即可
                        unconfirmedSet.remove(deliveryTag);
                    }

                    // 打印一下当前还未确认的集合
                    System.out.println("Still unconfirmed: " + unconfirmedSet);
                }

                // 处理被拒绝确认的消息
                @Override
                public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                    // 参考 handleAck 即可
                    System.out.println("Refuse: multiple-" + multiple + "\t deliveryTag-" + deliveryTag);
                    if (multiple) {
                        unconfirmedSet.headSet(deliveryTag + 1L).clear();
                    } else {
                        unconfirmedSet.remove(deliveryTag);
                    }

                    // 这里应该写 Nack 之后的处理方法
                    System.out.println("处理 Nack...");
                }
            });

            // 这里模拟发送多条消息
            for (int i = 0; i < 100; i++) {

                // 创建一个消息
                String message = "Async-Confirm" + i;

                // 获取一个序列号
                long sequenceNumber = channel.getNextPublishSeqNo();

                // 发送消息
                channel.basicPublish("", ConfirmConst.ASYNC_QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");

                // 发送后，将序列号存入 unconfirmedSet 中
                unconfirmedSet.add(sequenceNumber);
            }
        }
    }

}