package uk.ac.st_andrews.cs.mamoc_client.Communication;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import uk.ac.st_andrews.cs.mamoc_client.Utils.Utils;

public class ConnectionListener extends Thread {

    private Context mContext;
    private int mPort;
    private ServerSocket mServer;

    private boolean acceptRequests = true;

    public ConnectionListener(Context context, int port) {
        this.mContext = context;
        this.mPort = port;
    }

    @Override
    public void run() {
        try {
            Log.v("ConnListener", Build.MANUFACTURER + ": conn listener: " + mPort);

            mServer = new ServerSocket(mPort);
            mServer.setReuseAddress(true);

            if (mServer != null && !mServer.isBound()) {
                mServer.bind(new InetSocketAddress(mPort));
            }

            Socket client = null;

            while (acceptRequests){
                client = mServer.accept();
                handleData(client.getInetAddress().getHostAddress(), client.getInputStream());
            }

            Log.v("ConnListener", "ConnListener Terminated");

            client.close();
            client = null;

        } catch (IOException e) {
            Log.v("ConnListener", Build.MANUFACTURER + ": ConnListener Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private void handleData(String hostAddress, InputStream inputStream) {
        byte[] input = Utils.getInputStreamByteArray(inputStream);
        ObjectInput oin;
        try {
            oin = new ObjectInputStream(new ByteArrayInputStream(input));
            ITransferable transferObject = (ITransferable) oin.readObject();

            new DataHandler(mContext, transferObject, hostAddress).process();

            oin.close();
            return;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void tearDown(){
        acceptRequests = false;
    }
}
