package com.yt.ytbibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

public class DlxConsumer {
    private static final String DEAD_EXCHANGE_NAME = "dlx-direct-exchange";

    private static final String EXCHANGE_NAME = "direct2-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");


        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", "other");


        String queueName1 = "queue11";
        channel.queueDeclare(queueName1, true, false, false, args);
        channel.queueBind(queueName1, EXCHANGE_NAME, "queue1");


        Map<String, Object> args2 = new HashMap<>();
        args2.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        args2.put("x-dead-letter-routing-key", "boss");

        String queueName2 = "queue22";
        channel.queueDeclare(queueName2, true, false, false, args2);
        channel.queueBind(queueName2, EXCHANGE_NAME, "queue2");


        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            //拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            System.out.println(" [ queue1 ] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName1, false, deliverCallback1, consumerTag -> { });


        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            System.out.println(" [ queue2 ] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> { });


    }
}