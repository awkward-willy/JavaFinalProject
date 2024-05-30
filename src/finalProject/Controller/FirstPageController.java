package finalProject.Controller;

import javafx.event.ActionEvent;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

public class FirstPageController extends BaseController {
    @FXML
    public void goToIntroPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Rule.fxml"));
        changePage(loader, event);
    }

    @FXML
    public void goToChoosePage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Choose.fxml"));
        changePage(loader, event);
    }
}
