package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ClientTcpConnectionHandler implements Runnable {

    private PrintWriter socketOut;
    private BufferedReader socketIn;
    private Map<String, ClientTcpConnectionHandler> clients;
    private String name;

    ClientTcpConnectionHandler(Socket clientSocket, Map<String, ClientTcpConnectionHandler> clients) throws IOException {
        this.socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
        this.socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            name = socketIn.readLine();
            boolean connectFlag = shouldConnectionBeEstablished();
            String msg = getMessage(connectFlag);
            while (msg != null && connectFlag) {
                System.out.println("received msg: " + msg);
                senMsgToOtherClients(msg);
                msg = socketIn.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(name + " left chat");
            clients.remove(name);
        }
    }

    private String getMessage(boolean connectFlag) throws IOException {
        return connectFlag ? socketIn.readLine() : null;
    }

    private boolean shouldConnectionBeEstablished() {
        if (clients.containsKey(name)) {
            socketOut.println("This name is already taken");
            return false;
        } else {
            clients.put(name, this);
            System.out.println(name + " has connected");
            return true;
        }
    }

    private void senMsgToOtherClients(String msg) {
        for (ClientTcpConnectionHandler c : clients.values()) {
            c.sendMsgToClient(msg, name);
        }
    }

    public void sendMsgToClient(String msg, String sender) {
        if (!sender.equals(this.name)){
            socketOut.println(sender + " : " + msg);
        }
    }

}
