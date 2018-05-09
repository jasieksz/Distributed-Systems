package bank.ds.agh;

import static currency.ds.agh.Utils.PORT;

public class BankServer {
    public static void main(String[] args) {
        Bank bank = new Bank("localhost", PORT);
        bank.run();

    }
}
