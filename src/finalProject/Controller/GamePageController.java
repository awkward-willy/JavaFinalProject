package finalProject.Controller;

import finalProject.GameApp;
import finalProject.Socket.Client;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class GamePageController extends BaseController {
  private Client client;

  @FXML
  private Pane enemy;

  @FXML
  private Pane me;

  private GameApp gameApp;

  @FXML
  public void initialize() {
    gameApp = new GameApp();
    gameApp.initialize(me, enemy, true); // 允許互動
  }

  public void setClient(Client client) {
    this.client = client;
    gameApp.setClient(client);
    startGame();
  }

  private void startGame() {
    client.setGameApp(gameApp);
  }
}
