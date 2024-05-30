package finalProject.Controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

public class RulePageController extends BaseController {
    @FXML
    private void goToFirst(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/First.fxml"));
        changePage(loader, event);
    }
}
