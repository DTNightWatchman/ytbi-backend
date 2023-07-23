package com.yt.ytbibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class DlxProducer {

    private static final String DEAD_EXCHANGE_NAME = "dlx-direct-exchange";

    private static final String EXCHANGE_NAME = "direct2-exchange";


    public static void main(String[] argv) throws Exception {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try (Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel()) {
                channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");

                // 创建一个队列，随机分配一个队列名称
                String queueName = "boss_dlx_queue";
                channel.queueDeclare(queueName, true, false, false, null);
                channel.queueBind(queueName, DEAD_EXCHANGE_NAME, "boss");

                String queueName2 = "outher_dlx_queue";
                channel.queueDeclare(queueName2, true, false, false, null);
                channel.queueBind(queueName2, DEAD_EXCHANGE_NAME, "other");



                DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    //拒绝消息
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                    System.out.println(" [ boss_dlx_queue ] Received '" +
                            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                };
                channel.basicConsume(queueName, false, deliverCallback1, consumerTag -> { });


                DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    //拒绝消息
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                    System.out.println(" [ outher_dlx_queue ] Received '" +
                            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                };
                channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> { });



                Scanner scanner = new Scanner(System.in);


                while (scanner.hasNext()) {
                    String userInput = scanner.nextLine();
                    String[] strings = userInput.split(" ");
                    if (strings.length < 1) {
                        continue;
                    }
                    String message = strings[0];
                    String routeKey = strings[1];
                    channel.basicPublish(EXCHANGE_NAME, routeKey, null, message.getBytes(StandardCharsets.UTF_8));
                    System.out.println(" [x] Sent '" + message + " with routing " + routeKey + "'");

                }

            }
        }
        // 模板-自我介绍 -》 课程 -》以上是我的自我介绍
        // java 特性 -> 指定问题
        // hashTable hashMap
        // hashMap 扩容机制
    // 1.8 sych + cas 和 1.7对比
        // 大小堆，
    // 并发加锁（有什么锁），线程池，同步（CountDownLatch，CyclicBarrier，Semaphore）
    // 数据库 事务（加锁，快照），索引（）



}