package io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Handler implements Runnable{

    private static final int BUFFER_SIZE = 256;
    private static final String ROOT_DIR = "server/root";
    private final byte[] BUFFER = new byte[BUFFER_SIZE];
    private final Socket socket;
    private static Logger LOG = LoggerFactory.getLogger(Handler.class);
    DataOutputStream os;
    DataInputStream is;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }


    @Override
    public void run() {
        try {
            os = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());
            while (true) {
                String fileName = is.readUTF();
                LOG.debug("Received fileName: {}", fileName);
                long size = is.readLong();
                LOG.debug("File size: {}", size);
                int read;
                try (OutputStream fos = Files.newOutputStream(Paths.get(ROOT_DIR, fileName))) {
                    for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                        read = is.read();
                        fos.write(BUFFER, 0, read);
                    }
                } catch (Exception e) {
                    LOG.error("problem with file system");
                }
                os.writeUTF("OK");
            }
        } catch (Exception e) {
            LOG.error("stacktrace: ", e);
            try {
                os.writeUTF("Failed");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        try {
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
