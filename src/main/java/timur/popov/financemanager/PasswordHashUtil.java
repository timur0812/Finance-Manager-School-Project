package timur.popov.financemanager;

/**
 * Class implements hash function for registering and logging
 * matches method compares hashed value from db and entered password when user try to log-in
 */
public final class PasswordHashUtil { private PasswordHashUtil() {
    }

    public static String hash(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        long hash = 17;
        for (int i = 0; i < value.length(); i++) {
            hash = hash * 31 + value.charAt(i);
        }

        return String.format("%08x", hash & 0xffffffffL);
    }

    public static boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }

        return hash(rawPassword).equals(storedHash);
    }
}

