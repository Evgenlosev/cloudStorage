package com.geekbrains;

import java.io.Serializable;

public class Command implements Serializable {

    CommandType type;

    public CommandType getType() {
        return type;
    }

    public enum CommandType {
        FILE_MESSAGE,
        FILE_REQUEST,
        LIST_REQUEST,
        LIST_RESPONSE,
        PATH_IN_REQUEST,
        PATH_UP_REQUEST,
        PATH_RESPONSE
    }
}
