package timur.popov.financemanager.dao;

import timur.popov.financemanager.MainWindow;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

/**
 * Implementation of methods that open and close connection to database
 *
 */
public class DBUtil {
    private static final String URL = "jdbc:sqlite:app.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        // Lazy initialization keeps startup simple while still sharing one JDBC connection.
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }
    // Creates file and tables If database file doesn't exist or doesn't have some tables
    public void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            //Read the file with sql code that creates all tables
            String sql = new String(Files.readAllBytes(
                    Paths.get(MainWindow.class.getResource("sql/creating_tables.sql").toURI())
            ));

            stmt.executeUpdate(sql);
            System.out.println("Database initialized successfully!");

        } catch (Exception e) {
            System.err.println("Error during database initialization:");
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}