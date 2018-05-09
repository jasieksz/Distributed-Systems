package currency.ds.agh;

import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;

import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import rx.Observable;

import static currency.ds.agh.Utils.*;

public class CurrencyServiceImpl extends CurrencyServiceGrpc.CurrencyServiceImplBase {

    private ConcurrentMap<CurrencyType, Double> exchangeRates = new ConcurrentHashMap<>();
    private ConcurrentMap<CurrencyType, Set<StreamObserver<ExchangeRate>>> currencyBanksMap = new ConcurrentHashMap<>();

    public CurrencyServiceImpl() {
        initCurrencyBanksMap();
        updateExchangeRates();
        Observable.interval(UPDATE_RATES_INTERVAL, TimeUnit.SECONDS).subscribe(tick -> updateExchangeRates());
        Observable.interval(SIMULATE_RATES_INTERVAL, TimeUnit.SECONDS).subscribe(tick -> simulateExchangeRates());
        Observable.interval(NOTIFY_BANKS_INTERVAL, TimeUnit.SECONDS).subscribe(tick -> notifyBanks());
    }

    @Override
    public void getExchangeRates(Currencies request, StreamObserver<ExchangeRate> responseObserver) {
        request.getCurrencyList()
                .forEach(cT -> currencyBanksMap.get(cT).add(responseObserver));
    }

    public void notifyBanks() {
        System.out.println("Notifying banks " + exchangeRates);
        currencyBanksMap.entrySet()
                .stream()
                .forEach(e -> e.getValue()
                        .forEach(bank -> {
                            bank.onNext(ExchangeRate.newBuilder()
                                                    .setCurrency(e.getKey())
                                                    .setRate(exchangeRates.get(e.getKey()))
                                                    .build());
                        }));
    }


    private void updateExchangeRates() {
        Map<String, Double> currentRates = getExchangeRatesFromURL();
        Arrays.stream(CurrencyType.values())
                .filter(currencyType -> // EUR skipped beacuase it's base currency, UNRECOGNIZED = grpc generated :(
                        !(currencyType.equals(CurrencyType.EUR) || currencyType.equals(CurrencyType.UNRECOGNIZED)))
                .forEach(currencyType -> exchangeRates.put(currencyType, currentRates.get(currencyType.toString())));
    }

    private void simulateExchangeRates() {
        exchangeRates.entrySet()
                .forEach(e -> e.setValue(e.getValue() * ThreadLocalRandom.current().nextDouble(0.985, 1.015)));
    }

    private Map<String, Double> getExchangeRatesFromURL() {
        Map<String, Double> result = new HashMap<>();
        Gson gson = new Gson();

        try {
            URL url = new URL(CURRENCY_API_URL);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            ExchangeRatesAdapter ratesAdapter = gson.fromJson(reader, ExchangeRatesAdapter.class);
            result.putAll(ratesAdapter.rates);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private class ExchangeRatesAdapter {
        boolean success;
        Integer timestamp;
        String base;
        String date;
        Map<String, Double> rates;

        @Override
        public String toString() {
            return "ExchangeRatesAdapter{" +
                    "success=" + success +
                    ", timestamp=" + timestamp +
                    ", base='" + base + '\'' +
                    ", date='" + date + '\'' +
                    ", rates=" + rates +
                    '}';
        }
    }

    private void initCurrencyBanksMap(){
        Arrays.stream(CurrencyType.values())
                .filter(c -> !(c.equals(CurrencyType.EUR) || c.equals(CurrencyType.UNRECOGNIZED)))
                .forEach(c -> currencyBanksMap.put(c, new HashSet<>()));
    }

}
