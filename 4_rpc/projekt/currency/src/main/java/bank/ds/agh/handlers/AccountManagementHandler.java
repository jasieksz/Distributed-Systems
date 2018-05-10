package bank.ds.agh.handlers;

import bank.ds.agh.*;
import org.apache.thrift.TException;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static bank.ds.agh.Predicates.isPremium;

public class AccountManagementHandler implements AccountManagement.Iface {

    private ConcurrentMap<String, Account> accounts;

    public AccountManagementHandler(ConcurrentMap<String, Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public AccountDetails createAccount(Account account) throws InvalidOperationException {
        if (accounts.containsKey(account.pesel)){
            throw new InvalidOperationException("Account already exists");
        }
        accounts.put(account.pesel, account);
        return new AccountDetails(account.pesel, isPremium().test(account), account.income, account.baseCurrency);
    }
}
