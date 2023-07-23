package com.yt.ytbibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class TopicConsumer {

  private static final String EXCHANGE_NAME = "topic-exchange";

  public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        String queueName1 = "queue1";
        channel.queueDeclare(queueName1, true, false, false, null);
        channel.queueBind(queueName1, EXCHANGE_NAME, "#.queue1.#");

        String queueName2 = "queue2";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, EXCHANGE_NAME, "#.queue2.#");



        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [ queue1 ] Received '" +
                  delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName1, true, deliverCallback1, consumerTag -> { });


        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [ queue2 ] Received '" +
                  delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
      channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });



      String queueName3 = "queue3";
      channel.queueDeclare(queueName3, true, false, false, null);
      channel.queueBind(queueName3, EXCHANGE_NAME, "#.queue3.#");

      DeliverCallback deliverCallback3 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [ queue3 ] Received '" +
                  delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
      };
      channel.basicConsume(queueName3, true, deliverCallback3, consumerTag -> { });

  }
}