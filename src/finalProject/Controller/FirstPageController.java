package finalProject.Controller;

import javafx.event.ActionEvent;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FirstPageController {
    @FXML
    public void goToIntroPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Rule.fxml"));
        changePage(loader, event);
    }
    
    @FXML
    public void goToChoosePage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Choose.fxml"));
        changePage(loader, event);
    }
    
    public void changePage(FXMLLoader loader, ActionEvent event) throws IOException {
    	 Parent root = loader.load();
         Scene scene = new Scene(root);
         // 取得上一個頁面創建的stage並將其中的scene替換成更改後的scene
         Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
         stage.setScene(scene);
         // 顯示頁面
         stage.show();
    }
}
