package finalProject.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;
import finalProject.ChatClient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WaitingPageController {
	private ChatClient client;
	public int mode;
	public int difficulty;
	public int roomnum;

	public void setalldata(int mode,int difficulty,String roomnum) {
		this.mode = mode;
		this.difficulty = difficulty;
		this.roomnum = Integer.valueOf(roomnum);
	}
	
	@FXML
    public void goToGame(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Game.fxml"));
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
	
	// 建構client.
	@FXML
    public void initialize() {
        client = new ChatClient(this.mode,this.difficulty,this.roomnum);
//        while(true) {
//        	// pause 1 sec
//			//        	if a.checkstutus{
//			//        		break;
//        }
    }
}
