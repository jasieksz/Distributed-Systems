package chat;

import java.io.BufferedReader;
import java.io.IOException;

public class ClientTcpListener implements Runnable {

    private final BufferedReader inSocket;

    public ClientTcpListener(BufferedReader inSocket) {
        this.inSocket = inSocket;
    }

    @Override
    public void run() {
        try {
            String msg = inSocket.readLine();
            while (msg != null) {
                System.out.println(msg);
                msg = inSocket.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
