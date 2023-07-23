package com.yt.ytbibackend.mq;

import com.rabbitmq.client.*;

public class FanoutConsumer {
    private static final String EXCHANGE_NAME = "fanout-exchange";

    public static void main(String[] argv) throws Exception {
        // 1. 消费者和生产者要绑定同一个交换机
        // 2. 要先有队列才能绑定
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();
        // 声明交换机
        channel1.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        //channel2.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);


        String queueName1= "queue1";
        channel1.queueDeclare(queueName1, true, false, false, null);
        channel1. queueBind(queueName1, EXCHANGE_NAME, "");

        String queueName2= "queue2";
        channel2.queueDeclare(queueName2, true, false, false, null);
        channel2.queueBind(queueName2, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] [queue1] Received '" + message + "'");
        };

        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] [queue2] Received '" + message + "'");
        };
        channel1.basicConsume(queueName1, true, deliverCallback1, consumerTag -> { });
        channel2.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });

    }
}