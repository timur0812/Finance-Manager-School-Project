package timur.popov.financemanager.controller.tabs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import timur.popov.financemanager.dao.DashboardDAO;
import timur.popov.financemanager.dao.TransactionsDAO;
import timur.popov.financemanager.model.Transaction;

/**
 * Controller for dashboard.fxml layout
 * Load total sum for current month by type
 * Load table with recent transactions
 */
public class DashboardController {
    @FXML public Label totalIncomeLabel;
    @FXML public Label totalExpenseLabel;
    @FXML public TableView<Transaction> recentTransactionsTable;

    @FXML
    public void initialize() {
        loadDashboardData();

    }

    private void loadDashboardData() {
        double income = DashboardDAO.sumCurrentMonthByType("Income");
        double expense = DashboardDAO.sumCurrentMonthByType("Expense");
        totalIncomeLabel.setText(String.valueOf(income));
        totalExpenseLabel.setText(String.valueOf(expense));

        recentTransactionsTable.setItems(TransactionsDAO.getTransactions(16));
    }
}