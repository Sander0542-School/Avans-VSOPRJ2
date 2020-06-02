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

    private EventHandler<MouseEvent> backButtonEventHandler;
    private EventHandler<MouseEvent> deleteButtonEventHandler;
    private EventHandler<ActionEvent> optionsMenuEventHandler;

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

    public void handleBackButton(MouseEvent event) {
        if (this.backButtonEventHandler != null) {
            this.backButtonEventHandler.handle(event);
        }
    }

    public void handleDeleteButton(MouseEvent event) {
        if (this.deleteButtonEventHandler != null) {
            this.deleteButtonEventHandler.handle(event);
        }
    }

    public void handleOptionsButton(MouseEvent event) {
        this.optionsMenu.show(this, event.getScreenX(), event.getScreenY());
    }

    public void setOnBackButtonEvent(EventHandler<MouseEvent> eventHandler) {
        this.backButtonEventHandler = eventHandler;
    }

    public EventHandler<MouseEvent> getOnBackButtonEvent() {
        return this.backButtonEventHandler;
    }

    public void setOnDeleteButtonEvent(EventHandler<MouseEvent> eventHandler) {
        this.deleteButtonEventHandler = eventHandler;
    }

    public EventHandler<MouseEvent> getOnDeleteButtonEvent() {
        return this.deleteButtonEventHandler;
    }

    public void setOnOptionsMenuEvent(EventHandler<ActionEvent> eventHandler) {
        this.optionsMenuEventHandler = eventHandler;
    }

    public EventHandler<ActionEvent> getOnOptionsMenuEvent() {
        return this.optionsMenuEventHandler;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.optionsMenu.getItems().addAll(new MenuItem("Info"), new MenuItem("Instellingen"));

        for (MenuItem item : this.optionsMenu.getItems()) {
            item.setId(item.getText().toLowerCase());
            item.setOnAction(actionEvent -> {
                if (AppBar.this.optionsMenuEventHandler != null) {
                    AppBar.this.optionsMenuEventHandler.handle(actionEvent);
                }
            });
        }
    }
}
