import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private static final String ROOT_DIR = "client/files";
    private static byte[] buffer = new byte[1024];
    public ListView<String> listView;
    public TextField input;
    private DataInputStream is;
    private DataOutputStream os;
    private static Logger LOG = LoggerFactory.getLogger(Controller.class);

    public void send(ActionEvent actionEvent) throws Exception {
        String fileName = input.getText();
        input.clear();
        sendFile(fileName);
    }

    private void sendFile(String fileName) throws IOException {
        Path file = Paths.get(ROOT_DIR, fileName);
        if (Files.exists(file)) {
            long size = Files.size(file);
            os.writeUTF(fileName);
            os.writeLong(size);
            InputStream fileStream = Files.newInputStream(file);
            int read;
            while ((read = fileStream.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        } else {
            os.writeUTF(fileName);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            fileInfo();
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        String msg = is.readUTF();
                        LOG.debug("received: {}", msg);
                        Platform.runLater(() -> input.setText(msg));
                    }
                } catch (Exception e){
                    LOG.error("Exeption while read from input stream");
                }

            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException e) {
            LOG.error("e = ", e);
        }
    }

    private void fileInfo() throws IOException {
        listView.getItems().clear();
        listView.getItems().addAll(
                Files.list(Paths.get(ROOT_DIR))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList())

        );
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = listView.getSelectionModel().getSelectedItem();
                input.setText(item);
            }
        });

    }
}
