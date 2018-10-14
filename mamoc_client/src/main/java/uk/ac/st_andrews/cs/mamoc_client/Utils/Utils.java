package uk.ac.st_andrews.cs.mamoc_client.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;

public class Utils {

    public static void alert(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4   true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }


    public static int getPort(Context context){
        int localPort = Utils.getInt(context, "localport");
        if (localPort < 0){
           localPort = getNextFreePort();
           Utils.savePort(context, "localport", localPort);
        }
        return localPort;
    }

    public static int getNextFreePort() {
        int localPort = -1;
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            localPort = socket.getLocalPort();
            if (socket != null && !socket.isClosed()){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v("Port", Build.MANUFACTURER + " asked for a port: " + localPort);
        return localPort;
    }

    public static boolean isWifiConnected(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

            return (cm != null) && (cm.getActiveNetworkInfo() != null) &&
                    (cm.getActiveNetworkInfo().getType() == 1);
    }

    public static boolean checkPermission(String strPermission, Context _c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(_c, strPermission);
            if (result == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static void requestPermission(String strPermission, int perCode, Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, strPermission)) {
            Toast.makeText(activity, "GPS permission allows us to access location data." +
                            " Please allow in App Settings for additional functionality.",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{strPermission}, perCode);
        }
    }

    public static void save(Context context, String key, String value){
        SharedPreferences.Editor prefsEditor = context.getSharedPreferences("kkd", Context.MODE_PRIVATE).edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    private static void savePort(Context context, String localport, int localPort) {
        SharedPreferences.Editor editor = context.getSharedPreferences("kkd", Context.MODE_PRIVATE).edit();
        editor.putInt(localport, localPort);
        editor.apply();
    }

    public static String getValue(Context cxt, String key) {
        SharedPreferences prefs = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public static int getInt(Context cxt, String key) {
        SharedPreferences prefs = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE);
        return prefs.getInt(key, -1);
    }

    public static boolean copyFile(InputStream is, OutputStream os) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            os.close();
            is.close();
        } catch (IOException e) {
            Log.d("DDDDX", e.toString());
            return false;
        }
        return true;
    }

    public static byte[] getInputStreamByteArray(InputStream inputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        try {
            while ((len = inputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return (baos.toByteArray());
    }

    public static void clearPreferences(Context cxt) {
        SharedPreferences.Editor prefsEditor = cxt.getSharedPreferences("kkd", Context
                .MODE_PRIVATE).edit();
        prefsEditor.clear().commit();
    }

}
