package timur.popov.financemanager.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import timur.popov.financemanager.DialogUtil;

/**
 * Dialog controller for user registration.
 */
public class UserRegistrationController {
	@FXML public TextField nameField;
	@FXML public PasswordField passwordField;

	public RegistrationData buildResult() {
		String name = nameField.getText() == null ? "" : nameField.getText().trim();
		String password = passwordField.getText() == null ? "" : passwordField.getText();

		if (name.isEmpty()) {
			DialogUtil.showError("Validation Error", "Name cannot be empty.");
			return null;
		}

		if (password.isEmpty()) {
			DialogUtil.showError("Validation Error", "Password cannot be empty.");
			return null;
		}

		return new RegistrationData(name, password);
	}

	public record RegistrationData(String name, String password) {
	}
}
