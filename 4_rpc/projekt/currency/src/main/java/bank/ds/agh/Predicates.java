package bank.ds.agh;

import currency.ds.agh.Utils;

import java.util.function.Predicate;

public class Predicates {
    public static Predicate<Account> isPremium() {
        return account -> account.income > Utils.STANDARD_INCOME_LIMIT;
    }
}
