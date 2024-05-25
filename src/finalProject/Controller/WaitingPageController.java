package finalProject.Controller;

import finalProject.ChatClient;
import javafx.fxml.FXML;

public class WaitingPageController {
	private ChatClient client;
	int roomNumber, difficult, mode;
	
	// 建構client.
	@FXML
    public void initialize() {
        client = new ChatClient();
//        while(true) {
//        	// pause 1 sec
//			//        	if a.checkstutus{
//			//        		break;
//        }
    }
}
