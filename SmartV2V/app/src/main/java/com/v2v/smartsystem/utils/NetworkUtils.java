package com.v2v.smartsystem.utils;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Networking utility for local peer-to-peer V2V ad-hoc communication.
 * Handles broadcast address discovery and Wi-Fi MulticastLock management.
 */
public class NetworkUtils {

    private static WifiManager.MulticastLock multicastLock;

    /**
     * Acquires Android Wi-Fi MulticastLock to allow incoming UDP broadcast packets.
     */
    public static void acquireMulticastLock(Context context) {
        if (context == null) return;
        try {
            WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifi != null && (multicastLock == null || !multicastLock.isHeld())) {
                multicastLock = wifi.createMulticastLock("V2V_MulticastLock");
                multicastLock.setReferenceCounted(true);
                multicastLock.acquire();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Releases Wi-Fi MulticastLock.
     */
    public static void releaseMulticastLock() {
        try {
            if (multicastLock != null && multicastLock.isHeld()) {
                multicastLock.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Resolves the subnet broadcast address for the active Wi-Fi / Hotspot interface.
     * Defaults to 255.255.255.255 if interface broadcast is unavailable.
     */
    public static InetAddress getBroadcastAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast != null) {
                        return broadcast;
                    }
                }
            }
            return InetAddress.getByName("255.255.255.255");
        } catch (Exception e) {
            try {
                return InetAddress.getByName("255.255.255.255");
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * Gets the active local IP address.
     */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
                if (!intf.isLoopback() && intf.isUp()) {
                    Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "127.0.0.1";
    }
}
