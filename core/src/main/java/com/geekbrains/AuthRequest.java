package com.geekbrains;

public class AuthRequest extends Command{

    private String login;
    private String pass;

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    public AuthRequest(String login, String pass) {
        this.login = login;
        this.pass = pass;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_REQUEST;
    }
}
