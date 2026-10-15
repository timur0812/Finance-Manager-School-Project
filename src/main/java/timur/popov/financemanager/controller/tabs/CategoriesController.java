package timur.popov.financemanager.controller.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import timur.popov.financemanager.MainWindow;
import timur.popov.financemanager.controller.dialogs.CreatingCategoryController;
import timur.popov.financemanager.dao.CategoriesDAO;
import timur.popov.financemanager.model.Category;
import timur.popov.financemanager.DialogUtil;

import java.io.IOException;

/**
 * Controller that handles all interaction with Category tab
 */
public class CategoriesController {
    @FXML private TextField searchCategoryField;
    @FXML private Button searchButton;
    @FXML private Button addNewCategoryButton;

    @FXML private TableView<Category> categoryTableView;
    @FXML private TableColumn<Category, String> categoryTypeColumn; 
    @FXML private TableColumn<Category, String> categoryNameColumn;

    private final ObservableList<Category> categoryData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Binding columns to fields in Category class
        categoryTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        loadCategoriesFromDatabase();

        categoryTableView.setItems(categoryData);

        //By clicking twice on specific raw, showEditDeleteDialog appears
        categoryTableView.setRowFactory(tv -> {
            TableRow<Category> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEditDeleteDialog(row.getItem());
                }
            });
            return row;
        });


        searchButton.setOnAction(event -> handleSearch());
        addNewCategoryButton.setOnAction(event -> openCreateCategoryDialog());
    }

    private void openCreateCategoryDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("view/dialogs/create-category-dialog.fxml"));
            Dialog<CreatingCategoryController.CategoryData> dialog = new Dialog<>();

            dialog.setDialogPane(fxmlLoader.load());
            dialog.setTitle("Create new Category");

            //Each dialog has own controller which handle entered data
            CreatingCategoryController controller = fxmlLoader.getController();

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.APPLY) {
                    // method buildResult() creates and returns AccountData instance with entered data
                    return controller.buildResult();
                } else if (dialogButton == ButtonType.CLOSE) {
                    // return empty data when operation is canceled
                    return new CreatingCategoryController.CategoryData(null, null);
                }
                return null;
            });
            // Retrieving CategoryData from dialog
            CreatingCategoryController.CategoryData data = DialogUtil.showUntilPresent(dialog).get();

            //If dialog was closed before completing, type = null -> method will stop
            if(data.name() == null) {
                return;
            }

            boolean saved = CategoriesDAO.createCategory(data.name(), data.type());
            if (saved) {
                refreshCategoriesView();
            } else {
                DialogUtil.showError("Error", "Could not save category");
            }

        } catch (IOException e) {
            System.err.println("Could not find create-category-dialog.fxml.");
            e.printStackTrace();
        }
    }

    private void loadCategoriesFromDatabase() {
        categoryData.setAll(CategoriesDAO.getAllCategories());
    }

    private void showEditDeleteDialog(Category category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Category Actions");
        alert.setHeaderText("Edit or Delete Category");
        alert.setContentText("Choose an action for: " + category.getName());

        ButtonType editButton = new ButtonType("Edit");
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(editButton, deleteButton, cancelButton);

        alert.showAndWait().ifPresent(type -> {
            if (type == editButton) {
                handleEdit(category);
            } else if (type == deleteButton) {
                handleDelete(category);
            }
        });
    }

    private void handleEdit(Category category) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("view/dialogs/create-category-dialog.fxml"));
            Dialog<CreatingCategoryController.CategoryData> dialog = new Dialog<>();
            dialog.setDialogPane(fxmlLoader.load());
            dialog.setTitle("Edit Category");

            CreatingCategoryController controller = fxmlLoader.getController();
            controller.setInitialData(category.getName(), category.getType());

            // Opening dialog for editing account data
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.APPLY) {
                    return controller.buildResult();
                } else if (dialogButton == ButtonType.CLOSE) {
                    return new CreatingCategoryController.CategoryData(null, null);
                }
                return null;
            });

            // Retrieving CategoryData from dialog
            CreatingCategoryController.CategoryData data = DialogUtil.showUntilPresent(dialog).get();
            if (data.name() == null) {
                return;
            }

            boolean updated = CategoriesDAO.updateCategory(category.getCategoryId(), data.name(), data.type());
            if (updated) {
                refreshCategoriesView();
            } else {
                DialogUtil.showError("Error", "Could not update category.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleDelete(Category category) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Category");
        confirm.setHeaderText("Delete selected category?");
        confirm.setContentText(category.getName());

        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return;
            }

            boolean deleted = CategoriesDAO.deleteCategory(category.getCategoryId());
            if (deleted) {
                refreshCategoriesView();
            } else {
                DialogUtil.showError("Error", "Could not delete category. It may be used in transactions.");
            }
        });
    }

    private void refreshCategoriesView() {
        loadCategoriesFromDatabase();
        handleSearch();
    }

    private void handleSearch() {
        String searchText = searchCategoryField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            categoryTableView.setItems(categoryData);
        } else {
            /*
             * Here I used lambda expression. each category instance in categoryData checks that it contains
             * searched text. If it returns true, category will be added to categoryTableView
             */
            ObservableList<Category> filteredList = categoryData.filtered(category ->
                    category.getName().toLowerCase().contains(searchText) ||
                            category.getType().toLowerCase().contains(searchText)
            );
            categoryTableView.setItems(filteredList);
        }
    }
}