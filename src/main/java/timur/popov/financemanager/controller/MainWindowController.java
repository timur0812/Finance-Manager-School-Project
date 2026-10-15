package timur.popov.financemanager.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import timur.popov.financemanager.MainWindow;
import timur.popov.financemanager.UserSession;
import timur.popov.financemanager.controller.dialogs.LogInController;
import timur.popov.financemanager.controller.dialogs.QuickActionController;
import timur.popov.financemanager.dao.TransactionsDAO;
import timur.popov.financemanager.dao.UserDAO;
import timur.popov.financemanager.model.User;
import timur.popov.financemanager.DialogUtil;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Primary MVC controller that coordinates the work of main window
 */
public class MainWindowController {
    @FXML
    public ComboBox<User> userSwitcherComboBox;
    @FXML
    private BorderPane mainBorderPane;

    private User lastLogged = null;

    private String currentSection = "dashboard";

    @FXML
    public void initialize() {

        //Change the format of displayed users(from User@76e15ffd to user name)
        userSwitcherComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user != null ? user.getName() : "";
            }
            @Override
            public User fromString(String string) { return null; }
        });


        List<User> users = UserDAO.getAllUsers();
        userSwitcherComboBox.getItems().setAll(users);


        // Start locked so the user must authenticate before interacting with app content.
        userSwitcherComboBox.getSelectionModel().clearSelection();
        userSwitcherComboBox.setPromptText("Select User");
        UserSession.getInstance().setCurrentUser(null);
        lastLogged = null;
        setAppLocked(true);
        DialogUtil.showInfo("Login Required", "Please choose a user and log in to continue.");

        //handling user switch and call dialog to log in
        userSwitcherComboBox.setOnAction(event -> {
            User selected = userSwitcherComboBox.getValue();
            if (selected == null) return;

            if (lastLogged != null && selected.getUserId() == lastLogged.getUserId()) return;

            boolean success = showLoginDialog(selected);
            if (success) {
                lastLogged = selected;
                UserSession.getInstance().setCurrentUser(selected);
                setAppLocked(false);
            } else {
                setAppLocked(true);
                lastLogged = null;
            }
        });
    }

    //switches tab on the main window
    public void SwitchSectionOnMainWindow(ActionEvent actionEvent) {
        if (!ensureAuthenticated()) {
            return;
        }
        // Regex extracts button id from ActionEvent string
        Pattern pattern = Pattern.compile("id=(\\w+)");

        //actionEvent.toString() = javafx.event.ActionEvent[source=Button[id=transactionsButton, styleClass=button sidebar-button]'Transactions']
        Matcher matcher = pattern.matcher(actionEvent.toString());
        String result;
        try {
            if (matcher.find()) {
                result = matcher.group(1).replaceFirst("Button$", "");
                currentSection = result;
            } else {
                throw new Exception("wrong name of Button");
            }

            loadSection(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
    @FXML
    private void handleManageUsers() {
        if (!ensureAuthenticated()) {
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("view/dialogs/manage-users-dialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(fxmlLoader.load());
            dialog.setTitle("Manage Users");
            dialog.getDialogPane().getStylesheets().add(MainWindow.class.getResource("style.css").toExternalForm());

            dialog.showAndWait();
            refreshUserSwitcher();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleAddIncome(ActionEvent actionEvent) throws IOException {
        if (!ensureAuthenticated()) {
            return;
        }

        openQuickActionDialog(
                "view/dialogs/add-income-dialog.fxml",
                "Add Income",
                "Income",
                "Income transaction has been saved.",
                "Could not save income transaction."
        );
    }
    @FXML
    public void handleAddExpense(ActionEvent actionEvent) throws IOException {
        if (!ensureAuthenticated()) {
            return;
        }

        openQuickActionDialog(
                "view/dialogs/add-expense-dialog.fxml",
                "Add Expanse",
                "Expense",
                "Expense transaction has been saved.",
                "Could not save expense transaction."
        );
    }

    private void openQuickActionDialog(String resourcePath, String dialogTitle, String transactionType,
                                       String successMessage, String errorMessage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource(resourcePath));
        Dialog<QuickActionController.EnteredData> dialog = new Dialog<>();

        dialog.setDialogPane(fxmlLoader.load());
        dialog.setTitle(dialogTitle);

        QuickActionController controller = fxmlLoader.getController();
        controller.setTransactionType(transactionType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.APPLY) {
                return controller.buildResult();
            }
            if (dialogButton == ButtonType.CLOSE) {
                // If user cancel operation of adding new transaction, the empty object will be returned.
                return new QuickActionController.EnteredData(-1, null, null, null);
            }
            return null;
        });

        QuickActionController.EnteredData data = DialogUtil.showUntilPresent(dialog).get();

        //check that object is not empty
        if (data.amount() == -1) {
            return;
        }

        boolean saved = TransactionsDAO.createTransaction(
                UserSession.getInstance().getCurrentUser().getUserId(),
                data.account(),
                data.category(),
                transactionType,
                data.amount(),
                data.description()
        );

        if (saved) {
            refreshVisibleTransactionData();
            DialogUtil.showInfo("Success", successMessage);
        } else {
            DialogUtil.showError("Error", errorMessage);
        }
    }

    //After adding new transaction this method updates tables in dashboard and transactions sections
    private void refreshVisibleTransactionData() {
        if ("transactions".equals(currentSection) || "dashboard".equals(currentSection)) {
            loadSection(currentSection);
        }
    }

    private void loadSection(String sectionName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource(String.format("view/%s.fxml", sectionName)));
            mainBorderPane.setCenter(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean showLoginDialog(User user) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("view/dialogs/log-in-dialog.fxml"));
            Dialog<Boolean> dialog = new Dialog<>();
            dialog.setDialogPane(fxmlLoader.load());
            dialog.setTitle("Login");

            LogInController controller = fxmlLoader.getController();
            controller.setUser(user);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.APPLY) {
                    return controller.buildResult();
                }
                return false;
            });

            return DialogUtil.showUntilPresent(dialog).get();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void refreshUserSwitcher() {
        User current = UserSession.getInstance().getCurrentUser();
        Integer currentUserId = current != null ? current.getUserId() : null;

        List<User> users = UserDAO.getAllUsers();
        userSwitcherComboBox.getItems().setAll(users);

        User selected = null;
        if (currentUserId != null) {
            for (User user : users) {
                if (user.getUserId() == currentUserId) {
                    selected = user;
                    break;
                }
            }
        }

        userSwitcherComboBox.setValue(selected);

        UserSession.getInstance().setCurrentUser(selected);
        lastLogged = selected;
        setAppLocked(false);
    }

    private boolean ensureAuthenticated() {
        // Guard clause pattern: central place that blocks protected actions in this controller.
        if (UserSession.getInstance().getCurrentUser() != null) {
            return true;
        }

        DialogUtil.showInfo("Login Required", "Please choose a user and log in to continue.");
        return false;
    }

    private void setAppLocked(boolean locked) {
        if (mainBorderPane.getCenter() != null) {
            mainBorderPane.getCenter().setDisable(locked);
        }
    }

}
