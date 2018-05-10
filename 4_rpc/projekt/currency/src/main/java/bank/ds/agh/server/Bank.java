package bank.ds.agh.server;

import bank.ds.agh.Account;
import bank.ds.agh.AccountManagement;
import bank.ds.agh.AccountService;
import bank.ds.agh.PremiumAccountService;
import bank.ds.agh.handlers.AccountManagementHandler;
import bank.ds.agh.handlers.AccountServiceHandler;
import bank.ds.agh.handlers.PremiumAccountServiceHandler;
import bank.ds.agh.handlers.ServerHandler;
import currency.ds.agh.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static currency.ds.agh.Utils.PORT;


public class Bank {
    private final ManagedChannel channel;
    private final CurrencyServiceGrpc.CurrencyServiceStub currencyServiceStub;
    private final ConcurrentMap<CurrencyType, Double> exchangeRates = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final int port;

    public Bank(String host, int port, List<CurrencyType> curr) {
        this.port = port;
        channel = ManagedChannelBuilder.forAddress(host, Utils.PORT)
                .usePlaintext(true)
                .build();

        currencyServiceStub = CurrencyServiceGrpc.newStub(channel);
        curr.forEach(currencyType -> exchangeRates.put(currencyType, 0.0));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down CurrencyServiceClient");
            channel.shutdown();

        }));
    }

    public void run() {
        System.out.println("Started Bank : " + port);
        // Get exchangeRates from currenyService
        subscribeToCurrencyService();

        // Handle client operations
        Runnable multiplex = () -> multiplexServerHandler();
        new Thread(multiplex).run();
    }

    /*
    https://github.com/grpc/grpc-java/issues/3095
     */
    private void subscribeToCurrencyService() {

        Currencies request = Currencies.newBuilder()
                .addAllCurrency(exchangeRates.keySet())
                .build();

        StreamObserver<ExchangeRate> streamObserver = new StreamObserver<ExchangeRate>() {
            @Override
            public void onNext(ExchangeRate value) {
                exchangeRates.put(value.getCurrency(), value.getRate());
                System.out.println(value.getCurrency().name() + value.getRate());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Stream completed");
            }
        };

        currencyServiceStub.getExchangeRates(request, streamObserver);
    }

    private void multiplexServerHandler(){
        AccountManagement.Processor<AccountManagementHandler> processor1 = new AccountManagement.Processor<>(new AccountManagementHandler(accounts));
        AccountService.Processor<AccountServiceHandler> processor2 = new AccountService.Processor<>(new AccountServiceHandler(accounts));
        PremiumAccountService.Processor<PremiumAccountServiceHandler> processor3 = new PremiumAccountService.Processor<>(new PremiumAccountServiceHandler(accounts, exchangeRates));

        try {
            TServerTransport serverTransport = new TServerSocket(port);
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();

            TMultiplexedProcessor multiplex = new TMultiplexedProcessor();
            multiplex.registerProcessor("manager", processor1);
            multiplex.registerProcessor("standard", processor2);
            multiplex.registerProcessor("premium", processor3);


            TServer server = new TSimpleServer(new TServer.Args(serverTransport).protocolFactory(protocolFactory).processor(multiplex));

            System.out.println("Starting the multiplex server...");
            server.serve();

        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }
}
