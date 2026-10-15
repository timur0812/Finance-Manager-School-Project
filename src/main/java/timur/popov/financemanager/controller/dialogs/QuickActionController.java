package timur.popov.financemanager.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import timur.popov.financemanager.UserSession;
import timur.popov.financemanager.dao.TransactionsDAO;
import timur.popov.financemanager.model.User;
import timur.popov.financemanager.DialogUtil;

import java.util.List;

/**
 * Shared dialog controller used by both add income and add expense dialogs.
 * It is possible because income and expense dialogs have same fields
 */
public class QuickActionController {
    @FXML public TextField amountField;
    @FXML public ComboBox<String> accountComboBox;
    @FXML public ComboBox<String> categoryComboBox;
    @FXML public TextArea descriptionArea;
    private String transactionType;

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
        loadOptionsFromDatabase();
    }

    private void loadOptionsFromDatabase() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        List<String> accounts = TransactionsDAO.getAccountsForUser(currentUser.getUserId());
        //Getting all available accounts for current user
        accountComboBox.getItems().setAll(accounts);
        //Getting all available categories for current user
        List<String> categories = TransactionsDAO.getCategoriesByType(transactionType);
        categoryComboBox.getItems().setAll(categories);
    }

    public EnteredData buildResult() {
        String amountField = this.amountField.getText();
        String descriptionField = this.descriptionArea.getText();
        String account = accountComboBox.getValue();
        String category = categoryComboBox.getValue();


        double amount;
        if(amountField.isEmpty()){
            DialogUtil.showError("Validation Error", "Amount cannot be empty.");
            return null;
        } else {
            try {
                amount = Double.parseDouble(amountField);
            } catch (NumberFormatException e) {
                DialogUtil.showError("Validation Error", "Amount must be a number");
                return null;
            }
        }

        if (amount <= 0) {
            DialogUtil.showError("Validation Error", "Amount must be a positive value");
            return null;
        }

        if (account == null) {
            DialogUtil.showError("Validation Error", "Account must be selected");
            return null;
        }

        if (category == null) {
            DialogUtil.showError("Validation Error", "Category must be selected");
            return null;
        }

        return new EnteredData(amount, account, category, descriptionField);
    }


    @FXML
    public void initialize() {
        loadOptionsFromDatabase();
    }

    public record EnteredData(double amount, String account, String category, String description) {}
}

