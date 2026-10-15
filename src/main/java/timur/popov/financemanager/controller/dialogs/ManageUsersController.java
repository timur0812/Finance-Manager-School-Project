package timur.popov.financemanager.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import timur.popov.financemanager.MainWindow;
import timur.popov.financemanager.PasswordHashUtil;
import timur.popov.financemanager.dao.UserDAO;
import timur.popov.financemanager.model.User;
import timur.popov.financemanager.DialogUtil;

import java.io.IOException;
import java.util.Optional;

/**
 * Dialog controller for user administration actions.
 */
public class ManageUsersController {
	@FXML
	private ListView<User> usersListView;

	@FXML
	public void initialize() {
        //This code is needed to display user names in the ListView and add edit/delete buttons for each user.
		usersListView.setCellFactory(listView -> new ListCell<>() {
            // It updates each row in ListView
			@Override
			protected void updateItem(User user, boolean empty) {
				super.updateItem(user, empty);
                //Handling empty rows
				if (empty || user == null) {
					setText(null);
					setGraphic(null);
					return;
				}
                //Declaring buttons and creating a grid
				Label nameLabel = new Label(user.getName());

				Button editButton = new Button("Edit name");
				editButton.setOnAction(event -> editUserName(user));

				Button deleteButton = new Button("Delete user");
				deleteButton.setOnAction(event -> deleteUser(user));

				Region spacer = new Region();
				HBox.setHgrow(spacer, Priority.ALWAYS);

				HBox row = new HBox(10, nameLabel, spacer, editButton, deleteButton);
				setText(null);
				setGraphic(row);
			}
		});

		loadUsers();
	}

	private void loadUsers() {
		usersListView.setItems(FXCollections.observableArrayList(UserDAO.getAllUsers()));
	}

	private void editUserName(User user) {
		if (!confirmPassword(user)) {
			return;
		}

		TextInputDialog dialog = new TextInputDialog(user.getName());
		dialog.setTitle("Edit Name");
		dialog.setHeaderText("Change user name");
		dialog.setContentText("New name:");

		Optional<String> result = dialog.showAndWait();
		if (result.isEmpty()) {
			return;
		}

		String newName = result.get().trim();
		if (newName.isEmpty()) {
			DialogUtil.showError("Error", "Name cannot be empty.");
			return;
		}

		if (UserDAO.updateUserName(user.getUserId(), newName)) {
			loadUsers();
		} else {
			DialogUtil.showError("Error", "Could not update user name.");
		}
	}

	private void deleteUser(User user) {
		if (!confirmPassword(user)) {
			return;
		}

		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
				"Delete user '" + user.getName() + "'?", ButtonType.OK, ButtonType.CANCEL);
		confirmation.setTitle("Delete User");
		confirmation.setHeaderText("Please confirm");

        /*
         * If user press Close, dialog will be closed
         * If user will close dialog, confirmation.showAndWait() will be ButtonType.CANCEL and method will be over
         */
		if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
			return;
		}

		if (UserDAO.deleteUser(user.getUserId())) {
			loadUsers();
		} else {
			DialogUtil.showError("Error", "Could not delete user. This user may have linked records.");
		}
	}

	private boolean confirmPassword(User user) {
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Password Required");
		dialog.setHeaderText("Enter password for " + user.getName());

		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Password");
		dialog.getDialogPane().setContent(passwordField);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		dialog.setResultConverter(buttonType -> buttonType == ButtonType.OK ? passwordField.getText() : null);
		Optional<String> result = dialog.showAndWait();

		// Password gate is an authorization check before destructive operations.
		return result.isPresent() && PasswordHashUtil.matches(result.get(), user.getPassword());
	}

	@FXML
	private void openAddUserDialog() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("view/dialogs/user-registration-dialog.fxml"));
			Dialog<UserRegistrationController.RegistrationData> dialog = new Dialog<>();
			dialog.setDialogPane(fxmlLoader.load());
			dialog.setTitle("Add User");
			dialog.getDialogPane().getStylesheets().add(MainWindow.class.getResource("style.css").toExternalForm());

			UserRegistrationController controller = fxmlLoader.getController();

			dialog.setResultConverter(buttonType -> {
				if (buttonType == ButtonType.APPLY) {
					return controller.buildResult();
				}
				return null;
			});

			Optional<UserRegistrationController.RegistrationData> result = dialog.showAndWait();

			if (result.isPresent()) {
				String hashed = PasswordHashUtil.hash(result.get().password());
				boolean saved = UserDAO.createUser(result.get().name(), hashed);
				if (saved) {
					loadUsers();
					DialogUtil.showInfo("User Created", "New user was added.");
				} else {
					DialogUtil.showError("Error", "Could not save user.");
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
