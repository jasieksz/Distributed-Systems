package currency.ds.agh;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class CurrencyService {
    private final int PORT = 12345;
    private Server server;

    public CurrencyService() {
        init();
    }

    private void init() {
        server = ServerBuilder
                .forPort(PORT)
                .addService(new CurrencyServiceImpl())
                .build();
    }

    public void run() throws IOException, InterruptedException {
        server.start();
        System.out.println("Starting CurrencyServiceServer");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down CurrencyServiceServer");
                server.shutdown();
            }
        });

        server.awaitTermination();
    }

}
