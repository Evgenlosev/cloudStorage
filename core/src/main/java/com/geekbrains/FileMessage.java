package com.geekbrains;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends Command {
    private final String name;
    private final long size;
    private final byte[] bytes;

    public FileMessage(Path path) throws IOException {
        name = path.getFileName().toString();
        size = Files.size(path);
        bytes = Files.readAllBytes(path);
    }

    public String getName() {
        return name;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public long getSize() {
        return size;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_MESSAGE;
    }
}
