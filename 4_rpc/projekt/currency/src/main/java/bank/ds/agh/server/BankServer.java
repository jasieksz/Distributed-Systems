package bank.ds.agh.server;

import currency.ds.agh.CurrencyType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static currency.ds.agh.Utils.PORT;


public class BankServer {
    public static void main(String[] args) {
        final int port = Integer.parseInt(args[0]);
        List<CurrencyType> currencies = Arrays.stream(args)
                .filter(a -> !a.matches(".*\\d+.*"))
                .map(CurrencyType::valueOf)
                .collect(Collectors.toList());
        Bank bank = new Bank("localhost", port, currencies);
        bank.run();

    }
}
