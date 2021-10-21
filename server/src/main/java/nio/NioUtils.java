package nio;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

@Slf4j
public class NioUtils {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("server","root");
        System.out.println(Files.exists(path));
        System.out.println(Files.size(path));

        System.out.println(path.toAbsolutePath());
        Path copy = path.resolve("var.txt");
        System.out.println(Files.exists(copy));
        WatchService watchService = FileSystems.getDefault().newWatchService();

        new Thread(() -> {
            while (true) {
                WatchKey key = null;
                try {
                    key = watchService.take();
                    if (key.isValid()) {
                        List<WatchEvent<?>> events = key.pollEvents();
                        for (WatchEvent<?> event : events) {
                            log.debug("kind {}, context {}", event.kind(), event.context());
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                key.reset();
            }
        }).start();

        path.register(watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);

        Files.write(copy, "My message".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

        Files.copy(copy, Paths.get("server","root", "2.txt"), StandardCopyOption.REPLACE_EXISTING);

        Files.walk(path).forEach(System.out::println);
    }

}
