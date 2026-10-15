package timur.popov.financemanager.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import timur.popov.financemanager.DialogUtil;
import timur.popov.financemanager.dao.UserDAO;
import timur.popov.financemanager.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog controller for account creation/editing.
 */
public class CreatingAccountController {
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField accountNameField;
    @FXML private ListView<User> usersListView;

    @FXML
    public void initialize() {
        //adding options to ComboBox
        typeComboBox.getItems().addAll("Debit", "Credit", "Savings", "Cash", "Investment");

        // SelectionMode.MULTIPLE will allow multiple users to be selected.
        usersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        /*
         * By default, ListView will display users using item.toString()
         * In this case, users will be displayed like User@76e15ffd which is not user-friendly
         * This code override updateItem method, so that ListView will use item.getName() to display users
         */
        usersListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        usersListView.getItems().setAll(UserDAO.getAllUsers());
    }

    public AccountData buildResult() {
        //getting entered data from dialog
        String selectedType = typeComboBox.getValue();
        String accountName = accountNameField.getText() == null ? "" : accountNameField.getText().trim();
        List<User> selectedUsers = usersListView.getSelectionModel().getSelectedItems();

        if (selectedType == null || selectedType.isBlank()) {
            DialogUtil.showError("Validation Error", "Type must be selected");
            return null;
        }

        if (accountName.isBlank()) {
            DialogUtil.showError("Validation Error", "Account name cannot be empty");
            return null;
        }

        if (selectedUsers == null || selectedUsers.isEmpty()) {
            DialogUtil.showError("Validation Error", "Select at least one user");
            return null;
        }

        List<Integer> userIds = new ArrayList<>();
        //filling out list with user IDs
        for (User user : selectedUsers) {
            userIds.add(user.getUserId());
        }

        return new AccountData(selectedType, accountName, userIds);
    }

    /*
     * This method is needed if user wants to edit an account
     * It sets values for the specific account into dialog
     */
    public void setInitialData(String type, String accountName, List<Integer> selectedUserIds) {
        typeComboBox.setValue(type);
        accountNameField.setText(accountName);

        if (selectedUserIds == null || selectedUserIds.isEmpty()) {
            return;
        }

        usersListView.getSelectionModel().clearSelection();
        for (int i = 0; i < usersListView.getItems().size(); i++) {
            User user = usersListView.getItems().get(i);
            if (selectedUserIds.contains(user.getUserId())) {
                usersListView.getSelectionModel().select(i);
            }
        }
    }

    public record AccountData(String type, String accountName, List<Integer> userIds) {}
}
