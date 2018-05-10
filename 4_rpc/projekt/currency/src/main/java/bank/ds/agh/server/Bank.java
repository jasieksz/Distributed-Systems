package bank.ds.agh.server;

import bank.ds.agh.Account;
import bank.ds.agh.AccountManagement;
import bank.ds.agh.AccountService;
import bank.ds.agh.PremiumAccountService;
import bank.ds.agh.handlers.AccountManagementHandler;
import bank.ds.agh.handlers.AccountServiceHandler;
import bank.ds.agh.handlers.PremiumAccountServiceHandler;
import bank.ds.agh.handlers.ServerHandler;
import currency.ds.agh.Currencies;
import currency.ds.agh.CurrencyServiceGrpc;
import currency.ds.agh.CurrencyType;
import currency.ds.agh.ExchangeRate;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static currency.ds.agh.Utils.PORT;


public class Bank {
    private final ManagedChannel channel;
    private final CurrencyServiceGrpc.CurrencyServiceStub currencyServiceStub;
    private final ConcurrentMap<CurrencyType, Double> exchangeRates = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();

    public Bank(String host, int port, List<CurrencyType> curr) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();

        currencyServiceStub = CurrencyServiceGrpc.newStub(channel);

        curr.forEach(currencyType -> exchangeRates.put(currencyType, 0.0));
    }

    public void run() {
        // Get exchangeRates from currenyService
        subscribeToCurrencyService();

        // Handle client operations
        AccountManagement.Processor<AccountManagementHandler> processor1 = new AccountManagement.Processor<>(new AccountManagementHandler(accounts));
        AccountService.Processor<AccountServiceHandler> processor2 = new AccountService.Processor<>(new AccountServiceHandler(accounts));
        PremiumAccountService.Processor<PremiumAccountServiceHandler> processor3 = new PremiumAccountService.Processor<>(new PremiumAccountServiceHandler(accounts, exchangeRates));
        new Thread(new ServerHandler(processor1, PORT + 1)).run();
        new Thread(new ServerHandler(processor2, PORT + 2)).run();
        new Thread(new ServerHandler(processor3, PORT + 3)).run();
    }

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
}
