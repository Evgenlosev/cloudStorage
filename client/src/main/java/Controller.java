import com.geekbrains.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class Controller implements Initializable {

    public ListView<String> clientListView;
    public ListView<String> serverListView;
    public Button buttonToServer;
    public Button buttonToClient;
    public TextField clienPath;
    public TextField serverPath;
    private Path currentDir;

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            currentDir = Paths.get("client", "root");
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            refreshClientView();
            addNavigationListeners();

            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        Command command = (Command) is.readObject();
                        switch (command.getType()) {
                            case LIST_RESPONSE:
                                ListResponse response = (ListResponse) command;
                                List<String> names = response.getNames();
                                refreshServerView(names);
                                log.debug("Updated the list of files on the server");
                                break;
                            case PATH_RESPONSE:
                                PathResponse pathResponse = (PathResponse) command;
                                String path = pathResponse.getPath();
                                Platform.runLater(() -> serverPath.setText(path));
                                log.debug("Updated the server path");
                                break;
                            case FILE_MESSAGE:
                                FileMessage message = (FileMessage) command;
                                Files.write(currentDir.resolve(message.getName()), message.getBytes());
                                refreshClientView();
                                log.debug("File received: {}", message.getName());
                                break;
                        }
                    }
                }catch (Exception e) {
                    log.error("Problem with reading of the input command");
                }

            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshServerView(List<String> names) {
        Platform.runLater(() -> {
            serverListView.getItems().clear();
            serverListView.getItems().addAll(names);
        });
    }

    private void addNavigationListeners() {
        clientListView.setOnMouseClicked(e ->{
            if (e.getClickCount() == 2) {
                String item = clientListView.getSelectionModel().getSelectedItem();
                Path newPath = currentDir.resolve(item);
                if (Files.isDirectory(newPath)) {
                    currentDir = newPath;
                    try {
                        refreshClientView();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        serverListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = serverListView.getSelectionModel().getSelectedItem();
                try {
                    os.writeObject(new PathInRequest(item));
                    os.flush();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    private void refreshClientView() throws IOException {
        clienPath.setText(currentDir.toString());
        List<String> names = Files.list(currentDir)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(() -> {
            clientListView.getItems().clear();
            clientListView.getItems().addAll(names);
        });
    }

    public void fileToServer(ActionEvent actionEvent) throws IOException {
        String fileName = clientListView.getSelectionModel().getSelectedItem();
        FileMessage message = new FileMessage(currentDir.resolve(fileName));
        os.writeObject(message);
        os.flush();
    }

    public void fileToClient(ActionEvent actionEvent) throws IOException {
        String fileName = serverListView.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileName));
        os.flush();
    }

    public void clientPathUp(ActionEvent actionEvent) throws IOException {
        if (currentDir.getParent() != null) {
            currentDir = currentDir.getParent();
        }
        clienPath.setText(currentDir.toString());
        refreshClientView();
    }

    public void serverPathUp(ActionEvent actionEvent) throws IOException {
        os.writeObject(new PathUpRequest());
        os.flush();
    }
}
