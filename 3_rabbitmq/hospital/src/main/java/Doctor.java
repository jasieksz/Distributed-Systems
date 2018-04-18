import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Doctor {
    private final static String QUEUE_EXAMINATION = "doctor";
    private final static String QUEUE_ADMIN = "admin";
    private final static String EXCHANGE_HOSPITAL = "hospital";
    private static boolean runFlag = true;
    private final static String corrId = UUID.randomUUID().toString();

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("DOCTOR " + corrId);

        BasicProperties props = new BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(corrId)
                .build();

        // CHANNEL
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // EXCHANGE
        channel.exchangeDeclare(EXCHANGE_HOSPITAL, BuiltinExchangeType.TOPIC);

        // RECEIVE EXAMINATION RESULTS
        channel.queueDeclare(QUEUE_EXAMINATION, false, false, false, null);
        channel.queueBind(QUEUE_EXAMINATION, EXCHANGE_HOSPITAL, EXCHANGE_HOSPITAL + "." + corrId + ".#");

        // RECEIVE ADMIN MESSAGES
        channel.queueDeclare(QUEUE_ADMIN, false, false, false, null);
        channel.queueBind(QUEUE_ADMIN, EXCHANGE_HOSPITAL, EXCHANGE_HOSPITAL + ".admin.#");

        // CONSUMER
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received from TECH: " + message);
            }
        };

        Consumer consumerAdmin = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received from ADMIN: " + message);
            }
        };



        // PROGRAM
        channel.basicConsume(QUEUE_EXAMINATION, true, consumer);
        channel.basicConsume(QUEUE_ADMIN, true, consumerAdmin);

        while (runFlag) {
            // read new
            System.out.println("Enter injury type or exit");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String injury = br.readLine();

            System.out.println("Enter patient name or exit");
            String patient = br.readLine();

            String message = "[" + injury + " " + patient + "]";

            // exit program
            if (message.contains("exit")) {
                runFlag = false;
                break;
            }
            // send
            if (message.contains("knee") || message.contains("elbow") || message.contains("hip")) {
                channel.basicPublish(EXCHANGE_HOSPITAL, EXCHANGE_HOSPITAL + "." + injury, props, message.getBytes("UTF-8"));
                System.out.println("Sent: " + message);
            } else {
                System.out.println("Invalid injury type");
            }

        }
        // close
        channel.close();
        connection.close();
    }
}
