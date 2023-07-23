package com.yt.ytbibackend.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class BiMqInitMain {

    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            String EXCHANGE_NAME = "bi_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            // 创建队列
            String queueName = "bi_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME, "bi_routingKey");
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
