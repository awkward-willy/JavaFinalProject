package finalProject.Controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RulePageController {
    @FXML
    public void goToPreviousPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/First.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        // 取得上一個頁面創建的stage並將其中的scene替換成更改後的scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        // 顯示頁面
        stage.show();
    }
}
