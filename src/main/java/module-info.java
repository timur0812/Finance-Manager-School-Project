module timur.popov.financemanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires jdk.jshell;
    requires javafx.base;


    opens timur.popov.financemanager to javafx.fxml;
    exports timur.popov.financemanager;
    exports timur.popov.financemanager.controller;
    opens timur.popov.financemanager.controller to javafx.fxml;
    exports timur.popov.financemanager.dao;
    opens timur.popov.financemanager.dao to javafx.fxml;
    exports timur.popov.financemanager.controller.tabs;
    opens timur.popov.financemanager.controller.tabs to javafx.fxml;
    opens timur.popov.financemanager.controller.dialogs to javafx.fxml;
    opens timur.popov.financemanager.model to javafx.base, javafx.fxml;
}