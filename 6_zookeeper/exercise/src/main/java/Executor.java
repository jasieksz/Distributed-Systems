import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.*;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class Executor extends Thread implements Watcher, DataMonitor.DataMonitorListener {

    private final static String NODE = "/znode_testowy";
    private final static String HOST_PORT = "127.0.0.1:2181";
    
    private DataMonitor dataMonitor;
    private ZooKeeper zooKeeper;
    private Process child;
    private String exec[];
    private static Printer printer;

    public Executor(String exec[]) throws KeeperException, IOException {
        this.exec = exec;
        zooKeeper = new ZooKeeper(HOST_PORT, 3000, this);
        dataMonitor = new DataMonitor(zooKeeper, this);
        printer = new Printer(zooKeeper);
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("USAGE: Executor program [args ...]");
            System.exit(2);
        }
        String exec[] = new String[args.length];
        System.arraycopy(args, 0, exec, 0, exec.length);
        try {
            new Executor(exec).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Enter command : [tree, quit]");
            try {
                String line = reader.readLine();
                if (line.startsWith("quit")) {
                    System.out.println("ENDING LOOP");
                    break;
                } else if (line.startsWith("tree")) {
                    System.out.println("PRINTING TREE");
                    printer.printTree(NODE, 0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private class Printer {
        private ZooKeeper zooKeeper;
        Printer(ZooKeeper zk){
            this.zooKeeper = zk;
        }

        private void printTree(String node, int level) {
            StringBuilder indent = new StringBuilder();
            for (int i = 0; i < level; i++)
                indent.append("\t");

            System.out.println(indent.toString() + node);
            List<String> children = null;
            try {
               children = zooKeeper.getChildren(node, false);
            } catch (KeeperException ignored) {
                // We can ignore this exception, it only tells us that we reach a leaf
//                System.out.println("Node doesn't have any more children");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (children != null){
                children.forEach(child -> printTree(node + "/" + child, level+1));
            }
        }
    }


    public void run() {
        try {
            synchronized (this) {
                while (!dataMonitor.dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void process(WatchedEvent event) {
        dataMonitor.process(event);
    }

    @Override
    public void create(String path) {
        if (child == null){
            try {
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec);
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void delete(String path) {
        if (child != null) {
            System.out.println("Stopping child");
            child.destroy();
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            child = null;
        }
    }

    @Override
    public void change(int size) {
        System.out.println("Children number changed: " + size);

    }

    @Override
    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }


    static class StreamWriter extends Thread {
        OutputStream os;
        InputStream is;

        StreamWriter(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
            start();
        }

        public void run() {
            byte b[] = new byte[80];
            int rc;
            try {
                while ((rc = is.read(b)) > 0) {
                    os.write(b, 0, rc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}

/*

    public void exists(byte[] data) {
        if (data == null) {
            if (child != null) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException ignored) {
                }
            }
            child = null;
        } else {
            if (child != null) {
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec);
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
 */