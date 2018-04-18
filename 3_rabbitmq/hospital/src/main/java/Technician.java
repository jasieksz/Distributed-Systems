import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Technician {
    private final static String EXCHANGE_HOSPITAL = "hospital";
    private final static String QUEUE_ADMIN = "admin";

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.print("TECHNICIAN - Specializations: [" + args[0] + " | " + args[1] + "]\n");

        // CHANNEL
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //EXCHANGE
        channel.exchangeDeclare(EXCHANGE_HOSPITAL, BuiltinExchangeType.TOPIC);

        // RECIVE EXAMINATION ORDERS
        final String QUEUE_SPECIALIZATION_1 = args[0];
        final String QUEUE_SPECIALIZATION_2 = args[1];

        channel.queueDeclare(QUEUE_SPECIALIZATION_1, false, false, false, null);
        channel.queueBind(QUEUE_SPECIALIZATION_1, EXCHANGE_HOSPITAL, EXCHANGE_HOSPITAL + "." + QUEUE_SPECIALIZATION_1 + ".#");

        channel.queueDeclare(QUEUE_SPECIALIZATION_2, false, false, false, null);
        channel.queueBind(QUEUE_SPECIALIZATION_2, EXCHANGE_HOSPITAL, EXCHANGE_HOSPITAL + "." + QUEUE_SPECIALIZATION_2 + ".#");

        channel.queueDeclare(QUEUE_ADMIN, false, false, false, null);
        channel.queueBind(QUEUE_ADMIN, EXCHANGE_HOSPITAL, EXCHANGE_HOSPITAL + ".admin.#");


        // CONSUMER
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received : " + message);
                working(5);
                System.out.println("REPLY TO : " + properties.getReplyTo());
                channel.basicPublish(EXCHANGE_HOSPITAL, EXCHANGE_HOSPITAL + "." + properties.getReplyTo(), null, (message + "[done]").getBytes("UTF-8"));
                System.out.println("Sent : " + (message + "[done]"));
            }
        };

        Consumer consumerAdmin = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received from ADMIN: " + message);
            }
        };

        System.out.println("Waiting for messages");
        channel.basicConsume(QUEUE_SPECIALIZATION_1, true, consumer);
        channel.basicConsume(QUEUE_SPECIALIZATION_2, true, consumer);
        channel.basicConsume(QUEUE_ADMIN, true, consumerAdmin);

//        channel.close();
//        connection.close();
    }

    private static void working(int time){
        try {
            Thread.sleep(time*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
