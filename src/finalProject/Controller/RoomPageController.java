package finalProject.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RoomPageController {
	@FXML
	private TextField roomnumber;
	public int mode;
	public int difficulty;

	public void setchoosedata(int mode, int difficulty) {
		this.mode = mode;
		this.difficulty = difficulty;
	}

	@FXML
	private void goToWaiting(ActionEvent event) throws IOException {
		// 先檢查是否有輸入房號
		if (roomnumber.getText().isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("錯誤");
			alert.setHeaderText(null);
			alert.setContentText("請輸入房號");
			alert.showAndWait();
			return;
		}

		// 嘗試 parse成數字
		try {
			Integer.parseInt(roomnumber.getText());
		} catch (NumberFormatException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("錯誤");
			alert.setHeaderText(null);
			alert.setContentText("房號只能為數字");
			alert.showAndWait();
			return;
		}

		FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Waiting.fxml"));
		changePage(loader, event);
		WaitingPageController waitingpagecontroller = loader.getController();
		waitingpagecontroller.setalldata(mode, difficulty, roomnumber.getText());
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
