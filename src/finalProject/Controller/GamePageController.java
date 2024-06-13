package finalProject.Controller;

import java.io.IOException;

import finalProject.Game.GameApp;
import finalProject.Socket.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class GamePageController extends BaseController {
  @FXML
  private Pane enemy;

  @FXML
  private Pane me;

  @FXML
  private Button LoseButton;

  @FXML
  private Button WinButton;

  private GameApp gameApp;

  @FXML
  public void initialize() {
    gameApp = new GameApp();
    gameApp.initialize(me, enemy, true); // 允許互動
  }

  @FXML
  public void showWin() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Win.fxml"));
    // 跳出新的視窗
    try {
      changePage(loader, new ActionEvent());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void showLose() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Lose.fxml"));
    // 跳出新的視窗
    try {
      changePage(loader, new ActionEvent());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setClientAndPlayer(Client client, int player) {
    gameApp.setClient(client);
    gameApp.setPlayer(player);
    client.setGameApp(gameApp);
    // 開一個新的執行緒來檢查 gameApp 是否有人贏了
    Thread checkWin = new Thread(() -> {
      while (true) {
        int status = gameApp.getStatus();
        if (status == 1) {
          System.out.println("Win");
          Platform.runLater(() -> {
            WinButton.fire();
          });
          break;
        } else if (status == 2) {
          System.out.println("Lose");
          Platform.runLater(() -> {
            LoseButton.fire();
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
    checkWin.start();
  }

  public void setPlayer(int player) {
    gameApp.setPlayer(player);
  }

  public void changePage(FXMLLoader loader, ActionEvent event) throws IOException {
    Parent root = loader.load();
    Scene scene = new Scene(root);
    Stage stage = new Stage();
    stage.setScene(scene);
    stage.show();
  }
}
