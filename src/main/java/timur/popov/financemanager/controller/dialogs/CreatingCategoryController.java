package timur.popov.financemanager.controller.dialogs;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import timur.popov.financemanager.DialogUtil;

/**
 * Dialog controller for category creation/editing.
 */
public class CreatingCategoryController {

    @FXML public TextField nameField;
    @FXML public ComboBox<String> typeComboBox;
    @FXML public TextArea descriptionArea;

    public CategoryData buildResult() {
        String categoryName = nameField.getText() == null ? "" : nameField.getText().trim();
        String categoryType = (typeComboBox.getValue() == null) ? null : typeComboBox.getValue().toString();

        if (categoryName.isEmpty()) {
            DialogUtil.showError("Validation Error", "Category name cannot be empty");
            return null;
        }

        if (categoryType == null) {
            DialogUtil.showError("Validation Error", "Category type must be selected");
            return null;
        }

        return new CategoryData(categoryName, categoryType);
    }

    /*
     * This method is needed if user wants to edit an account
     * It sets values for the specific account into dialog
     */
    public void setInitialData(String name, String type) {
        nameField.setText(name);
        typeComboBox.setValue(type);
    }


    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll(
                "Income",
                "Expense"
        );
    }

    public record CategoryData(String name, String type) {}
}
