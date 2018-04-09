public class Main {
    @SuppressWarnings("unchecked")
    public static void main(String[] args){
        try {
            Command command = new Command();
            command.runCommandLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
