package io.autofire.client.android.iface;

import android.Manifest;
import android.content.Context;
import android.os.Environment;

import io.autofire.client.japi.iface.FileBatchPersistence;

public class ExternalStoragePersistenceImpl extends FileBatchPersistence {
    protected String rootDirectory(Object platformContext) {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    public boolean isAvailable(Object platformContext) {
        Context ctx = (Context) platformContext;
        boolean hasPermission =
                Utils.hasPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasPermission) {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state);
        }

        return false;
    }
}
