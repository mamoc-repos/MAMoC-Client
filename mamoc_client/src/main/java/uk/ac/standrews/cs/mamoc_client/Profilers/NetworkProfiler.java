package uk.ac.standrews.cs.mamoc_client.Profilers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import static uk.ac.standrews.cs.mamoc_client.Constants.PING;
import static uk.ac.standrews.cs.mamoc_client.Constants.PONG;

public class NetworkProfiler {

    public static final String TAG = "NetworkProfiler";

    private final Context context;

    private static final int rttPings = 5;
    private static final int rttInfinite = 100000000;

    public NetworkProfiler(Context context) {
        this.context = context;
    }

    public int measureRtt(String serverIp, int serverPort) {
        Socket clientSocket = new Socket();
        try {
            clientSocket.connect(new InetSocketAddress(serverIp, serverPort), 1000);

            try (OutputStream os = clientSocket.getOutputStream();
                 InputStream is = clientSocket.getInputStream();
                 DataInputStream dis = new DataInputStream(is)) {

                return rttPing(is, os);
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

        return -1;
    }

    private int rttPing(InputStream in, OutputStream out) {
        Log.d(TAG, "Pinging");
        int tRtt = 0;
        int response;
        int rtt;
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

    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET
    })
    public final NetworkType getNetworkType() {
        NetworkType result = NetworkType.UNKNOWN;
        if (hasPermission(context)) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork == null) {
                    result = NetworkType.UNKNOWN;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                        || activeNetwork.getType() == ConnectivityManager.TYPE_WIMAX) {
                    result = NetworkType.WIFI;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    TelephonyManager manager =
                            (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                    if (manager != null && manager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                        switch (manager.getNetworkType()) {

                            // Unknown
                            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                                result = NetworkType.CELLULAR_UNKNOWN;
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
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean hasPermission(final Context context) {
        boolean permGranted =
                context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
        if (!permGranted) {
            Log.e(TAG, ">\t" + Manifest.permission.ACCESS_NETWORK_STATE);
            Log.w(TAG,
                    "\nPermission not granted/missing!\nMake sure you have declared the permission in your manifest file (and granted it on API 23+).\n");
        }
        return permGranted;
    }

    public float getWiFiSpeed(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return getWifiNetworkSpeed(wifiInfo);
    }

    /**
     * Return general class of wifi network type. Unfortunately, there is no Android API method
     * to do this, link speed in {@link WifiInfo#LINK_SPEED_UNITS "Mbps"} must be used
     * and a maximum speed of wifi class must be compared with the value returned from
     * {@link WifiInfo#getLinkSpeed()}.
     *
     * @param wifiInfo
     * @return network speed by one of the WIFI_DRAFT_X type
     */
    private float getWifiNetworkSpeed(WifiInfo wifiInfo) {
        if (wifiInfo == null) {
            return 0;
        }
        int linkSpeed = wifiInfo.getLinkSpeed(); //measured using WifiInfo.LINK_SPEED_UNITS
        return linkSpeed;
    }

    public float mobileNetSpeed(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = tm.getNetworkType();
        float netSpeed = getMobileNetworkSpeed(networkType);
        return netSpeed;
    }

    /**
     * Return hypothetical speed of mobile network. This method is an equivalent
     * of {@link TelephonyManager#getNetworkClass()}
     *
     * @param networkType
     * @return network speed by one of the XG type
     */
    private float getMobileNetworkSpeed(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return 114;
            case 16: // TelephonyManager.NETWORK_TYPE_GSM:
                return 0;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return 296;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return 115;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return 153;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return 60;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return 384;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return 2.46F;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return 3.1F;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return 21.6F;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return 5.76F;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return 14;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return 4.9F;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return 1.285F;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return 42;
            case 17: // TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return 0;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return 100;
            case 18: // TelephonyManager.NETWORK_TYPE_IWLAN:
                return 0;
            default:
                return 0;
        }
    }
}
