package nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class Server {
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private ByteBuffer buffer;
    private static Path ROOT = Paths.get("server", "root");

    public Server() throws IOException {
        buffer = ByteBuffer.allocate(256);
        serverChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverChannel.bind(new InetSocketAddress(8189));
        log.debug("Server started");
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (serverChannel.isOpen()) {
            selector.select();

            Set<SelectionKey> keys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }

        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        buffer.clear();
        int read = 0;
        StringBuilder msg = new StringBuilder();
        while (true) {
            if (read == -1) {
                channel.close();
                return;
            }
            read = channel.read(buffer);
            if (read == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                msg.append((char) buffer.get());
            }
            buffer.clear();

        }
        String message = msg.toString().trim();
        log.debug("recieved: {}", message);
        if (message.equals("ls")) {
            channel.write(ByteBuffer.wrap(getFilesInfo().getBytes(StandardCharsets.UTF_8)));
        } else if (message.startsWith("cat")) {
            try {
                String filename = message.split(" ")[1];
                channel.write(ByteBuffer.wrap(getFileDataAsString(filename).getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                channel.write(ByteBuffer.wrap("Command cat should have only two arguments\n".getBytes(StandardCharsets.UTF_8)));
            }
        } else {
            channel.write(ByteBuffer.wrap("Wrong command. Use cat fileName or ls".getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        log.debug("Client Accepted");

    }

    private String getFilesInfo() throws IOException {
        return Files.list(ROOT).map(this::resolveFilesType)
                .collect(Collectors.joining("\n")) + "\n";
    }

    private String resolveFilesType(Path path) {
        if (Files.isDirectory(path)) {
            return "[DIR]\t" + path.getFileName().toString();
        } else {
            return "[FILE]\t" + path.getFileName().toString();
        }
    }

    private String getFileDataAsString(String filename) throws IOException {
       if (Files.isDirectory(ROOT.resolve(filename))) {
           return "Cannot be applied to " + filename + "\n";
       } else {
           return new String(Files.readAllBytes(ROOT.resolve(filename))) + "\n";
       }
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }

}
