package uk.ac.standrews.cs.mamoc_client.ServiceDiscovery;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import uk.ac.standrews.cs.mamoc_client.Utils.Utils;

public class ConnectionListener extends Thread {

    private final String TAG = "ConnectionListener";
    
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
            Log.d(TAG, Build.MANUFACTURER + ": conn listener: " + mPort);
            mServer = new ServerSocket(mPort);
            mServer.setReuseAddress(true);

            if (mServer != null && !mServer.isBound()) {
                mServer.bind(new InetSocketAddress(mPort));
            }

            Log.d(TAG, "Inet4Address: " + Inet4Address.getLocalHost().getHostAddress());

            Socket socket = null;
            while (acceptRequests) {
                // this is a blocking operation
                socket = mServer.accept();
                Log.d(TAG, socket.getInetAddress().getHostAddress());
                Log.d(TAG, socket.getInetAddress().getHostName());
                Log.d(TAG, socket.getInetAddress().getCanonicalHostName());

                handleData(socket.getInetAddress().getHostAddress(), socket.getInputStream());
            }
            Log.e(TAG, Build.MANUFACTURER + ": Connection listener terminated. " +
                    "acceptRequests: " + acceptRequests);
            socket.close();
            socket = null;

        } catch (IOException e) {
            Log.e(TAG, Build.MANUFACTURER + ": Connection listener EXCEPTION. " + e.toString());
            e.printStackTrace();
        }
    }

    private void handleData(String hostAddress, InputStream inputStream) {
        byte[] input = Utils.getInputStreamByteArray(inputStream);
        ObjectInput oin;
        try {
            oin = new ObjectInputStream(new ByteArrayInputStream(input));
            ITransferable transferObject = (ITransferable) oin.readObject();

            new DataHandler(mContext, hostAddress, transferObject).process();

            oin.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void tearDown(){
        acceptRequests = false;
    }
}
