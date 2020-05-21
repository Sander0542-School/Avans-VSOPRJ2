package nl.avans.vsoprj2.wordcrex.controls.gameboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import nl.avans.vsoprj2.wordcrex.Colors;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BoardTile extends AnchorPane implements Initializable {
    @FXML
    private Label letter;
    @FXML
    private Label multiplier;
    @FXML
    private Label worth;

    TileType tileType;
    private boolean confirmed = true;

    public BoardTile(TileType tileType) {
        this();

        this.setTileType(tileType);
    }

    public BoardTile() {
        super();

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/controls/gameboard/BoardTile.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTileType(TileType tileType) {
        this.tileType = tileType;

        this.multiplier.setText(tileType == TileType.NORMAL ? "" : tileType.value);

        this.updateBackgroundColor();
    }

    public TileType getTileType() {
        return this.tileType;
    }

    private void updateBackgroundColor() {
        Color color = this.getTileType().color;

        if (!this.letter.getText().isEmpty()) {
            color = Color.rgb(255, 255, 255);
            if (!this.isConfirmed()) {
                color = Color.rgb(244, 230, 167);
            }
        }

        this.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 6;", Colors.toRGBCode(color)));
    }

    public boolean isConfirmed() {
        return this.confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setLetter(char letter, int worth) {
        this.letter.setText(String.valueOf(letter));
        this.worth.setText(String.valueOf(worth));

        this.updateBackgroundColor();
    }

    public boolean hasLetter() {
        return !this.letter.getText().isEmpty();
    }

    public char getLetter() {
        return this.letter.getText().charAt(0);
    }

    public int getWorth() {
        String worth = this.worth.getText();
        return Integer.parseInt(worth.isEmpty() ? "0" : worth);
    }

    public enum TileType {
        NORMAL("--", Color.rgb(27, 23, 68)),
        START("*", Color.rgb(237, 17, 147)),
        TWOLETTER("2L", Color.rgb(45, 171, 225)),
        THREEWORD("3W", Color.rgb(237, 17, 147)),
        FOURLETTER("4L", Color.rgb(42, 77, 154)),
        FOURWORD("4W", Color.rgb(242, 102, 35)),
        SIXLETTER("6L", Color.rgb(11, 149, 68));

        private final String value;
        private final Color color;

        TileType(String value, Color color) {
            this.value = value;
            this.color = color;
        }

        public static TileType fromDatabase(String databaseValue) {
            for (TileType tileType : values()) {
                if (tileType.value.equals(databaseValue)) {
                    return tileType;
                }
            }
            return null;
        }

        public String getValue() {
            return this.value;
        }

        public Color getColor() {
            return this.color;
        }
    }
}
