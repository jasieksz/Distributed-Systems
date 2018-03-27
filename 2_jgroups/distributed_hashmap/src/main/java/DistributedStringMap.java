import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DistributedStringMap extends ReceiverAdapter implements SimpleStringMap {

    private ProtocolStack protocolStack;
    private JChannel jChannel;
    private static final String CHANNEL_NAME = "DHM";
    private static final String MULTICAST_ADDRESS = "230.0.0.27";

    private Map<String, String> map = new ConcurrentHashMap<>();

    public DistributedStringMap() throws Exception {
        initJGroupsConnection();
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

        protocolStack.init();
    }

    /*
     *  SimpleStringMap Interface Implementation
     */

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public String put(String key, String value) {
        return null;
    }

    @Override
    public String remove(String key) {
        return null;
    }

    /*
     * Receiver Interface Implementation
     */

    @Override
    public void viewAccepted(View view) {

    }

    @Override
    public void receive(Message message) {

    }

    @Override
    public void getState(OutputStream outputStream) throws Exception {

    }

    @Override
    public void setState(InputStream inputStream) throws Exception {

    }


}

class MapMethods implements Serializable {

    private final String key;
    private final String value;
    private final MethodType methodType;

    public MapMethods(String key, MethodType methodType) {
        this(key, null, methodType);
    }

    public MapMethods(String key, String value, MethodType methodType) {
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


