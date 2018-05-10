package bank.ds.agh.handlers;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;

public class ServerHandler implements Runnable {
    private TProcessor processor;
    private final int port;

    public ServerHandler(TProcessor processor, int port) {
        this.processor = processor;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            TNonblockingServerTransport nonblockserverTransport = new TNonblockingServerSocket(port);
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(nonblockserverTransport).processor(processor));
            System.out.println("Starting the multithread server...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
