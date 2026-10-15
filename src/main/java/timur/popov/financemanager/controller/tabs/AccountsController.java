package timur.popov.financemanager.controller.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import timur.popov.financemanager.MainWindow;
import timur.popov.financemanager.controller.dialogs.CreatingAccountController;
import timur.popov.financemanager.dao.AccountsDAO;
import timur.popov.financemanager.model.Account;
import timur.popov.financemanager.DialogUtil;

import java.io.IOException;
import java.util.List;

/**
 * Controller for account management.
 */
public class AccountsController {
    @FXML private TextField searchAccountField;
    @FXML private Button searchButton;
    @FXML private Button addNewAccountButton;

    @FXML private TableView<Account> accountTableView;
    @FXML private TableColumn<Account, String> accountTypeColumn;
    @FXML private TableColumn<Account, String> accountNameColumn;
    @FXML private TableColumn<Account, String> accountUsersColumn;

    private final ObservableList<Account> accountData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Argument of new PropertyValueFactory<>() must have the same name as in Account class. Otherwise, they will not be bound and there will be empty column
        accountTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        accountNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        accountUsersColumn.setCellValueFactory(new PropertyValueFactory<>("users"));

        loadAccounts();
        accountTableView.setItems(accountData);

        //By clicking twice on specific raw, showEditDeleteDialog appears
        accountTableView.setRowFactory(tv -> {
            TableRow<Account> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEditDeleteDialog(row.getItem());
                }
            });
            return row;
        });

        searchButton.setOnAction(event -> handleSearch());
        addNewAccountButton.setOnAction(event -> openCreateAccountDialog());
    }

    private void openCreateAccountDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("view/dialogs/create-account-dialog.fxml"));
            Dialog<CreatingAccountController.AccountData> dialog = new Dialog<>();
            dialog.setDialogPane(fxmlLoader.load());
            dialog.setTitle("Create New Account");
            dialog.getDialogPane().getStylesheets().add(MainWindow.class.getResource("style.css").toExternalForm());

            //Each dialog has own controller which handle entered data
            CreatingAccountController controller = fxmlLoader.getController();

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.APPLY) {
                    // method buildResult() creates and returns AccountData instance with entered data
                    return controller.buildResult();
                } else if (dialogButton == ButtonType.CLOSE) {
                    // return empty data when operation is canceled
                    return new CreatingAccountController.AccountData(null, null, List.of());
                }
                return null;
            });
            // Retrieving AccountData from dialog
            CreatingAccountController.AccountData data = DialogUtil.showUntilPresent(dialog).get();

            //If dialog was closed before completing, type = null -> method will stop
            if (data.type() == null) {
                return;
            }

            boolean created = AccountsDAO.createAccountWithUsers(data.type(), data.accountName(), data.userIds());
            if (created) {
                refreshAccountsView();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showEditDeleteDialog(Account account) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Account Actions");
        alert.setHeaderText("Edit or Delete Account");
        alert.setContentText("Choose an action for: " + account.getName());

        ButtonType editButton = new ButtonType("Edit");
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(editButton, deleteButton, cancelButton);

        alert.showAndWait().ifPresent(type -> {
            if (type == editButton) {
                handleEdit(account);
            } else if (type == deleteButton) {
                handleDelete(account);
            }
        });
    }

    private void handleEdit(Account account) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("view/dialogs/create-account-dialog.fxml"));
            Dialog<CreatingAccountController.AccountData> dialog = new Dialog<>();
            dialog.setDialogPane(fxmlLoader.load());
            dialog.setTitle("Edit Account");
            dialog.getDialogPane().getStylesheets().add(MainWindow.class.getResource("style.css").toExternalForm());

            CreatingAccountController controller = fxmlLoader.getController();
            controller.setInitialData(account.getType(), account.getName(), AccountsDAO.getUserIdsForAccount(account.getACCOUNT_ID()));

            // Opening dialog for editing account data
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.APPLY) {
                    return controller.buildResult();
                } else if (dialogButton == ButtonType.CLOSE) {
                    return new CreatingAccountController.AccountData(null, null, List.of());
                }
                return null;
            });

            CreatingAccountController.AccountData data = DialogUtil.showUntilPresent(dialog).get();
            if (data.type() == null) {
                return;
            }
            boolean updated = AccountsDAO.updateAccountWithUsers(
                    account.getACCOUNT_ID(),
                    data.type(),
                    data.accountName(),
                    data.userIds()
            );

            if (updated) {
                refreshAccountsView();
            } else {
                DialogUtil.showError("Error", "Could not update account.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleDelete(Account account) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Account");
        confirm.setHeaderText("Delete selected account?");
        confirm.setContentText(account.getName());

        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return;
            }

            boolean deleted = AccountsDAO.deleteAccount(account.getACCOUNT_ID());
            if (deleted) {
                refreshAccountsView();
            } else {
                DialogUtil.showError("Error", "Could not delete account.");
            }
        });
    }

    private void loadAccounts() {
        List<Account> accounts = AccountsDAO.getAllAccountsWithUsers();
        accountData.setAll(accounts);
    }

    private void refreshAccountsView() {
        loadAccounts();
        handleSearch();
    }

    private void handleSearch() {
        String searchText = searchAccountField.getText() == null ? "" : searchAccountField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            accountTableView.setItems(accountData);
            return;
        }
        /*
         * Here I used lambda expression. matchesSearch is applied to each account instance from accountData
         * If matchesSearch returns true, account will be added to accountTableView
         */
        accountTableView.setItems(accountData.filtered(account -> matchesSearch(account, searchText)));
    }

    private boolean matchesSearch(Account account, String searchText) {
        if (searchText.isEmpty()) {
            return true;
        }

        return containsText(account.getName(), searchText)
                || containsText(account.getType(), searchText)
                || containsText(account.getUsers(), searchText);
    }

    private boolean containsText(String value, String searchText) {
        return value != null && value.toLowerCase().contains(searchText);
    }
}
