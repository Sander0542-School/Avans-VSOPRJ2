package nl.avans.vsoprj2.wordcrex.controls.gameboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
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
    private LetterTile letterTile;
    private Character letterValue;

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

    public void setLetterTile(LetterTile letterTile) {
        this.letterTile = letterTile;
    }

    public LetterTile getLetterTile() {
        return this.letterTile;
    }

    public void setLetterValue(Character letterValue) {
        this.letterValue = letterValue;
    }

    public Character getLetterValue() {
        return this.letterValue;
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

    public void selectTile() {
        this.setStyle("-fx-background-color: #9C936A; -fx-background-radius: 6;");
    }

    public void deselectTile() {
        this.setStyle("-fx-background-color: #F4E6A7; -fx-background-radius: 6;");
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

    public void removeLetter() {
        this.letter.setText("");
        this.worth.setText("");
        this.updateBackgroundColor();
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
