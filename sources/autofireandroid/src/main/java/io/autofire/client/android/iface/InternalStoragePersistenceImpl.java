package io.autofire.client.android.iface;

import android.content.Context;

import io.autofire.client.japi.iface.FileBatchPersistence;

public class InternalStoragePersistenceImpl extends FileBatchPersistence {
    protected String rootDirectory(Object platformContext) {
        Context ctx = (Context) platformContext;
        return ctx.getFilesDir().getAbsolutePath();
    }

    private String readUUIDHelper(Object platformContext) {
        return getString(platformContext, "uuid", true);
    }

    @Override
    public String readUUID(Object platformContext) {
        ExternalStoragePersistenceImpl external = new ExternalStoragePersistenceImpl();
        if (external.isAvailable(platformContext)) {
            String uuid = external.readUUID(platformContext);
            if (io.autofire.client.japi.event.Utils.isNullOrEmpty(uuid))
                return readUUIDHelper(platformContext);
            else
                return external.readUUID(platformContext);
        }

        return readUUIDHelper(platformContext);
    }

    private boolean writeUUIDHelper(Object platformContext, String uuid) {
        boolean ret = true;

        try {
            setString(platformContext, "uuid", uuid, true);
        } catch (Exception e) {
            ret = false;
        }

        return ret;
    }

    @Override
    public boolean writeUUID(Object platformContext, String uuid) {
        ExternalStoragePersistenceImpl external = new ExternalStoragePersistenceImpl();
        if (external.isAvailable(platformContext))
            return external.writeUUID(platformContext, uuid) ||
                    writeUUIDHelper(platformContext, uuid);
        else
            return writeUUIDHelper(platformContext, uuid);
    }
}
