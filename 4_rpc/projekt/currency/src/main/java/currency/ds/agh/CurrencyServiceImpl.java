package currency.ds.agh;

import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import rx.Observable;

public class CurrencyServiceImpl extends CurrencyServiceGrpc.CurrencyServiceImplBase {

    private ConcurrentMap<CurrencyType, Double> exchangeRates = new ConcurrentHashMap<>();
    private ConcurrentMap<CurrencyType, Set<StreamObserver<ExchangeRate>>> currencyBanksMap = new ConcurrentHashMap<>();

    public CurrencyServiceImpl() {
        updateExchangeRates();
        Observable.interval(Utils.UPDATE_RATES_INTERVAL, TimeUnit.SECONDS).subscribe(tick -> updateExchangeRates());
        Observable.interval(Utils.SIMULATE_RATES_INTERVAL, TimeUnit.SECONDS).subscribe(tick -> simulateExchangeRates());
        Observable.interval(Utils.NOTIFY_BANKS_INTERVAL, TimeUnit.SECONDS).subscribe(tick -> notifyBanks());
    }

    @Override
    public void getExchangeRates(Currencies request, StreamObserver<ExchangeRate> responseObserver) {
        request.getCurrencyList()
                .forEach(cT -> currencyBanksMap.get(cT).add(responseObserver));
    }

    public void notifyBanks() {
        System.out.println("Notifying banks" + exchangeRates);
        currencyBanksMap.entrySet()
                .stream()
                .forEach(e -> e.getValue()
                        .forEach(bank -> bank.onNext(ExchangeRate.newBuilder()
                                .setCurrency(e.getKey())
                                .setRate(exchangeRates.get(e.getKey()))
                                .build())));
    }


    private void updateExchangeRates() {
        Map<String, Double> currentRates = getExchangeRatesFromURL();
        Arrays.stream(CurrencyType.values())
                .filter(currencyType -> !(currencyType.equals(CurrencyType.EUR) || currencyType.equals(CurrencyType.UNRECOGNIZED)))
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
            URL url = new URL("http://data.fixer.io/api/latest?access_key=bf788ce1c664cbb37a888d8fb3465c73"); // api key xD
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

}
