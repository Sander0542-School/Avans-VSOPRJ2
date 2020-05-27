package nl.avans.vsoprj2.wordcrex.controls.gameboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
    private LetterTile letterTile;
    private boolean selected;

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

    public void setSize(double size) {
        this.setPrefWidth(size);
        this.setPrefHeight(size);

        this.multiplier.setFont(Font.font(size * 0.375));
        this.letter.setFont(Font.font(size * 0.5));
        this.worth.setFont(Font.font(size * 0.222));
        this.worth.setPadding(new Insets(size * 0.416, 0, 0, size * 0.583));
    }

    public void setTile(Tile tile) {
        this.tile = tile;

        this.multiplier.setText(this.tile.getTileType() == Tile.TileType.NORMAL ? "" : this.tile.getTileType().getValue());
        this.letter.setText(this.tile.hasLetter() ? this.tile.getLetter().toString() : "");
        this.worth.setText(this.tile.hasWorth() ? this.tile.getWorth().toString() : "");

        this.updateBackgroundColor();
    }

    public void setLetterTile(LetterTile letterTile) {
        this.letterTile = letterTile;

        if (this.letterTile != null) {
            this.letter.setText(letterTile.getLetter().getSymbol());
            this.worth.setText(String.valueOf(letterTile.getLetter().getValue()));
        } else {
            this.setTile(this.tile);
            this.setSelected(false);
        }

        this.updateBackgroundColor();
    }

    public LetterTile getLetterTile() {
        return this.letterTile;
    }

    public void updateBackgroundColor() {
        Color color = this.getTile().getTileType().getColor();

        this.multiplier.setVisible(true);

        if (this.tile.hasLetter()) {
            color = Color.rgb(255, 255, 255);
            if (this.tile.isHighlighted()) {
                color = Color.rgb(145, 242, 129);
            }
            this.multiplier.setVisible(false);
        }
        if (this.letterTile != null) {
            color = Color.rgb(250, 235, 182);
            if (this.isSelected()) {
                color = Color.rgb(156, 147, 106);
            }
            this.multiplier.setVisible(false);
        }

        this.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 6;", Colors.toRGBCode(color)));
    }

    public void setSelected(boolean selected) {
        this.selected = selected;

        this.updateBackgroundColor();
    }

    public boolean isSelected() {
        return this.selected;
    }

    public Tile getTile() {
        return this.tile;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public String getLetter() {
        if (this.getTile().hasLetter()) {
            return this.getTile().getLetter().toString();
        }
        if (this.getLetterTile() != null) {
            return this.getLetterTile().getLetter().getSymbol();
        }

        return null;
    }
}
