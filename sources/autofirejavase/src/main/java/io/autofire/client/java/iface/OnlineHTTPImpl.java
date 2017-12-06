package io.autofire.client.java.iface;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import io.autofire.client.japi.util.BasicHTTPImpl;

public class OnlineHTTPImpl extends BasicHTTPImpl {
    @Override
    public boolean isOnline(Object platformContext) {
        Enumeration<NetworkInterface> eni;
        try {
            eni = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return false;
        }
        while (eni.hasMoreElements()) {
            Enumeration<InetAddress> eia = eni.nextElement().getInetAddresses();
            while (eia.hasMoreElements()) {
                InetAddress ia = eia.nextElement();
                if (!ia.isAnyLocalAddress() && !ia.isLoopbackAddress() && !ia.isSiteLocalAddress()) {
                    if (!ia.getHostName().equals(ia.getHostAddress()))
                        return true;
                }
            }
        }

        return false;
    }
}
