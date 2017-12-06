package io.autofire.client.android.iface;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.autofire.client.japi.util.BasicHTTPImpl;

public class OnlineHTTPImpl extends BasicHTTPImpl {
    @SuppressLint("MissingPermission")
    @Override
    public boolean isOnline(Object platformContext) {
        Context ctx = (Context) platformContext;
        if (!Utils.hasPermission(ctx, Manifest.permission.INTERNET))
            return false;
        boolean hasPermission =
                Utils.hasPermission(ctx, Manifest.permission.ACCESS_NETWORK_STATE);
        if (hasPermission) {
            ConnectivityManager cm =
                    (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                return netInfo != null && netInfo.isConnected();
            }
        }

        return true;
    }
}
