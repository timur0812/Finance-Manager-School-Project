package timur.popov.financemanager.dao;

import timur.popov.financemanager.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static timur.popov.financemanager.dao.DBUtil.getConnection;

/**
 * DAO that handles CRUD operation for user.
 */
public class UserDAO {

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT UserID, Name, Password, RegistrationDate FROM User ORDER BY Name";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Retrieve all users from database
            while (rs.next()) {
                User user = new User(
                        rs.getInt("UserID"),
                        rs.getString("Name"),
                        rs.getString("RegistrationDate"),
                        rs.getString("Password"),
                        null
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static boolean updateUserName(int userId, String newName) {
        String sql = "UPDATE User SET Name = ? WHERE UserID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createUser(String name, String hashedPassword) {
        // We don't need to turn off AutoCommit because there is only one query that will change database
        String nextIdSql = "SELECT COALESCE(MAX(UserID), 0) + 1 FROM User";
        String insertSql = "INSERT INTO User (UserID, Name, Password, RegistrationDate) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             Statement idStmt = conn.createStatement();
             ResultSet rs = idStmt.executeQuery(nextIdSql)) {

            int nextId = 1;
            if (rs.next()) {
                nextId = rs.getInt(1);
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, nextId);
                pstmt.setString(2, name);
                pstmt.setString(3, hashedPassword);
                pstmt.setString(4, LocalDate.now().toString());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteUser(int userId) {
        Connection conn = null;

        try {
            conn = getConnection();
            /*
             * Turning off AutoCommit because we will execute several commands because
             * we don't want changes to be recorded into database until all command are executed without errors
             */
            conn.setAutoCommit(false);

            List<Integer> linkedAccountIds = new ArrayList<>();
            try (PreparedStatement selectAccounts = conn.prepareStatement(
                    "SELECT AccountID FROM UserAccount WHERE UserID = ?")) {
                selectAccounts.setInt(1, userId);
                try (ResultSet rs = selectAccounts.executeQuery()) {
                    while (rs.next()) {
                        //getting all accountsID with user that we want to delete
                        linkedAccountIds.add(rs.getInt("AccountID"));
                    }
                }
            }

            try (PreparedStatement deleteUserTransactions = conn.prepareStatement(
                    "DELETE FROM Transactions WHERE UserID = ?")) {
                deleteUserTransactions.setInt(1, userId);
                deleteUserTransactions.executeUpdate();
            }

            try (PreparedStatement deleteUserMappings = conn.prepareStatement(
                    "DELETE FROM UserAccount WHERE UserID = ?")) {
                deleteUserMappings.setInt(1, userId);
                deleteUserMappings.executeUpdate();
            }

            try (PreparedStatement countMappings = conn.prepareStatement(
                    "SELECT COUNT(*) FROM UserAccount WHERE AccountID = ?");
                 PreparedStatement deleteAccountTransactions = conn.prepareStatement(
                         "DELETE FROM Transactions WHERE AccountID = ?");
                 PreparedStatement deleteAccount = conn.prepareStatement(
                         "DELETE FROM Account WHERE AccountID = ?")) {

                for (Integer accountId : linkedAccountIds) {
                    countMappings.setInt(1, accountId);
                    try (ResultSet rs = countMappings.executeQuery()) {
                        //checking if there is any records with accounts from linkedAccountIds
                        if (rs.next() && rs.getInt(1) == 0) {
                            //If no, we delete those accounts because they have no linked user anymore
                            deleteAccountTransactions.setInt(1, accountId);
                            deleteAccountTransactions.executeUpdate();

                            deleteAccount.setInt(1, accountId);
                            deleteAccount.executeUpdate();
                        }
                    }
                }
            }

            try (PreparedStatement deleteUser = conn.prepareStatement(
                    "DELETE FROM User WHERE UserID = ?")) {
                deleteUser.setInt(1, userId);
                boolean deleted = deleteUser.executeUpdate() > 0;

                conn.commit();
                return deleted;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
