package currency.ds.agh;

import java.io.IOException;

public class CurrencyServiceServer {
    public static void main(String[] args) {
        CurrencyService currencyService = new CurrencyService();
        try {
            currencyService.run();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
