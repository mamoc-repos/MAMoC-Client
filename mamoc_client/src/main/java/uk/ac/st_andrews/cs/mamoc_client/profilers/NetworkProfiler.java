package uk.ac.st_andrews.cs.mamoc_client.profilers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static uk.ac.st_andrews.cs.mamoc_client.Constants.PING;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.PONG;

public class NetworkProfiler {

    public static final String TAG = "DeviceProfiler";

    private final Context context;

    private static final int rttPings = 5;
    public static final int rttInfinite = 100000000;
    public static int rtt = rttInfinite;

    public NetworkProfiler(Context context) {
        this.context = context;
    }

    public static void measureRtt(String serverIp, int serverPort) {
        Socket clientSocket = new Socket();
        try {
            clientSocket.connect(new InetSocketAddress(serverIp, serverPort), 1000);

            try (OutputStream os = clientSocket.getOutputStream();
                 InputStream is = clientSocket.getInputStream();
                 DataInputStream dis = new DataInputStream(is)) {

                rttPing(is, os);
            } catch (IOException e) {
                Log.w(TAG, "Could not connect with server for measuring the RTT: " + e);
            }
        } catch (IOException e) {
            Log.d(TAG, "Could not connect to server for RTT measuring: " + e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "Could not close socket on RTT measuring: " + e);
            }
        }
    }

    /**
     * Doing a few pings on a given connection to measure how big the RTT is between the client and
     * the remote machine
     *
     * @param in
     * @param out
     * @return
     */
    private static int rttPing(InputStream in, OutputStream out) {
        Log.d(TAG, "Pinging");
        int tRtt = 0;
        int response;
        try {
            for (int i = 0; i < rttPings; i++) {
                Long start = System.nanoTime();
                Log.d(TAG, "Send Ping");
                out.write(PING);

                Log.d(TAG, "Read Response");
                response = in.read();
                if (response == PONG)
                    tRtt = (int) (tRtt + (System.nanoTime() - start) / 2);
                else {
                    Log.d(TAG, "Bad Response to Ping - " + response);
                    tRtt = rttInfinite;
                }
            }
            rtt = tRtt / rttPings;
            Log.d(TAG, "Ping - " + rtt / 1000000 + "ms");

        } catch (Exception e) {
            Log.e(TAG, "Error while measuring RTT: " + e);
            rtt = rttInfinite;
        }
        return rtt;
    }

    public final boolean isWifiEnabled() {
        boolean wifiState = false;

        WifiManager wifiManager =
                (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            wifiState = wifiManager.isWifiEnabled();
        }
        return wifiState;
    }

    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET
    })
    public final boolean isNetworkAvailable() {
        if (hasPermission(context, Manifest.permission.INTERNET)
                && hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                return netInfo != null && netInfo.isConnected();
            }
        }
        return false;
    }

    public final String getIPv4Address() {
        String result = null;
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase(Locale.getDefault());
                        boolean isIPv4 = addr instanceof Inet4Address;
                        if (isIPv4) {
                            result = sAddr;
                        }
                    }
                }
            }
        } catch (SocketException e) {
                Log.e("network", e.getLocalizedMessage());
        }

        return result;
    }

    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET
    })
    @NetworkType
    public final int getNetworkType() {
        int result = NetworkType.UNKNOWN;
        if (hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork == null) {
                    result = NetworkType.UNKNOWN;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                        || activeNetwork.getType() == ConnectivityManager.TYPE_WIMAX) {
                    result = NetworkType.WIFI_WIFIMAX;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    TelephonyManager manager =
                            (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                    if (manager != null && manager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                        switch (manager.getNetworkType()) {

                            // Unknown
                            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                                result = NetworkType.CELLULAR_UNKNOWN;
                                break;
                            // Cellular Data 2G
                            case TelephonyManager.NETWORK_TYPE_EDGE:
                            case TelephonyManager.NETWORK_TYPE_GPRS:
                            case TelephonyManager.NETWORK_TYPE_CDMA:
                            case TelephonyManager.NETWORK_TYPE_IDEN:
                            case TelephonyManager.NETWORK_TYPE_1xRTT:
                                result = NetworkType.CELLULAR_2G;
                                break;
                            // Cellular Data 3G
                            case TelephonyManager.NETWORK_TYPE_UMTS:
                            case TelephonyManager.NETWORK_TYPE_HSDPA:
                            case TelephonyManager.NETWORK_TYPE_HSPA:
                            case TelephonyManager.NETWORK_TYPE_HSPAP:
                            case TelephonyManager.NETWORK_TYPE_HSUPA:
                            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                                result = NetworkType.CELLULAR_3G;
                                break;
                            // Cellular Data 4G
                            case TelephonyManager.NETWORK_TYPE_LTE:
                                result = NetworkType.CELLULAR_4G;
                                break;
                            // Cellular Data Unknown Generation
                            default:
                                result = NetworkType.CELLULAR_UNIDENTIFIED_GEN;
                                break;
                        }
                    }
                }
            }
        }
        return result;
    }

    boolean hasPermission(final Context context, final String permission) {
        boolean permGranted =
                context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        if (!permGranted) {
            Log.e(TAG, ">\t" + permission);
            Log.w(TAG,
                    "\nPermission not granted/missing!\nMake sure you have declared the permission in your manifest file (and granted it on API 23+).\n");
        }
        return permGranted;
    }
}
