package timur.popov.financemanager.dao;

import timur.popov.financemanager.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static timur.popov.financemanager.dao.DBUtil.getConnection;

/**
 * DAO for category CRUD operations.
 */
public class CategoriesDAO {

    public static List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT CategoryID, Name, Type FROM Category ORDER BY Type, Name";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            //Fill out ArrayList with all available categories
            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("CategoryID"),
                        rs.getString("Type"),
                        rs.getString("Name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public static boolean createCategory(String name, String type) {
        if (name == null || name.isBlank() || type == null || type.isBlank()) {
            return false;
        }

        String insertSql = "INSERT INTO Category (Name, Type) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, name);
            insertStmt.setString(2, type);
            return insertStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateCategory(int categoryId, String name, String type) {
        String sql = "UPDATE Category SET Name = ?, Type = ? WHERE CategoryID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setInt(3, categoryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteCategory(int categoryId) {
        String deleteTransactionsSql = "DELETE FROM Transactions WHERE CategoryID = ?";
        String deleteCategorySql = "DELETE FROM Category WHERE CategoryID = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            /*
             * Turning off AutoCommit because we will execute several commands because
             * we don't want changes to be recorded into database until all command are executed without errors
             */
            conn.setAutoCommit(false);

            try (PreparedStatement deleteTransactions = conn.prepareStatement(deleteTransactionsSql)) {
                deleteTransactions.setInt(1, categoryId);
                deleteTransactions.executeUpdate();
            }

            int affected;
            try (PreparedStatement deleteCategory = conn.prepareStatement(deleteCategorySql)) {
                deleteCategory.setInt(1, categoryId);
                affected = deleteCategory.executeUpdate();
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

