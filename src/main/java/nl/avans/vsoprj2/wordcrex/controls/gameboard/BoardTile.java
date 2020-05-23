package nl.avans.vsoprj2.wordcrex.controls.gameboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import nl.avans.vsoprj2.wordcrex.Colors;
import nl.avans.vsoprj2.wordcrex.models.Tile;

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

    Tile tile;

    public BoardTile(Tile tile) {
        this();

        this.setTile(tile);
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

    public void setTile(Tile tile) {
        this.tile = tile;

        this.multiplier.setText(this.tile.getTileType() == Tile.TileType.NORMAL ? "" : this.tile.getTileType().getValue());
        this.letter.setText(this.tile.hasLetter() ? this.tile.getLetter().toString() : "");
        this.worth.setText(this.tile.hasWorth() ? this.tile.getWorth().toString() : "");

        Color color = this.getTile().getTileType().getColor();

        if (this.tile.hasLetter()) {
            color = Color.rgb(255, 255, 255);
            if (!this.tile.isConfirmed()) {
                color = Color.rgb(145,242,129);
            }
            if (this.tile.isHighlighted()) {
                color = Color.rgb(250, 235, 182);
            }
        }

        this.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 6;", Colors.toRGBCode(color)));
    }

    public Tile getTile() {
        return this.tile;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
