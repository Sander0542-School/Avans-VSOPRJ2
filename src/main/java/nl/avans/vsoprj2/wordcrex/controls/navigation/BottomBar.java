package nl.avans.vsoprj2.wordcrex.controls.navigation;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import nl.avans.vsoprj2.wordcrex.Singleton;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BottomBar extends HBox implements Initializable {

    @FXML
    private BottomBarItem observer;

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
                        BottomBar.this.barItemClicked(event);
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
        if (this.barItemEventHandler != null) {
            this.barItemEventHandler.handle(event);
        }
    }

    public void setOnBarItemClicked(EventHandler eventHandler) {
        this.barItemEventHandler = eventHandler;
    }

    public EventHandler getOnBarItemClicked() {
        return this.barItemEventHandler;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.observer.setVisible(Singleton.getInstance().getUser().getRole().equalsIgnoreCase("observer"));
    }
}
