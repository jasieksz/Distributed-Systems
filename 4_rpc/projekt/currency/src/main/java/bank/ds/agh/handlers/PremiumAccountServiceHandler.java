package bank.ds.agh.handlers;

import bank.ds.agh.*;
import bank.ds.agh.server.Bank;
import currency.ds.agh.CurrencyType;
import org.apache.thrift.TException;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static bank.ds.agh.Predicates.isPremium;

public class PremiumAccountServiceHandler implements PremiumAccountService.Iface {

    private ConcurrentMap<String, Account> accounts;
    private ConcurrentMap<CurrencyType, Double> exchangeRates;

    public PremiumAccountServiceHandler(ConcurrentMap<String, Account> accounts, ConcurrentMap<CurrencyType, Double> exchangeRates) {
        this.accounts = accounts;
        this.exchangeRates = exchangeRates;
    }

    @Override
    public CreditCosts getCreditCosts(String pesel, CreditParameters creditParameters) throws InvalidOperationException {
        if (!accounts.containsKey(pesel)) {
            throw new InvalidOperationException("No such account");
        }
        Account account = accounts.get(pesel);

        if (!isPremium().test(account)) {
            throw new InvalidOperationException("Account isn't premium");
        }
        /*
         * Client gives monthly amount of credit and duration
         * Client gets total cost
         */
        double exchangeRate = exchangeRates.get(CurrencyType.valueOf(creditParameters.currency));
        double creditCurrencyCost = (creditParameters.cost + creditFee(creditParameters.cost))
                * creditDurationInMonths(creditParameters.start, creditParameters.stop);
        double baseCurrencyCost = creditCurrencyCost / exchangeRate;

        return new CreditCosts(baseCurrencyCost, creditCurrencyCost);
    }

    @Override
    public AccountDetails getAccountDetails(String pesel) throws InvalidOperationException {
        if (!accounts.containsKey(pesel)) {
            throw new InvalidOperationException("No such account");
        }
        Account account = accounts.get(pesel);
        return new AccountDetails(account.pesel, isPremium().test(account), account.income, account.baseCurrency);
    }

    private int creditDurationInMonths(ThriftDate start, ThriftDate stop) {
        return (stop.year - start.year) * 12 + (stop.month - start.month);
    }

    private double creditFee(double totalCost) {
        return totalCost * 0.025;
    }
}
