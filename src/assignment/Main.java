package assignment;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // 加載 FXML
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("./FXML/First.fxml"));
	        Parent root = loader.load();
            
            // 配置 Stage
            primaryStage.setTitle("JAVA FINAL PROJECT");
            primaryStage.setScene(new Scene(root));

            // 顯示 Stage
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
