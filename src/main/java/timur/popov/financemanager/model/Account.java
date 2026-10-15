package timur.popov.financemanager.model;

/**
 * Account class keeps information about specific account.
 * users field stores information about all users through comma(e.g Alex, John, Ivan)
 */
public class Account {
    private final int ACCOUNT_ID;
    private String name;
    private String type;
    private String users;


    public Account(int ACCOUNT_ID, String name, String type, String users) {
        this.ACCOUNT_ID = ACCOUNT_ID;
        this.name = name;
        this.type = type;
        this.users = (users == null) ? "" : users;
    }

    public int getACCOUNT_ID() {
        return ACCOUNT_ID;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }


    public String getUsers() {
        return users;
    }
}
