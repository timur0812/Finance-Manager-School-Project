package timur.popov.financemanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import timur.popov.financemanager.dao.DBUtil;

import java.io.IOException;

/**
 * Main JavaFX application shell in the MVC setup.
 * Initialize connection to database when program start anc close connection to db when program stops.
 * Creates a main window using main-window.fxml layout, add styles and set up basic parameters such as title and size.
 */
public class MainWindow extends Application {
    @Override
    public void init() throws Exception {
        super.init();
        new DBUtil().initializeDatabase();
    }

    @Override
    public void stop() throws Exception {
        DBUtil.closeConnection();
        super.stop();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("view/main-window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        scene.getStylesheets().add(String.valueOf(MainWindow.class.getResource("style.css"))); 
        stage.setTitle("Budget Tracker");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
