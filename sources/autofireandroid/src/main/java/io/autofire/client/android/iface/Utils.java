package io.autofire.client.android.iface;

import android.content.Context;
import android.content.pm.PackageManager;

public class Utils {
    // reference:
    //   https://stackoverflow.com/questions/7203668/
    //     how-permission-can-be-checked-at-runtime-without-throwing-securityexception
    public static boolean hasPermission(Context ctx, String permission) {
        int res = ctx.checkCallingOrSelfPermission(permission);

        return res == PackageManager.PERMISSION_GRANTED;
    }
}
