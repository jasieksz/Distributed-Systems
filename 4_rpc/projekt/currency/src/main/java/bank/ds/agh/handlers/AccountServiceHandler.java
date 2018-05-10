package bank.ds.agh.handlers;

import bank.ds.agh.Account;
import bank.ds.agh.AccountDetails;
import bank.ds.agh.AccountService;
import bank.ds.agh.InvalidOperationException;
import org.apache.thrift.TException;

import java.util.concurrent.ConcurrentMap;

import static bank.ds.agh.Predicates.isPremium;

public class AccountServiceHandler implements AccountService.Iface {

    private ConcurrentMap<String, Account> accounts;

    public AccountServiceHandler(ConcurrentMap<String, Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public AccountDetails getAccountDetails(String pesel) throws InvalidOperationException {
        if (!accounts.containsKey(pesel)){
            throw new InvalidOperationException("No such account");
        }
        Account account = accounts.get(pesel);
        return new AccountDetails(account.pesel, isPremium().test(account), account.income, account.baseCurrency);
    }
}
