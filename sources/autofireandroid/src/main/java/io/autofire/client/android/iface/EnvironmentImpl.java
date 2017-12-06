package io.autofire.client.android.iface;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import java.util.Locale;

import io.autofire.client.japi.iface.EnvironmentProvider;

public class EnvironmentImpl implements EnvironmentProvider {
    /**
     * Returns the consumer friendly device name
     * See:
     * http://stackoverflow.com/questions/1995439/get-android-phone-model-programmatically
     */
    private static String getConsumerFriendlyDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer))
            return capitalize(model);
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str))
            return str;
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c))
                capitalizeNext = true;
            phrase.append(c);
        }

        return phrase.toString();
    }

    public String getPlatform(Object platformContext) {
        return "Android";
    }

    public String getOs(Object platformContext) {
        return "android " + Build.VERSION.RELEASE;
    }

    public String getModel(Object platformContext) {
        return getConsumerFriendlyDeviceName();
    }

    public String getLocale(Object platformContext) {
        return Locale.getDefault().toString();
    }

    public String getVersion(Object platformContext) {
        Context ctx = (Context) platformContext;
        String versionName;
        try {
            versionName = ctx
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "Unknown";
        }

        return versionName;
    }

    public int getVersionCode(Object platformContext) {
        Context ctx = (Context) platformContext;
        int versionCode;
        try {
            versionCode = ctx
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0)
                    .versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionCode = 0;
        }

        return versionCode;
    }
}
