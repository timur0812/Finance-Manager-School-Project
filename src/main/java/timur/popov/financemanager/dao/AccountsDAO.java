package timur.popov.financemanager.dao;

import timur.popov.financemanager.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static timur.popov.financemanager.dao.DBUtil.getConnection;

/**
 * Implements CRUD operation for account.
 */
public class AccountsDAO {

    public static List<Account> getAllAccountsWithUsers() {
        String sql = "SELECT a.AccountID, a.Type, a.Name, COALESCE(GROUP_CONCAT(DISTINCT u.Name), '') AS Users " +
                "FROM Account a " +
                "LEFT JOIN UserAccount ua ON ua.AccountID = a.AccountID " +
                "LEFT JOIN User u ON u.UserID = ua.UserID " +
                "GROUP BY a.AccountID, a.Type, a.Name " +
                "ORDER BY a.Name";

        List<Account> accounts = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                accounts.add(new Account(
                        rs.getInt("AccountID"),
                        rs.getString("Name"),
                        rs.getString("Type"),
                        rs.getString("Users")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accounts;
    }

    public static boolean createAccountWithUsers(String type, String accountName, List<Integer> userIds) {
        if (type == null || accountName == null || accountName.isBlank() || userIds == null || userIds.isEmpty()) {
            return false;
        }

        String insertAccountSql = "INSERT INTO Account (Name, Type) VALUES (?, ?)";
        String insertUserAccountSql = "INSERT INTO UserAccount (UserID, AccountID) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            /*
             * Turning off AutoCommit because we will execute several commands because
             * we don't want changes to be recorded into database until all command are executed without errors
             */
            conn.setAutoCommit(false);

            int accountId;
            //Statement.RETURN_GENERATED_KEYS allows us to get generated AccountID after inserting new account
            try (PreparedStatement accountStmt = conn.prepareStatement(insertAccountSql, Statement.RETURN_GENERATED_KEYS)) {
                accountStmt.setString(1, accountName);
                accountStmt.setString(2, type);
                accountStmt.executeUpdate();

                try (ResultSet generatedKeys = accountStmt.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        // rollback allows us to undo all changes made to database
                        conn.rollback();
                        return false;
                    }
                    //Indexes starts from 1
                    accountId = generatedKeys.getInt(1);
                }
            }

            try (PreparedStatement userAccountStmt = conn.prepareStatement(insertUserAccountSql)) {
                // Goes through all users and replace parameters in query with variable values and add them to batch
                for (Integer userId : userIds) {
                    userAccountStmt.setInt(1, userId);
                    userAccountStmt.setInt(2, accountId);
                    userAccountStmt.addBatch();
                }
                // Basically execute several sql commands at once
                userAccountStmt.executeBatch();
            }
            //apply all changes to database
            conn.commit();
            return true;
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
                    // Because there is only one instance of connection to database we have to return AutoCommit to true.
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<Integer> getUserIdsForAccount(int accountId) {
        List<Integer> userIds = new ArrayList<>();
            String sql = "SELECT UserID FROM UserAccount WHERE AccountID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt("UserID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return userIds;
    }

    public static boolean updateAccountWithUsers(int accountId, String type, String accountName, List<Integer> userIds) {
        if (accountName == null || accountName.isBlank() || type == null || type.isBlank() || userIds == null || userIds.isEmpty()) {
            return false;
        }

        String updateAccountSql = "UPDATE Account SET Name = ?, Type = ? WHERE AccountID = ?";
        String deleteMappingsSql = "DELETE FROM UserAccount WHERE AccountID = ?";
        String insertMappingSql = "INSERT INTO UserAccount (UserID, AccountID) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            // Same idea as in createAccountWithUsers() method
            conn.setAutoCommit(false);

            try (PreparedStatement updateStmt = conn.prepareStatement(updateAccountSql)) {
                updateStmt.setString(1, accountName);
                updateStmt.setString(2, type);
                updateStmt.setInt(3, accountId);
                updateStmt.executeUpdate();
            }

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteMappingsSql)) {
                deleteStmt.setInt(1, accountId);
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertMappingSql)) {
                for (Integer userId : userIds) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, accountId);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            conn.commit();
            return true;
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

    public static boolean deleteAccount(int accountId) {
        String deleteTransactionsSql = "DELETE FROM Transactions WHERE AccountID = ?";
        String deleteMappingsSql = "DELETE FROM UserAccount WHERE AccountID = ?";
        String deleteAccountSql = "DELETE FROM Account WHERE AccountID = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(deleteTransactionsSql)) {
                pstmt.setInt(1, accountId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(deleteMappingsSql)) {
                pstmt.setInt(1, accountId);
                pstmt.executeUpdate();
            }

            int affected;
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAccountSql)) {
                pstmt.setInt(1, accountId);
                affected = pstmt.executeUpdate();
            }

            conn.commit();
            return affected > 0;
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
