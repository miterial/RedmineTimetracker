package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.taskadapter.redmineapi.RedmineException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.sound.midi.Track;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class LoginController implements Initializable {

    @FXML
    ImageView imgLogo;
    @FXML
    JFXButton btnLogin;
    @FXML
    JFXPasswordField passwordField;
    @FXML
    JFXTextField usernameField;
    @FXML
    Label errorMsg;

    Stage stage;

    TrackerController trackerController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        Image img = new Image("logo-full.png");
        imgLogo.setImage(img);
        imgLogo.setCache(true);

        btnLogin.setOnAction(actionEvent -> {

            btnLogin.setDisable(true);
            btnLogin.setText("Loading...");
            errorMsg.setVisible(false);

            // TODO: make tray open same window that was closed
                /*javax.swing.SwingUtilities.invokeLater(this::createTrayIcon);

                Platform.setImplicitExit(false);*/

            Task<Parent> loadTask = new Task<Parent>() {
                @Override
                public Parent call() throws IOException, InterruptedException {

                    FXMLLoader loader;
                    loader = new FXMLLoader(getClass().getResource("/tracker/tracker.fxml"));

                    try {
                        //trackerController = new TrackerController(usernameField.getText(), passwordField.getText());
                        trackerController = new TrackerController("user", "user");  //todo: get credentials from external file
                    } catch (RedmineException e) {
                        log.error("Incorrect login/password");
                        e.printStackTrace();
                        throw new InterruptedException();
                    }

                    loader.setController(trackerController);

                    return loader.load();
                }
            };

            loadTask.setOnSucceeded(e -> {
                stage = new Stage();
                stage.setTitle("Redmine Timetracker");

                Parent root = loadTask.getValue();
                Scene scene = new Scene(root);
                scene.getStylesheets().addAll(
                        "bootstrapfx.css", "style.css"); // set stylesheets, через запятую
                stage.setScene(scene);

                stage.getIcons().add(new Image(TrackerController.class.getResourceAsStream( "/logo-full.png" )));

                stage.show();
                stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
                root.requestFocus();

                ((Node) (actionEvent.getSource())).getScene().getWindow().hide();
            });

            loadTask.setOnFailed(e -> {
                errorMsg.setVisible(true);
                btnLogin.setText("LOGIN");
                btnLogin.setDisable(false);
            });

            Thread thread = new Thread(loadTask);
            thread.start();
        });

    }

    private void closeWindowEvent(WindowEvent windowEvent) {

        if(trackerController.isTracking()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setTitle("Warning");
            alert.setContentText("You have to stop tracker before closing");

            alert.showAndWait();
        }
        else Platform.exit();

        windowEvent.consume();
    }

}
