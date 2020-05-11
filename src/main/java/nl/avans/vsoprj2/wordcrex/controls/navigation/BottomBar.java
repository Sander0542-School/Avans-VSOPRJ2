package nl.avans.vsoprj2.wordcrex.controls.navigation;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BottomBar extends HBox implements Initializable {

    private EventHandler barItemEventHandler = null;

    public BottomBar() {
        super();

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/controls/navigation/BottomBar.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(Node child : this.getChildren()) {
            if (child instanceof BottomBarItem) {
                child.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        barItemClicked(event);
                    }
                });
            }
        }
    }

    public String getActive() {
        for (Node child : this.getChildren()) {
            if (((BottomBarItem) child).isActive()) {
                return child.getId();
            }
        }

        return null;
    }

    public void setActive(String value) {
        for (Node child : this.getChildren()) {
            ((BottomBarItem) child).setActive(child.getId().equals(value));
        }
    }

    public void barItemClicked(MouseEvent event) {
        if (barItemEventHandler != null) {
            barItemEventHandler.handle(event);
        }
    }

    public void setOnBarItemClicked(EventHandler eventHandler) {
        barItemEventHandler = eventHandler;
    }

    public EventHandler getOnBarItemClicked() {
        return barItemEventHandler;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
