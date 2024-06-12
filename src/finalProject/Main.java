package finalProject;

import finalProject.Game.MusicPlayer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;

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
            primaryStage.setResizable(false);

            // 播放背景音樂
            MusicPlayer.playBackgroundMusic();

            // primaryStage 按下 M 要 toggle 音樂播放
            primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                switch (event.getCode()) {
                    case M:
                        MusicPlayer.toggleBackgroundMusic();
                        break;
                    default:
                        break;
                }
            });

            // 關閉視窗時自動停止音樂
            primaryStage.setOnCloseRequest(event -> {
                MusicPlayer.stopBackgroundMusic();
            });

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
