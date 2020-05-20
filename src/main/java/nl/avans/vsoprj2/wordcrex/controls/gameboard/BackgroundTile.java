package nl.avans.vsoprj2.wordcrex.controls.gameboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import nl.avans.vsoprj2.wordcrex.models.Board;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BackgroundTile extends AnchorPane implements Initializable {
    @FXML
    private Label color;

    public BackgroundTile(Board.TileType tileType) {
        this();

        this.setTileType(tileType);
    }

    public BackgroundTile() {
        super();

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/controls/gameboard/BackgroundTile.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTileType(Board.TileType tileType) {
        switch (tileType) {
            case START:
                this.color.setStyle("-fx-background-color: #ed1193;");
                this.color.setText("*");
                break;
            case TWOLETTER:
                this.color.setStyle("-fx-background-color: #2dabe1;");
                this.color.setText("2L");
                break;
            case THREEWORD:
                this.color.setStyle("-fx-background-color: #ed1193;");
                this.color.setText("3W");
                break;
            case FOURLETTER:
                this.color.setStyle("-fx-background-color: #2a4d9a;");
                this.color.setText("4L");
                break;
            case FOURWORD:
                this.color.setStyle("-fx-background-color: #f26623;");
                this.color.setText("4W");
                break;
            case SIXLETTER:
                this.color.setStyle("-fx-background-color: #0b9544;");
                this.color.setText("6L");
                break;
            default:
                this.color.setStyle("-fx-background-color: #1b1744;");
                this.color.setText("");
                break;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
