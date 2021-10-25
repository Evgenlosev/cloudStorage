import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private Scene authScene;
    private Scene storageScene;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent authParent = FXMLLoader.load(getClass().getResource("auth.fxml"));
        authScene = new Scene(authParent);
        primaryStage.setScene(authScene);
        primaryStage.setTitle("Авторизация");
        primaryStage.show();
        primaryStage.setMinHeight(primaryStage.getHeight());
        primaryStage.setMinWidth(primaryStage.getWidth());
    }
    
}
