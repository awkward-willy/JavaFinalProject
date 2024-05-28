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
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

public class ChoosePageController {
	@FXML
	private CheckBox Standalone;
	@FXML
	private CheckBox Connect;
	@FXML
	private CheckBox Difficult;
	@FXML
	private CheckBox Easy;
	int mode;
	int difficulty;

	@FXML
	public void choosemode(ActionEvent event) throws IOException {
		if (Standalone.isSelected()) {
			Connect.setSelected(false);
			mode = 0;
		} else if (Connect.isSelected()) {
			Standalone.setSelected(false);
			mode = 1;
		}
	}

	@FXML
	public void choosedifficulty(ActionEvent event) throws IOException {
		if (Difficult.isSelected()) {
			Easy.setSelected(false);
			difficulty = 0;
		} else if (Easy.isSelected()) {
			Difficult.setSelected(false);
			difficulty = 1;
		}
	}

	@FXML
	private void goToRoom(ActionEvent event) throws IOException {
		// 先檢查是否有選擇模式和難度
		if (mode == 0 || difficulty == 0) {
			// 跳出警告視窗
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("錯誤");
			alert.setHeaderText(null);
			alert.setContentText("請選擇模式和難度");
			alert.showAndWait();
			return;
		}

		FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Room.fxml"));
		changePage(loader, event);
		RoomPageController roompagecontroller = loader.getController();
		roompagecontroller.setchoosedata(mode, difficulty);
	}

	@FXML
	private void goToFirst(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/First.fxml"));
		changePage(loader, event);
		Standalone.setSelected(false);
		Connect.setSelected(false);
		Difficult.setSelected(false);
		Easy.setSelected(false);
		mode = 0;
		difficulty = 0;
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
