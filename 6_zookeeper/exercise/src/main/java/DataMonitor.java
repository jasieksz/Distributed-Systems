import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataMonitor implements Watcher {

    private final static String NODE = "/znode_testowy";

    private ZooKeeper zooKeeper;
    public boolean dead;
    private DataMonitorListener listener;
    private List<String> children = new ArrayList<>();

    public DataMonitor(ZooKeeper zk, DataMonitorListener listener) {
        this.zooKeeper = zk;
        this.listener = listener;
    }

    /**
     * Other classes use the DataMonitor by implementing this method
     * exists ==> create | delete | change
     */
    public interface DataMonitorListener {
        void create(String path);

        void delete(String path);

        void change(int size);

        void closing(int reasonCode);
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println(event);
        String path = event.getPath();


        if (event.getType() == Event.EventType.None) {
            if (event.getState() == Event.KeeperState.Expired) {
                dead = true;
                listener.closing(0);
            }
        } else if (event.getType() == Event.EventType.NodeCreated) {
            System.out.println("Created new node");
            if (path.equals(NODE)) {
                try {
                    zooKeeper.getChildren(NODE, this);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
                listener.create(path);
            }
        } else if (event.getType() == Event.EventType.NodeDeleted) {
            System.out.println("Deleted node");
            if (path.equals(NODE)) {
                listener.delete(path);
            } else if (path.contains(NODE)) {
                children.remove(path);
            }
        } else if (event.getType() == Event.EventType.NodeChildrenChanged) {
            if (path.contains(NODE)) {
                try {
                    recursiveAdd(path);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
                listener.change(children.size());
            }
        }
        // SUBSCRIBE
        try {
            zooKeeper.exists(NODE, this);
            for (String child : children) {
                if (zooKeeper.exists(child, this) != null) {
                    zooKeeper.getChildren(child, this);
                }
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void recursiveAdd(String path) throws KeeperException, InterruptedException {
        if(!children.contains(path)){
//            System.out.println("REC PATH ADD " + path);
            children.add(path);
        }
        if (zooKeeper.exists(path, false) != null) {
            for (String child : zooKeeper.getChildren(path, false)){
                recursiveAdd(path + "/" + child);
            }
        }
    }
}



/*

    public void process(WatchedEvent event) {
        String path = event.getPath();
        if (event.getType() == Event.EventType.None) {
            // We are are being told that the state of the
            // connection has changed
            switch (event.getState()) {
                case SyncConnected:
                    // In this particular example we don't need to do anything
                    // here - watches are automatically re-registered with
                    // server and any watches triggered while the client was
                    // disconnected will be delivered (in order of course)
                    break;
                case Expired:
                    // It's all over
                    dead = true;
                    listener.closing(KeeperException.Code.SessionExpired);
                    break;
            }
        } else {
            if (path != null && path.equals(znode)) {
                // Something has changed on the node, let's find out
                zk.exists(znode, true, this, null);
            }
        }
        if (chainedWatcher != null) {
            chainedWatcher.process(event);
        }
    }

    public void processResult(int rc, String path, Object ctx, Stat stat) {
        boolean exists;
        switch (rc) {
            case KeeperException.Code.Ok:
                exists = true;
                break;
            case KeeperException.Code.NoNode:
                exists = false;
                break;
            case KeeperException.Code.SessionExpired:
            case KeeperException.Code.NoAuth:
                dead = true;
                listener.closing(rc);
                return;
            default:
                // Retry errors
                zk.exists(znode, true, this, null);
                return;
        }

        byte b[] = null;
        if (exists) {
            try {
                b = zk.getData(znode, false, null);
            } catch (KeeperException e) {
                // We don't need to worry about recovering now. The watch
                // callbacks will kick off any exception handling
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }
        if ((b == null && b != prevData)
                || (b != null && !Arrays.equals(prevData, b))) {
            listener.exists(b);
            prevData = b;
        }
    }
 */