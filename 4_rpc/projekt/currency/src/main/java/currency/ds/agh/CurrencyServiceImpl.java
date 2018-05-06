package currency.ds.agh;

import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;

import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.io.IOException;
import java.net.URL;

public class CurrencyServiceImpl extends CurrencyServiceGrpc.CurrencyServiceImplBase {

    private ConcurrentMap<CurrencyType, Double> exchangeRates = new ConcurrentHashMap<>();

    public CurrencyServiceImpl() {

    }

    @Override
    public StreamObserver<Currency> getExchangeRates(StreamObserver<ExchangeRate> responseObserver) {
        return super.getExchangeRates(responseObserver);
    }

    private void updateExchangeRates() throws IOException {
        Map<String, Double> currentRates = getExchangeRatesFromURL();
        for (CurrencyType currencyType : CurrencyType.values()) {
            exchangeRates.put(currencyType, currentRates.get(currencyType.toString()));
        }
    }

    private Map<String, Double> getExchangeRatesFromURL() {
        class ExchangeRatesAdapter {
            private boolean success;
            private Integer timestamp;
            private String base;
            private String date;
            private Map<String, Double> rates;
        }

        try {
            URL url = new URL("http://data.fixer.io/api/latest?access_key=bf788ce1c664cbb37a888d8fb3465c73"); // api key xD
            InputStreamReader reader = new InputStreamReader(url.openStream());
            ExchangeRatesAdapter ratesAdapter = new Gson().fromJson(reader, ExchangeRatesAdapter.class);
            return ratesAdapter.rates;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
