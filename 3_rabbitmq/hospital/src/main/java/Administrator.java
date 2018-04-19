import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Administrator {
    private final static String QUEUE_ADMIN = "admin";
    //    private final static String EXCHANGE_ADMIN = "admin";
    private final static String EXCHANGE_HOSPITAL = "hospital";
    private final static String ADMIN_ID = UUID.randomUUID().toString();
    private static boolean runFlag = true;

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("ADMIN");

        // CHANNEL
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // EXCHANGE
//        channel.exchangeDeclare(EXCHANGE_ADMIN, BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare(EXCHANGE_HOSPITAL, BuiltinExchangeType.TOPIC);


        // RECEIVE ALL MESSAGES
        channel.queueDeclare(QUEUE_ADMIN + ADMIN_ID, false, false, true, null);
        channel.queueBind(QUEUE_ADMIN + ADMIN_ID, EXCHANGE_HOSPITAL, "#.admin.log.#");

        // CONSUMER
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Logged: " + message);
            }
        };

        // PROGRAM
        channel.basicConsume(QUEUE_ADMIN + ADMIN_ID, true, consumer);

        while (runFlag) {
            // read new
            System.out.println("Enter operation type : [info, exit]");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String operation = br.readLine();

            if (operation.contains("exit")) {
                runFlag = false;
                break;
            } else if (operation.contains("info")) {
                System.out.println("Enter message");
                String message = br.readLine();
                channel.basicPublish(EXCHANGE_HOSPITAL, EXCHANGE_HOSPITAL + ".admin.info", null, message.getBytes("UTF-8"));
                System.out.println("Sent: " + message);
            }
        }

        channel.close();
        connection.close();
    }
}
