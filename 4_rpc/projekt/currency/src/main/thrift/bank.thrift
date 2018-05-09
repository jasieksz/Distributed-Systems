namespace java bank

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

struct CreditParameters {
    1: Currency currency,
    2: double cost,
}

exception AuthorizationException {
    1: string why
}

exception InvalidArgumentException {
    1: string why
}

service AccountManagement {
    AccountDetails createAccount(1: Account account) throws(1: InvalidArgumentException authorizationException),
}

service AccountService {
    AccountDetails getAccountDetails(1: PESEL pesel) throws(1: AuthorizationException authorizationException),
}

service PremiumAccountService extends AccountService {
    CreditCosts getCreditCosts(1: PESEL pesel, 2: CreditParameters creditParameters) throws (
        1: AuthorizationException authorizationException,
        2: InvalidArgumentException invalidArgumentException,
    ),
}