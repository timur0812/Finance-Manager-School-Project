package timur.popov.financemanager;

import timur.popov.financemanager.model.User;

/**
 * Keeps information about current user
 * Implements Singleton pattern
 * Used by controllers to define current user or to change current user
 */
public final class UserSession {
    private static final UserSession INSTANCE = new UserSession();

    private User currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        return INSTANCE;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }

    public void clear() {
        currentUser = null;
    }
}

