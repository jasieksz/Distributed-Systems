package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class ChatClient implements Runnable {

    private final int portNumber;
    private final String hostName;
    private String name;
    private final int multicastPortNumber; //multicast udp args
    private final String multicastAddress;

    public ChatClient(String multicastAddress, int portNumber, int multicastPortNumber) {
        this.multicastAddress = multicastAddress;
        this.portNumber = portNumber;
        this.multicastPortNumber = multicastPortNumber;
        hostName = "localhost";
    }

    public void run() {
        System.out.println("CLIENT");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try (Socket socket = new Socket(hostName, portNumber)) {
            PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOut.println(name);

            //START LISTENER
            ClientTcpListener tcpListener = new ClientTcpListener(socketIn);
            new Thread(tcpListener).start();

            while (true) {
                String msg = reader.readLine();
                if (msg.startsWith("U")) {
                    sendMsgThroughUdp(name + "/" + msg.substring(1));
                } else {
                    socketOut.println(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsgThroughUdp(String msg) {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            InetAddress address = InetAddress.getLocalHost();
            byte[] sendBuffer = msg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, portNumber);
            udpSocket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setName(String name) {
        this.name = name;
    }
}
