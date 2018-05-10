namespace java bank.ds.agh

typedef string PESEL
typedef string Currency

struct Account {
    1: PESEL pesel,
    2: string firstname,
    3: string lastname,
    4: double income,
    5: string baseCurrency,
}

struct AccountDetails {
    1: PESEL pesel,
    2: bool isPremium,
    3: double income,
    4: Currency currency,
}

struct CreditCosts {
    1: double baseCurrencyCost,
    2: double creditCurrencyCost,
}

struct ThriftDate {
  1: i32 year,
  2: i32 month,
}

struct CreditParameters {
    1: Currency currency,
    2: double cost,
    3: ThriftDate start,
    4: ThriftDate stop,
}

exception InvalidOperationException {
    1: string msg
}


service AccountManagement {
    AccountDetails createAccount(1: Account account) throws(1: InvalidOperationException invalidOperationException),
}

service AccountService {
    AccountDetails getAccountDetails(1: PESEL pesel) throws(1: InvalidOperationException invalidOperationException),
}

service PremiumAccountService extends AccountService {
    CreditCosts getCreditCosts(1: PESEL pesel, 2: CreditParameters creditParameters) throws (
        1: InvalidOperationException invalidOperationException,
    ),
}