package chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer implements Runnable {

    private final int DGRAM_LENGTH;
    private final Integer portNumber;
    private final ConcurrentHashMap<String, ClientTcpConnectionHandler> clients;
    private final ExecutorService executor;

    ChatServer(int dgram_length, Integer portNumber) {
        DGRAM_LENGTH = dgram_length;
        this.portNumber = portNumber;
        clients = new ConcurrentHashMap<>();
        executor = Executors.newFixedThreadPool(4);
    }


    public void run() {
        System.out.println("SERVER");
        executor.execute(() -> handleUdpTraffic(clients));

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while(true){
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientTcpConnectionHandler(clientSocket, clients)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleUdpTraffic(Map<String, ClientTcpConnectionHandler> clients) {
        try (DatagramSocket udpSocket  = new DatagramSocket(portNumber)){
            byte[] buffer = new byte[DGRAM_LENGTH];
            while (true) {
                Arrays.fill(buffer, (byte)0);
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(receivePacket);
                String msg = new String(receivePacket.getData()).trim();
                String[] parts = msg.split("/");
                System.out.println("received udp msg: " + parts[1]);

                //SEND MSG TO CLIENTS
                for (ClientTcpConnectionHandler c : clients.values()) {
                    c.sendMsgToClient(parts[1], parts[0]);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

