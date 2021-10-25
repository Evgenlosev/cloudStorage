package io;

import io.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.ServerSocket;
import java.net.Socket;


public class IoServer {

    //private static Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {

        try (ServerSocket server = new ServerSocket(8189)) {
            //LOG.debug("Server started...");
            while (true) {
                Socket socket = server.accept();
                //LOG.debug("Client accepted...");
                Handler handler = new Handler(socket);
                new Thread(handler).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
