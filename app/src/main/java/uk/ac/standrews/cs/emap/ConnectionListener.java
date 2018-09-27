package uk.ac.standrews.cs.emap;

import android.content.Context;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionListener extends Thread {

    private Context mContext;

    private boolean acceptRequests = true;

    public ConnectionListener(Context context, int port) throws IOException {
        this.mContext = context;
        ServerListener server = new ServerListener(port);
        server.start();
    }

    static class ServerListener extends Thread {
        private int mPort;
        private ServerSocket mServer;

        public ServerListener(int port) throws IOException {
            mServer = new ServerSocket(port);
            mServer.setReuseAddress(true);        }

        @Override
        public void run() {
            while (true) {

                try {

                    if (mServer != null && !mServer.isBound()){
                        mServer.bind(new InetSocketAddress(mPort));
                    }

                    final Socket socketToClient = mServer.accept();
                    ClientHandler clientHandler = new ClientHandler(socketToClient);
                    clientHandler.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class ClientHandler extends Thread{
        private Socket socket;
        ObjectInputStream inputStream;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            inputStream = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Object o = inputStream.readObject();
                    System.out.println("Read object: "+o);
                } catch (IOException e) {
                    e.printStackTrace();

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
