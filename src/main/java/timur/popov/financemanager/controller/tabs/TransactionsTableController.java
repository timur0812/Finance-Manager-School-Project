package timur.popov.financemanager.controller.tabs;

import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import timur.popov.financemanager.model.Transaction;

/**
 * Transaction table controller that is used in multiple places
 */
public class TransactionsTableController {
    @FXML private TableView<Transaction> transactionsTableView;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> userColumn;
    @FXML private TableColumn<Transaction, String> accountColumn; 
    @FXML private TableColumn<Transaction, String> descriptionColumn;

    @FXML
    public void initialize() {
        setupTableColumns();
    }

    private void setupTableColumns() {
        //Binding table columns to fields in Transaction class
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        
        accountColumn.setCellValueFactory(new PropertyValueFactory<>("account"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        
        // implementation of tooltip that keeps long descriptions readable when user points cursor at description field
        descriptionColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);

                    
                    Tooltip tooltip = new Tooltip(item);
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(200);

                    setTooltip(tooltip);
                }
            }
        });
    }
}