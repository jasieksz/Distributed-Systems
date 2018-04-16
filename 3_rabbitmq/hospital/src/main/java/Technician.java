import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Technician {
    private final static String EXCHANGE_NAME = "e1";
    private final static String EXCHANGE_NAME2 = "e2";
    private static boolean runFlag = true;
    private final static String corrId = UUID.randomUUID().toString();

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("TECHNICIAN\nSpecializations :");
        Arrays.stream(args).forEach(System.out::println);


        // CHANNEL
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //EXCHANGE
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        channel.exchangeDeclare(EXCHANGE_NAME2, BuiltinExchangeType.TOPIC);

        // QUEUE
        String QUEUE_NAME_1 = args[0];
        String QUEUE_NAME_2 = args[1];

        channel.queueDeclare(QUEUE_NAME_1, false, false, false, null);
        channel.queueBind(QUEUE_NAME_1, EXCHANGE_NAME, QUEUE_NAME_1 + ".*");

        channel.queueDeclare(QUEUE_NAME_2, false, false, false, null);
        channel.queueBind(QUEUE_NAME_2, EXCHANGE_NAME, QUEUE_NAME_2 + ".*");

        // CONSUMER
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received : " + message);

                message = message + " done";
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                channel.basicPublish(EXCHANGE_NAME2, properties.getReplyTo(), null, message.getBytes("UTF-8"));
                System.out.println("Sent : " + message);
            }
        };

        System.out.println("Waiting for messages");
        channel.basicConsume(QUEUE_NAME_1, true, consumer);
        channel.basicConsume(QUEUE_NAME_2, true, consumer);

//        channel.close();
//        connection.close();
    }
}
