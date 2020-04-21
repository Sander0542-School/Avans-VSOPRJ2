package nl.avans.vsoprj2.wordcrex.controls.navigation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import nl.avans.vsoprj2.wordcrex.Colors;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BottomBarItem extends VBox implements Initializable {
    @FXML
    private ImageView image;
    @FXML
    private Label label;

    public BottomBarItem() {
        super();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controls/navigation/BottomBarItem.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setImage(Image image) {
        this.image.setImage(image);

        ImageView clipImage = new ImageView(image);
        clipImage.setFitHeight(24);
        clipImage.setFitWidth(24);

        this.image.setClip(clipImage);
    }

    public Image getImage() {
        return this.image.getImage();
    }

    public void setLabel(String text) {
        this.label.setText(text);
    }

    public String getLabel() {
        return this.label.getText();
    }

    public boolean isActive() {
        return this.label.getTextFill().equals(Colors.ACCENT);
    }

    public void setActive(boolean active) {
        if (active) {
            Blend blend = new Blend(BlendMode.MULTIPLY);
            ColorInput colorInput = new ColorInput(0, 0, this.image.getImage().getWidth(), this.image.getImage().getHeight(), Colors.ACCENT);
            blend.setTopInput(colorInput);
            this.image.setEffect(blend);
        } else {
            this.image.setEffect(null);
        }

        this.label.setTextFill(active ? Colors.ACCENT : Color.WHITE);
        this.label.setFont(new Font(active ? 13 : 12));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
