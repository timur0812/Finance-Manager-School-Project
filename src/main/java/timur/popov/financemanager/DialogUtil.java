package timur.popov.financemanager;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.Optional;

/**
 * Utility class that implement all dialog behavior to keep controllers small. Example of encapsulation.
 */
public final class DialogUtil {
    private DialogUtil() {
    }

    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    public static <T> Optional<T> showUntilPresent(Dialog<T> dialog) {
        Optional<T> result;
        /*
         * Repeats until controller returns a non-null result.
         * It's needed when user tries to create new transaction, category, account and so on.
         * The result variable contains an Optional instance with the data entered by the user.
         */
        do {
            result = dialog.showAndWait();
        } while (result.isEmpty());
        return result;
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

