import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import java.io.*;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DistributedStringMap extends ReceiverAdapter implements SimpleStringMap {

    private ProtocolStack protocolStack;
    private JChannel jChannel;
    private static final String CHANNEL_NAME = "DHM";
    private static final String MULTICAST_ADDRESS = "230.0.0.27";

    private final Map<String, String> stateMap = new ConcurrentHashMap<>();

    public DistributedStringMap(){

        try {
            initJGroupsConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * JGROUPS Initialization
     */

    private void initJGroupsConnection() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        jChannel = new JChannel(false);

        initProtocolStack();
        jChannel.setReceiver(this);
        jChannel.connect(CHANNEL_NAME);
        jChannel.getState(null, 10000);
    }

    private void initProtocolStack() throws Exception {
        protocolStack = new ProtocolStack();
        jChannel.setProtocolStack(protocolStack);
        protocolStack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName(MULTICAST_ADDRESS)))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new STATE_TRANSFER())
                .addProtocol(new FRAG2());
//                .addProtocol(new SEQUENCER())
//                .addProtocol(new FLUSH());

        protocolStack.init();
    }

    /*
     *  SimpleStringMap Interface Implementation
     */

    @Override
    public boolean containsKey(String key) {
        return stateMap.containsKey(key);
    }

    @Override
    public String get(String key) {
        return stateMap.get(key);
    }

    @Override
    public String put(String key, String value) {
        String putElement = stateMap.get(key);
        propagateMethod(new MapElement(key, value, MethodType.PUT));
        return putElement;
    }

    @Override
    public String remove(String key) {
        if (stateMap.containsKey(key)) {
            String removedElement = stateMap.get(key);
            propagateMethod(new MapElement(key, MethodType.REMOVE));
            return removedElement;
        }
        return null;
    }

    /*
     * Helper methods
     */

    private void propagateMethod(MapElement mapElement) {
        try {
            jChannel.send(new Message(null, null, mapElement));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * ReceiverAdapter Implementation
     */

    @Override
    public void viewAccepted(View view) {
        if (view instanceof MergeView) {
            ViewHandler viewHandler = new ViewHandler(jChannel, (MergeView) view);
            viewHandler.start();
        }
        System.out.println("View : " + view);
    }

    @Override
    public synchronized void receive(Message message) {
        System.out.println("Received : " + message + " " + message.getObject().toString());
        MapElement element = (MapElement) message.getObject();
        if (element.getMethodType().equals(MethodType.PUT)) {
            stateMap.put(element.getKey(), element.getValue());
        } else {
            stateMap.remove(element.getKey());
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (stateMap) {
            Util.objectToStream(stateMap, new DataOutputStream(output));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setState(InputStream input) throws Exception {
        Map<String, String> map;
        map = (Map<String, String>) Util.objectFromStream(new DataInputStream(input));
        synchronized (stateMap) {
            stateMap.clear();
            stateMap.putAll(map);
        }
        System.out.println(map.size() + " elements in map history):");
        map.values().forEach(System.out::println);
    }
}

class MapElement implements Serializable {

    private final String key;
    private final String value;
    private final MethodType methodType;

    public MapElement(String key, MethodType methodType) {
        this(key, null, methodType);
    }

    public MapElement(String key, String value, MethodType methodType) {
        this.key = key;
        this.value = value;
        this.methodType = methodType;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public MethodType getMethodType() {
        return methodType;
    }
}


