package timur.popov.financemanager.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import timur.popov.financemanager.PasswordHashUtil;
import timur.popov.financemanager.model.User;
import timur.popov.financemanager.DialogUtil;

/**
 * Login dialog controller that handle data entered in log-in window
 */
public class LogInController {

    @FXML private DialogPane loginDialogPane;
    @FXML private Label userNameLabel;
    @FXML private PasswordField passwordField;

    private User user;

    public void setUser(User user) {
        this.user = user;
        userNameLabel.setText(user.getName());

        Node applyNode = loginDialogPane.lookupButton(ButtonType.APPLY);
        ((Button) applyNode).setText("Login");

        Node closeNode = loginDialogPane.lookupButton(ButtonType.CLOSE);
        ((Button) closeNode).setText("Cancel");
    }

    public Boolean buildResult() {
        String entered = passwordField.getText();

        if (entered.isEmpty()) {
            DialogUtil.showError("Login Error", "Password cannot be empty.");
            return null;
        }

        //checking that entered password is correct
        if (!PasswordHashUtil.matches(entered, user.getPassword())) {
            // if password doesn't match, it clears password field and dialog continues waiting for correct data
            DialogUtil.showError("Login Error", "Incorrect password. Please try again.");
            passwordField.clear();
            return null;
        }

        return true;
    }
}
