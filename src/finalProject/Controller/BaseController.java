package finalProject.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class BaseController {
    public void changePage(FXMLLoader loader, ActionEvent event) throws IOException {
        Parent root = loader.load();
        Scene scene = new Scene(root);
        // 取得上一個頁面創建的stage並將其中的scene替換成更改後的scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        // 顯示頁面
        stage.show();
    }

    // 顯示錯誤訊息
    public void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
