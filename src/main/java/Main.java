import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import controller.LoginController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main extends Application {

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        log.debug("Starting");
        this.primaryStage = primaryStage;

        //set controller to Login form
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login/login.fxml"));
        LoginController controller = new LoginController();
        loader.setController(controller);

        Pane root = loader.load();
        this.primaryStage.setTitle("Redmine Timetracker");
        Scene scene = new Scene(root);

        // add bootstrap lib
        scene.getStylesheets().addAll(
                "bootstrapfx.css"); // set stylesheets, через запятую

        this.primaryStage.getIcons().add(new Image(getClass().getResourceAsStream( "/logo-full.png" )));
        this.primaryStage.setScene(scene);
        this.primaryStage.show();
    }
}
