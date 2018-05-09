package bank.ds.agh;

public class Account {
    private final String name;
    private final String surname;
    private final String pesel;
    private int income;

    public Account(String name, String surname, String pesel, int income) {
        this.name = name;
        this.surname = surname;
        this.pesel = pesel;
        this.income = income;
    }


    public int getIncome() {
        return income;
    }
}
