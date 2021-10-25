package com.geekbrains;

public class AuthResponse extends Command{

    private boolean authOk = false;

    public void setAuthOk(boolean authOk) {
        this.authOk = authOk;
    }

    public boolean getAuthOk() {
        return authOk;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_RESPONSE;
    }
}
