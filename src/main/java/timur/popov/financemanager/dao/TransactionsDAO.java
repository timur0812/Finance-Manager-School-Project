package timur.popov.financemanager.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import timur.popov.financemanager.model.Transaction;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static timur.popov.financemanager.dao.DBUtil.getConnection;

/**
 * DAO for transaction.
 * The whole filtering is here to keep MVC model
 */
public class TransactionsDAO {
    public static ObservableList<Transaction> getTransactions(int limit) {
            ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

        String sql = "SELECT t.TransactionID, c.Type, c.Name AS CategoryName, t.Amount, " +
                "t.CreationDate, u.Name AS UserName, a.Name AS AccountName, t.Description " +
                "FROM Transactions t " +
                "JOIN Category c ON t.CategoryID = c.CategoryID " +
                "JOIN User u ON t.UserID = u.UserID " +
                "JOIN Account a ON t.AccountID = a.AccountID " +
                "ORDER BY t.CreationDate DESC, t.TransactionID DESC " +
                "LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction trans = new Transaction(
                            rs.getInt("TransactionID"),
                            rs.getString("Type"),         
                            rs.getString("CategoryName"), 
                            rs.getDouble("Amount"),       
                            rs.getString("CreationDate"),
                            rs.getString("UserName"),     
                            rs.getString("AccountName"),  
                            rs.getString("Description")   
                    );
                    transactionList.add(trans);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionList;
    }

    public static List<Transaction> searchTransactions(String searchText, String type, String user, String account, String category, LocalDate from, LocalDate to, Double min, Double max) {
        List<Transaction> list = new ArrayList<>();
        // Base query that will be completed with filters
        StringBuilder sql = new StringBuilder(
                "SELECT t.TransactionID, c.Type, c.Name AS CategoryName, t.Amount, " +
                        "t.CreationDate, u.Name AS UserName, a.Name AS AccountName, t.Description " +
                        "FROM Transactions t " +
                        "JOIN Category c ON t.CategoryID = c.CategoryID " +
                        "JOIN User u ON t.UserID = u.UserID " +
                        "JOIN Account a ON t.AccountID = a.AccountID WHERE 1=1 "
        );
        //Using Object type because parameters have different types
        List<Object> params = new ArrayList<>();

        //Add filters to query and save them to params list
        if (searchText != null && !searchText.isEmpty()) {
            sql.append(" AND (t.Description LIKE ? OR c.Name LIKE ? OR u.Name LIKE ?)");
            String likeValue = "%" + searchText + "%"; // % is a wildcard for “any sequence of characters”
            params.add(likeValue);
            params.add(likeValue);
            params.add(likeValue);
        }
        if (type != null) {
            sql.append(" AND c.Type = ?");
            params.add(type);
        }
        if (user != null) {
            sql.append(" AND u.Name = ?");
            params.add(user);
        }
        if (account != null) {
            sql.append(" AND a.Name = ?");
            params.add(account);
        }
        if (category != null) {
            sql.append(" AND c.Name = ?");
            params.add(category);
        }
        if (from != null) {
            sql.append(" AND t.CreationDate >= ?");
            params.add(from.toString());
        }
        if (to != null) {
            sql.append(" AND t.CreationDate <= ?");
            params.add(to.toString());
        }
        if (min != null) {
            sql.append(" AND t.Amount >= ?");
            params.add(min);
        }
        if (max != null) {
            sql.append(" AND t.Amount <= ?");
            params.add(max);
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            // Parameters list save the order of added parameters.
            for (int i = 0; i < params.size(); i++) {
                // i + 1 because in list indexes starts from 0, but in pstmt it starts from 1.
                pstmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Transaction trans = new Transaction(
                        rs.getInt("TransactionID"),
                        rs.getString("Type"),
                        rs.getString("CategoryName"),
                        rs.getDouble("Amount"),
                        rs.getString("CreationDate"),
                        rs.getString("UserName"),
                        rs.getString("AccountName"),
                        rs.getString("Description")
                );
                list.add(trans);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Used in TransactionController when filter options are loaded(e.g. user names, category names and account names)
    public static List<String> getColumnData(String tableName, String columnName) {
        List<String> options = new ArrayList<>();
        String sql = "SELECT " + columnName + " FROM " + tableName;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                options.add(rs.getString(columnName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return options;
    }
    // Used in QuickActionController when user create new transaction and need to choose account
    public static List<String> getAccountsForUser(int userId) {
        List<String> accounts = new ArrayList<>();
        String sql = "SELECT a.Name " +
                "FROM Account a " +
                "JOIN UserAccount ua ON ua.AccountID = a.AccountID " +
                "WHERE ua.UserID = ? " +
                "ORDER BY a.Name";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(rs.getString("Name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accounts;
    }
    // Used in QuickActionController when user create new transaction and need to choose category
    public static List<String> getCategoriesByType(String type) {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT Name FROM Category WHERE Type = ? ORDER BY Name";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(rs.getString("Name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public static boolean createTransaction(int userId, String accountName, String categoryName, String type, double amount, String description) {
        String accountIdSql = "SELECT a.AccountID " +
                "FROM Account a " +
                "JOIN UserAccount ua ON ua.AccountID = a.AccountID " +
                "WHERE a.Name = ? AND ua.UserID = ?";
        String categoryIdSql = "SELECT CategoryID FROM Category WHERE Name = ? AND Type = ?";
        String insertSql = "INSERT INTO Transactions (Amount, CreationDate, Description, AccountID, UserID, CategoryID) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            Integer accountId = null;
            try (PreparedStatement accountStmt = conn.prepareStatement(accountIdSql)) {
                accountStmt.setString(1, accountName);
                accountStmt.setInt(2, userId);
                try (ResultSet accountRs = accountStmt.executeQuery()) {
                    if (accountRs.next()) {
                        accountId = accountRs.getInt("AccountID");
                    }
                }
            }

            Integer categoryId = null;
            try (PreparedStatement categoryStmt = conn.prepareStatement(categoryIdSql)) {
                categoryStmt.setString(1, categoryName);
                categoryStmt.setString(2, type);
                try (ResultSet categoryRs = categoryStmt.executeQuery()) {
                    if (categoryRs.next()) {
                        categoryId = categoryRs.getInt("CategoryID");
                    }
                }
            }

            if (accountId == null || categoryId == null) {
                return false;
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setDouble(1, amount);

                insertStmt.setString(2, LocalDate.now().toString());
                insertStmt.setString(3, description);
                insertStmt.setInt(4, accountId);
                insertStmt.setInt(5, userId);
                insertStmt.setInt(6, categoryId);
                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateTransactionBasic(int transactionId, double amount, String description) {
        String sql = "UPDATE Transactions SET Amount = ?, Description = ? WHERE TransactionID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, description);
            pstmt.setInt(3, transactionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteTransaction(int transactionId) {
        String sql = "DELETE FROM Transactions WHERE TransactionID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}