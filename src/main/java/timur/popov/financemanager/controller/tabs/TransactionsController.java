package timur.popov.financemanager.controller.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import timur.popov.financemanager.dao.TransactionsDAO;
import timur.popov.financemanager.model.Transaction;
import timur.popov.financemanager.DialogUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller that handles all interaction with Transaction tab
 */
public class TransactionsController {
    @FXML private Button searchButton;
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private ComboBox<String> accountFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private ComboBox<String> typeFilterComboBox;
    @FXML private ComboBox<String> userFilterComboBox;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private Spinner<Double> amountFromSpinner;
    @FXML private Spinner<Double> amountToSpinner;
    @FXML private TextField searchTransactionsField;


    @FXML
    public void initialize() {
        // Load values to filters
        setupFilterComboBox(accountFilterComboBox, "All Accounts", TransactionsDAO.getColumnData("Account", "Name"));
        setupFilterComboBox(userFilterComboBox, "All Users", TransactionsDAO.getColumnData("User", "Name"));
        setupFilterComboBox(categoryFilterComboBox, "All Categories", TransactionsDAO.getColumnData("Category", "Name"));
        typeFilterComboBox.setItems(FXCollections.observableArrayList("All Types", "Income", "Expense"));
        typeFilterComboBox.getSelectionModel().selectFirst();

        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        setupRowActions();
        setupAmountSpinners();
        handleSearch();

        searchButton.setOnAction(event -> handleSearch());
    }

    private void setupFilterComboBox(ComboBox<String> comboBox, String firstItem, List<String> items) {
        ObservableList<String> options = FXCollections.observableArrayList(firstItem);
        options.addAll(items);
        comboBox.setItems(options);
        comboBox.getSelectionModel().selectFirst();
    }

    private void setupRowActions() {
        transactionsTable.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    //By clicking twice on specific raw, showEditDeleteDialog appears
                    showEditDeleteDialog(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupAmountSpinners() {
        amountFromSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100000.0, 0.0, 10.0));
        amountToSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100000.0, 100000.0, 10.0));
        amountFromSpinner.setEditable(true);
        amountToSpinner.setEditable(true);
    }

    private void showEditDeleteDialog(Transaction transaction) {
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Transaction Actions");
        alert.setHeaderText("Edit or Delete Transaction");
        alert.setContentText("Choose an action for: " + transaction.getDescription());

        ButtonType editButton = new ButtonType("Edit");
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(editButton, deleteButton, cancelButton);

        alert.showAndWait().ifPresent(type -> {
            if (type == editButton) {
                handleEdit(transaction);
            } else if (type == deleteButton) {
                handleDelete(transaction);
            }
        });
    }

    private void handleEdit(Transaction transaction) {
        // Creating dialog and adding fields to it
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction");

        TextField amountField = new TextField(String.valueOf(transaction.getAmount()));
        amountField.setPromptText("Amount");

        TextArea descriptionArea = new TextArea(transaction.getDescription() == null ? "" : transaction.getDescription());
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        VBox content = new VBox(10,
                new Label("Amount"), amountField,
                new Label("Description"), descriptionArea
        );
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType != ButtonType.APPLY) {
                return;
            }

            double newAmount;
            try {
                //converting string amount value from the field into that double
                newAmount = Double.parseDouble(amountField.getText());
            } catch (NumberFormatException e) {
                DialogUtil.showError("Validation Error", "Amount must be a number.");
                return;
            }
            // Checking that amount is a positive number
            if (newAmount <= 0) {
                DialogUtil.showError("Validation Error", "Amount must be positive.");
                return;
            }

            boolean updated = TransactionsDAO.updateTransactionBasic(
                    transaction.getId(),
                    newAmount,
                    descriptionArea.getText()
            );

            if (updated) {
                //refreshing table with actual data
                handleSearch();
            } else {
                DialogUtil.showError("Error", "Could not update transaction.");
            }
        });
    }

    private void handleDelete(Transaction transaction) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Transaction");
        confirm.setHeaderText("Delete selected transaction?");
        confirm.setContentText(transaction.getDescription());

        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return;
            }

            boolean deleted = TransactionsDAO.deleteTransaction(transaction.getId());
            if (deleted) {
                //refreshing table with actual data
                handleSearch();
            } else {
                DialogUtil.showError("Error", "Could not delete transaction.");
            }
        });
    }


    @FXML
    public void handleSearch() {
        //Retrieving data from filters
        String searchText = searchTransactionsField.getText();

        String selectedType = normalizeFilterValue(typeFilterComboBox, "All Types");
        String selectedUser = normalizeFilterValue(userFilterComboBox, "All Users");
        String selectedAccount = normalizeFilterValue(accountFilterComboBox, "All Accounts");
        String selectedCategory = normalizeFilterValue(categoryFilterComboBox, "All Categories");

        LocalDate fromDate = dateFromPicker.getValue();
        LocalDate toDate = dateToPicker.getValue();
        Double minAmount = (Double) amountFromSpinner.getValue();
        Double maxAmount = (Double) amountToSpinner.getValue();

        
        //Getting spinner values that appear when program starts(default values)
        Double minBound = ((SpinnerValueFactory.DoubleSpinnerValueFactory) amountFromSpinner.getValueFactory()).getMin();
        Double maxBound = ((SpinnerValueFactory.DoubleSpinnerValueFactory) amountToSpinner.getValueFactory()).getMax();

        // If default values are not changed, we assume that this filter is not used, so we set it to null
        if (minAmount != null && minAmount.equals(minBound)) {
            minAmount = null;
        }
        if (maxAmount != null && maxAmount.equals(maxBound)) {
            maxAmount = null;
        }

        List<Transaction> results = TransactionsDAO.searchTransactions(
                searchText, selectedType, selectedUser, selectedAccount, selectedCategory, fromDate, toDate, minAmount, maxAmount
        );

        transactionsTable.setItems(FXCollections.observableArrayList(results));
    }

    private String normalizeFilterValue(ComboBox<String> comboBox, String allValue) {
        String value = comboBox.getValue();
        return (value == null || value.equals(allValue)) ? null : value;
    }

}
