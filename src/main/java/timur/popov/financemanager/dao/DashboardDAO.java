package timur.popov.financemanager.dao;

import java.sql.*;
import java.time.LocalDate;

import static timur.popov.financemanager.dao.DBUtil.getConnection;

/**
 * DAO needed for proper work of dashboard
 * Have one method that returns total sum for current month by category type
 */
public class DashboardDAO {
    public static double sumCurrentMonthByType(String categoryType) {
        // Sets interval for current month
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate startOfNextMonth = startOfMonth.plusMonths(1);

        String sql = "SELECT SUM(t.Amount) FROM Transactions t " +
                "JOIN Category c ON t.CategoryID = c.CategoryID " +
                "WHERE c.Type = ? AND t.CreationDate >= ? AND t.CreationDate < ?";

        // Open connection and replace parameters(?) in query with variables
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoryType);
            pstmt.setString(2, startOfMonth.toString());
            pstmt.setString(3, startOfNextMonth.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // return value in column 1 of result set. There is only one column in result set because SUM(t.Amount)
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Return 0 if no transaction in current month
        return 0.0;
    }
}