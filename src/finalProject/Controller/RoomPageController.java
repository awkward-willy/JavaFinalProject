package finalProject.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;

public class RoomPageController extends BaseController {
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
			showError("錯誤", "請輸入房號");
			return;
		}

		// 嘗試 parse成數字
		try {
			Integer.parseInt(roomnumber.getText());
		} catch (NumberFormatException e) {
			showError("錯誤", "房號只能為數字");
			return;
		}

		FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Waiting.fxml"));
		changePage(loader, event);
		WaitingPageController waitingpagecontroller = loader.getController();
		waitingpagecontroller.setalldata(mode, difficulty, roomnumber.getText());
	}

}
