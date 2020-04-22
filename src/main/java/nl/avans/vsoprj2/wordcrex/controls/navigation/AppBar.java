package nl.avans.vsoprj2.wordcrex.controls.navigation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AppBar extends AnchorPane implements Initializable {
    @FXML
    private Label titleLabel;
    @FXML
    private ImageView backButton;
    @FXML
    private ImageView optionsButton;
    @FXML
    private ImageView deleteButton;

    public AppBar() {
        super();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controls/navigation/AppBar.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitle(String value) {
        titleLabel.setText(value);
    }

    public boolean getBackButton() {
        return backButton.isVisible();
    }

    public void setBackButton(boolean visible) {
        backButton.setVisible(visible);
        backButton.setManaged(visible);
    }

    public boolean getOptionsButton() {
        return optionsButton.isVisible();
    }

    public void setOptionsButton(boolean visible) {
        optionsButton.setVisible(visible);
        optionsButton.setManaged(visible);
    }

    public boolean getDeleteButton() {
        return deleteButton.isVisible();
    }

    public void setDeleteButton(boolean visible) {
        deleteButton.setVisible(visible);
        deleteButton.setManaged(visible);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
