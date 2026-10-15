package timur.popov.financemanager.model;

import java.util.List;

/**
 * Basic user class used in Model and Controller
 */
public class User {
    private final int userId;
    private final String name;
    private final String registrationDate;
    private final String hashedPassword;

    public User(int userId, String name, String registrationDate, String hashedPassword, List<Account> ignoredAccountList) {
        this.userId = userId;
        this.name = name;
        this.registrationDate = registrationDate;
        this.hashedPassword = hashedPassword;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public String getPassword() {
        return hashedPassword;
    }
}
