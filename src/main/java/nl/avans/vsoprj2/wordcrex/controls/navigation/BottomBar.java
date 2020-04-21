package nl.avans.vsoprj2.wordcrex.controls.navigation;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.management.openmbean.InvalidKeyException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BottomBar extends AnchorPane implements Initializable {
    @FXML
    private HBox bottomBar;

    private String active;


    public BottomBar() {
        super();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controls/navigation/BottomBar.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getActive() {
        return active;
    }

    public void setActive(String value) {
        ColorAdjust ca = new ColorAdjust();
        ca.setHue(0.3);
        ca.setSaturation(1.0);

        ObservableList<Node> children = bottomBar.getChildren();

        for (Node child:children) {
            if (child.getId().equals(value)) {
                child.setEffect(ca);
                this.active = child.getId();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
