package nl.avans.vsoprj2.wordcrex.controls.navigation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

    private ContextMenu optionsMenu = new ContextMenu();

    private EventHandler backButtonEventHandler;
    private EventHandler optionsMenuEventHandler;

    public AppBar() {
        super();

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/controls/navigation/AppBar.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTitle() {
        return this.titleLabel.getText();
    }

    public void setTitle(String value) {
        this.titleLabel.setText(value);
    }

    public boolean getBackButton() {
        return this.backButton.isVisible();
    }

    public void setBackButton(boolean visible) {
        this.backButton.setVisible(visible);
        this.backButton.setManaged(visible);
    }

    public boolean getOptionsButton() {
        return this.optionsButton.isVisible();
    }

    public void setOptionsButton(boolean visible) {
        this.optionsButton.setVisible(visible);
        this.optionsButton.setManaged(visible);
    }

    public boolean getDeleteButton() {
        return this.deleteButton.isVisible();
    }

    public void setDeleteButton(boolean visible) {
        this.deleteButton.setVisible(visible);
        this.deleteButton.setManaged(visible);
    }

    public void handleOptionsButton(MouseEvent event) {
        this.optionsMenu.show(this, event.getScreenX(), event.getScreenY());
    }

    public void handleBackButton(MouseEvent event) {
        if (this.backButtonEventHandler != null) {
            this.backButtonEventHandler.handle(event);
        }
    }

    public void setOnBackButtonEvent(EventHandler eventHandler) {
        this.backButtonEventHandler = eventHandler;
    }

    public EventHandler getOnBackButtonEvent() {
        return this.backButtonEventHandler;
    }

    public void setOnOptionsMenuEvent(EventHandler eventHandler) {
        optionsMenuEventHandler = eventHandler;
    }

    public EventHandler getOnOptionsMenuEvent() {
        return optionsMenuEventHandler;
    }

    public void deleteButtonClicked(MouseEvent event) {
        if (deleteButtonEventHandler != null) {
            deleteButtonEventHandler.handle(event);
        }
    }

    public EventHandler getOnDeleteButtonEvent() {
        return deleteButtonEventHandler;
    }

    public void setOnDeleteButtonEvent(EventHandler eventHandler) {
        deleteButtonEventHandler = eventHandler;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        optionsMenu.getItems().addAll(new MenuItem("Info"), new MenuItem("Settings"));

        for (MenuItem item : this.optionsMenu.getItems()) {
            item.setId(item.getText().toLowerCase());
            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    if (AppBar.this.optionsMenuEventHandler != null) {
                        AppBar.this.optionsMenuEventHandler.handle(actionEvent);
                    }
                }
            });
        }
    }
}
