import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Doctor {
    private final static String QUEUE_NAME = "qDoctor";
    private final static String EXCHANGE_NAME = "e1";
    private final static String EXCHANGE_NAME2 = "e2";
    private static boolean runFlag = true;
    private final static String corrId = UUID.randomUUID().toString();

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("DOCTOR");

        BasicProperties props = new BasicProperties
                .Builder()
                .replyTo(corrId)
                .build();

        // CHANNEL
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //EXCHANGE
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        channel.exchangeDeclare(EXCHANGE_NAME2, BuiltinExchangeType.TOPIC);

        // QUEUE
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME2, corrId);

        // CONSUMER
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received : " + message);
            }
        };

        while (runFlag){
            // receive
            channel.basicConsume(QUEUE_NAME, true, consumer);
            // read new
            System.out.println("Enter injury type and patient name [knee.smith] or exit");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String message = br.readLine();

            // exit program
            if (message.contains("exit")){
                runFlag = false;
                break;
            }
            // send
            channel.basicPublish(EXCHANGE_NAME, message, props, message.getBytes("UTF-8"));
            System.out.println("Sent : " + message);

        }
        // close
        channel.close();
        connection.close();
    }
}
