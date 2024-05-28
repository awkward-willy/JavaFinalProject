package finalProject.Controller;

import finalProject.GameApp;
import finalProject.Socket.Client;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class GamePageController {
  private Client client;

  @FXML
  private Pane enemy;

  @FXML
  private Pane me;

  private GameApp myBoardApp;
  private GameApp enemyBoardApp;

  @FXML
  public void initialize() {
    myBoardApp = new GameApp();
    enemyBoardApp = new GameApp();
    myBoardApp.initialize(me);
    enemyBoardApp.initialize(enemy);
  }

  public void setClient(Client client) {
    this.client = client;
    startGame();
  }

  private void startGame() {
    myBoardApp.setClient(client);
    enemyBoardApp.setClient(client);
    client.setGameApp(myBoardApp, enemyBoardApp);
  }
}
