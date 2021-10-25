import com.geekbrains.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

import static com.geekbrains.Command.CommandType.AUTH_RESPONSE;

@Slf4j
public class AuthController implements Initializable {

    public TextField loginField;
    public PasswordField passField;
    public Button authButton;

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        Command command = (Command) is.readObject();
                        if (command.getType() == AUTH_RESPONSE) {
                            AuthResponse authResponse = (AuthResponse) command;
                            if (authResponse.getAuthOk()) {
                                Stage stage = (Stage) loginField.getScene().getWindow();
                                Platform.runLater(()-> {
                                    try {
                                        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("storage.fxml"));
                                        Parent root = fxmlLoader.load();
                                        Controller controller = fxmlLoader.getController();
                                        stage.setTitle("Облачное хранилище");
                                        stage.setScene(new Scene(root));
                                    } catch (IOException ioException) {
                                        log.error("exception while trying to change scene");
                                    }
                                });
                            } else {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.WARNING, "Неверный логин или пароль",
                                            ButtonType.OK);
                                    alert.showAndWait();
                                });
                            }
                        }
                    }
                }catch (Exception e) {
                    log.error("Ошибка при получении ответа об авторизации");
                }

            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(ActiveEvent activeEvent) throws IOException {
        ((Stage)loginField.getScene().getWindow()).close();
        Stage stage = new Stage();
        Parent parent = FXMLLoader.load(getClass().getResource("storage.fxml"));
        Scene scene = new Scene(parent);

//        scene.getStylesheets().add(getClass().getResource("styles.css")).toExternalFrom());

        stage.setScene(scene);
        stage.show();

    }

    public void sendAuthReq(ActionEvent actionEvent) throws IOException {
        String login = loginField.getText();
        String pass = passField.getText();
        os.writeObject(new AuthRequest(login, pass));
        os.flush();
    }
}
