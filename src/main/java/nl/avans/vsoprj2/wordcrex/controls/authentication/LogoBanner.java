package nl.avans.vsoprj2.wordcrex.controls.authentication;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LogoBanner extends VBox implements Initializable {
    public LogoBanner() {
        super();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controls/authentication/LogoBanner.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
