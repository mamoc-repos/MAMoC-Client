package uk.ac.standrews.cs.mamoc_client.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {

    static final ArrayList<String> ignoredLibs = new ArrayList<>();

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
    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET
    })
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

    private static int getNextFreePort() {
        int localPort = -1;
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            localPort = socket.getLocalPort();
            if (!socket.isClosed()){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v("Port", Build.MANUFACTURER + " asked for a port: " + localPort);
        return localPort;
    }

    public static boolean isWifiConnected(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            return (cm != null) && (cm.getActiveNetworkInfo() != null) &&
                    (cm.getActiveNetworkInfo().getType() == 1);
    }

    public static boolean checkPermission(String strPermission, Context _c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(_c, strPermission);
            return result == PackageManager.PERMISSION_GRANTED;
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
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    private static void savePort(Context context, String localport, int localPort) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(localport, localPort);
        editor.apply();
    }

    public static String getValue(Context cxt, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cxt);
        return prefs.getString(key, null);
    }

    public static int getInt(Context cxt, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cxt);
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
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
        prefsEditor.clear().apply();
    }

    public static String readFile(Context context, String fileName)
    {
    //    String path = context.getApplicationContext().getFilesDir().getAbsolutePath();
        String myData = "";
        File myExternalFile = new File(fileName);
        try {
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            while ((strLine = br.readLine()) != null) {
                myData = myData + strLine + "\n";
            }
            br.close();
            in.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myData;
    }

    private static void loadIgnoredLibs(Context context) {
        String ignoredList = "ignored.list";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(ignoredList)));
            String mLine = reader.readLine().trim();
            while (mLine != null) {
                mLine = mLine.trim();
                if (mLine.length() != 0) {
                    ignoredLibs.add(StringUtils.toClassName(mLine));
                }
                mLine = reader.readLine();
            }
        } catch (IOException e) {
            Log.d("ignored", e.getLocalizedMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d("ignored", e.getLocalizedMessage());

                }
            }
        }
    }

    private static boolean isIgnored(String className) {
        for (String ignoredClass : ignoredLibs) {
            if (className.startsWith(ignoredClass)) {
                return true;
            }
        }
        return false;
    }

    public static String getDottedDecimalIP(byte[] ipAddr) {

        /*
         * ripped from:
         * http://stackoverflow.com/questions/10053385/how-to-get-each-devices-ip-address-in-wifi-direct-scenario
         *
         * */

        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }

    public static String getWiFiIPAddress(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int ipAddr = wm.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddr = Integer.reverseBytes(ipAddr);
        }
        
        byte[] ipByteArray = BigInteger.valueOf(ipAddr).toByteArray();

        return  getDottedDecimalIP(ipByteArray);
    }
}
