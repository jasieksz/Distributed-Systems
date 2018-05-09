package currency.ds.agh;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

import static currency.ds.agh.Utils.PORT;

public class CurrencyService {

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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down CurrencyServiceServer");
            server.shutdown();
        }));

        server.awaitTermination();
    }

}
