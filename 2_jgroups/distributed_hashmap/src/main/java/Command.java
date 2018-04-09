import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Command {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    DistributedStringMap map;

    public Command() throws Exception {
        map = new DistributedStringMap();
    }

    public void runCommandLine() throws IOException {
        while (true) {
            System.out.print("> ");
            String command = br.readLine();
            String lowerCommand = command.replaceAll("\\s+","").toLowerCase();
            if (lowerCommand.startsWith("p")) {
                this.handlePutOperation(command);
            } else if (lowerCommand.startsWith("g")) {
                this.handleReadOperation(command);
            } else if (lowerCommand.startsWith("r")) {
                this.handleRemoveOperation(command);
            }
        }
    }

    private void handleRemoveOperation(String command) throws IOException {
        final String[] splittedCommand = command.split(" ");
        String key = getKey(splittedCommand);
        final String value = map.remove(key);
        if(value != null) {
            System.out.println("Removed value: " + value);
        } else {
            System.out.println("No such value!");
        }
    }

    private void handleReadOperation(String command) throws IOException {
        final String[] splittedCommand = command.split(" ");
        String key = getKey(splittedCommand);
        final String value = map.get(key);
        System.out.println(value != null ? value : "No such entry in the map!");
    }

    private String getKey(String[] splittedCommand) throws IOException {
        String key;
        if(splittedCommand.length < 2){
            System.out.print("Enter key: ");
            key = br.readLine();
        } else {
            key = splittedCommand[1];
        }
        return key;
    }

    private void handlePutOperation(String command) throws IOException {
        final String[] splittedCommand = command.split(" ");
        String key, value;
        if(splittedCommand.length < 3){
            System.out.print("Enter key: ");
            key = br.readLine();
            System.out.print("Enter Value: ");
            value = br.readLine();
        } else {
            key = splittedCommand[1];
            value = splittedCommand[2];
        }
        this.map.put(key, value);
    }
}