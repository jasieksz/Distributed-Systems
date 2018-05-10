package bank.ds.agh.server;

import currency.ds.agh.CurrencyType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static currency.ds.agh.Utils.PORT;


public class BankServer {
    public static void main(String[] args) {
        List<CurrencyType> currencies = Arrays.stream(args).map(CurrencyType::valueOf).collect(Collectors.toList());
        Bank bank = new Bank("localhost", PORT, currencies);
        bank.run();

    }
}
