package bank.ds.agh;

public class PremiumAccount extends Account {
    public PremiumAccount(String name, String surname, String pesel, int income) {
        super(name, surname, pesel, income);
    }

    @Override
    public int getIncome() {
        return super.getIncome();
    }

    public void getCredit(){
        return;
    }
}
