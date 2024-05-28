package finalProject.Controller;

import javafx.fxml.FXML;
import java.io.IOException;
import java.net.ConnectException;

import finalProject.Socket.Client;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class WaitingPageController {
    private Client client;
    public int mode;
    public int difficulty;
    public int roomnum;
    private volatile boolean startGame = false;

    @FXML
    private Button GameButton;

    @FXML
    private Button MenuButton;

    public void setalldata(int mode, int difficulty, String roomnum) {
        this.mode = mode;
        this.difficulty = difficulty;
        this.roomnum = Integer.valueOf(roomnum);

        // 當視窗關閉時，關閉連線
        Stage stage = (Stage) GameButton.getScene().getWindow();
        stage.setOnCloseRequest((WindowEvent event) -> {
            client.closeConnection();
            // 結束 JavaFX 應用程式
            Platform.exit();
        });

        try {
            // 測試伺服器是否可用
            if (client.testServerAvailable()) {
                startCheckingGameStatus();
            } else {
                showErrorAndGoToMenu("無法連線至伺服器，可能是因為伺服器尚未啟動，請稍後再試。");
                return;
            }
        } catch (RuntimeException e) {
            showErrorAndGoToMenu("無法連線至伺服器，可能是因為伺服器尚未啟動，請稍後再試。");
            return;
        }
    }

    @FXML
    public void goToGame(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Game.fxml"));
        changePage(loader, event, GamePageController.class);
    }

    @FXML
    public void goToMenu(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/First.fxml"));
        changePage(loader, event, FirstPageController.class);
    }

    private void startCheckingGameStatus() {
        // 透過 client 連線至伺服器
        client.initConnection(roomnum, mode, difficulty);

        // 建立新執行緒以避免阻塞 JavaFX 執行緒
        Thread checkerThread = new Thread(() -> {
            while (!startGame) {
                startGame = client.getStartGame();
                if (startGame) {
                    // 使用 Platform.runLater()，因目前執行緒為背景執行緒，無法直接更新 JavaFX 元件
                    // runLater 會將要執行的程式碼加入 JavaFX 執行緒的執行序列中，等待執行
                    Platform.runLater(() -> {
                        // 觸發隱藏的按鈕，進入遊戲畫面
                        GameButton.fire();
                    });
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        // 設定執行緒為背景執行緒，當應用程式結束時，背景執行緒會自動結束
        checkerThread.setDaemon(true);
        checkerThread.start();
    }

    // 顯示錯誤彈跳視窗並返回主畫面
    private void showErrorAndGoToMenu(String errorMessage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
            MenuButton.fire();
        });
    }

    public void changePage(FXMLLoader loader, ActionEvent event, Class<?> controllerClass) throws IOException {
        // 載入新的 FXML 檔案
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();

        // 取得頁面的 controller
        Object controller = loader.getController();
        if (controllerClass.isInstance(controller)) {
            if (controller instanceof GamePageController) {
                ((GamePageController) controller).setClient(client);
            }
        } else {
            throw new IllegalStateException("Controller type mismatch.");
        }
    }

    // 建構client.
    @FXML
    public void initialize() {
        client = new Client();
    }
}
