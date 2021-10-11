import com.geekbrains.FileMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    private static Logger LOG = LoggerFactory.getLogger(Controller.class);

    public void send(ActionEvent actionEvent) throws Exception {
        String fileName = input.getText();
        input.clear();
        sendFile(fileName);
    }

    private void sendFile(String fileName) throws IOException {
        Path file = Paths.get(ROOT_DIR, fileName);
        os.writeObject(new FileMessage(file));
        os.flush();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            fileInfo();
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        String msg = (String) is.readObject();
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
