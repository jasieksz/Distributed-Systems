package chat;

public class ChatServerApp {
    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(1024, 1234);
        chatServer.run();
    }
}
