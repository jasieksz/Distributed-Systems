package chat;

public class ChatClientApp {

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient("224.0.0.1", 1234, 2345);
        chatClient.setName(args[0]);
        chatClient.run();
    }

}
