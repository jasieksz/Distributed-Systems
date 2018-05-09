package bank.ds.agh;

import currency.ds.agh.Currencies;
import currency.ds.agh.CurrencyServiceGrpc;
import currency.ds.agh.CurrencyType;
import currency.ds.agh.ExchangeRate;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import rx.Observable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static currency.ds.agh.Utils.NOTIFY_BANKS_INTERVAL;

public class Bank {
    private final ManagedChannel channel;
    private final CurrencyServiceGrpc.CurrencyServiceStub currencyServiceStub;
    private final ConcurrentMap<CurrencyType, Double> exchangeRates = new ConcurrentHashMap<>();

    public Bank(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                                    .usePlaintext(true)
                                    .build();

        currencyServiceStub = CurrencyServiceGrpc.newStub(channel);

        exchangeRates.put(CurrencyType.PLN, 0.0);
        exchangeRates.put(CurrencyType.USD, 0.0);
    }

    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                channel.shutdown().awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        subscribeToCurrencyService();

        while(true){
            // TODO : tutaj będzie komunikacja z klientem (zarządzanie kontem / kredyty / etc.)
        }
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
