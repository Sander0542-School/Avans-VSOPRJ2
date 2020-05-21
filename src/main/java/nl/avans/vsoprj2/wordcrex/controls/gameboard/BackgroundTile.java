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
                this.setBackgroundColor("#ed1193");
                this.color.setText("*");
                break;
            case TWOLETTER:
                this.setBackgroundColor("#2dabe1");
                this.color.setText("2L");
                break;
            case THREEWORD:
                this.setBackgroundColor("#ed1193");
                this.color.setText("3W");
                break;
            case FOURLETTER:
                this.setBackgroundColor("#2a4d9a");
                this.color.setText("4L");
                break;
            case FOURWORD:
                this.setBackgroundColor("#f26623");
                this.color.setText("4W");
                break;
            case SIXLETTER:
                this.setBackgroundColor("#0b9544");
                this.color.setText("6L");
                break;
            default:
                this.setBackgroundColor("#1b1744");
                this.color.setText("");
                break;
        }
    }

    private void setBackgroundColor(String hexColor) {
        this.color.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 6;", hexColor));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
