package io.autofire.client.android.iface;

import android.content.Context;
import android.content.SharedPreferences;

import io.autofire.client.japi.iface.KVBatchPersistence;

public class PrefsPersistenceImpl extends KVBatchPersistence {
    public static final String PREFIX = "io.autofire.";
    private static final String PREF_FILE = PREFIX + "prefs";
    private static final int MODE = Context.MODE_PRIVATE;

    private static String getName(String key) {
        return PREFIX + key;
    }

    private static SharedPreferences read(Object platformContext) {
        Context ctx = (Context) platformContext;
        return ctx.getSharedPreferences(PREF_FILE, MODE);
    }

    private static SharedPreferences.Editor edit(Object platformContext) {
        SharedPreferences rd = read(platformContext);
        return rd.edit();
    }

    protected String getString(Object platformContext,
                               String key,
                               boolean isAbsolute) {
        return read(platformContext)
                .getString(getName(key), DEFAULT_STRING);
    }

    protected void setString(Object platformContext,
                             String key, String value,
                             boolean isAbsolute) {
        SharedPreferences.Editor editor = edit(platformContext);
        editor.putString(getName(key), value);
        editor.apply();
    }

    protected int getInt(Object platformContext,
                         String key,
                         boolean isAbsolute) {
        return read(platformContext)
                .getInt(getName(key), DEFAULT_INT);
    }

    protected void setInt(Object platformContext,
                          String key, int value,
                          boolean isAbsolute) {
        SharedPreferences.Editor editor = edit(platformContext);
        editor.putInt(getName(key), value);
        editor.apply();
    }

    public boolean isAvailable(Object platformContext) {
        return true;
    }

    public boolean persistToDisk(Object platformContext) {
        return edit(platformContext).commit();
    }

    private String readUUIDHelper(Object platformContext) {
        return getString(platformContext, "uuid", true);
    }

    @Override
    public String readUUID(Object platformContext) {
        InternalStoragePersistenceImpl internal = new InternalStoragePersistenceImpl();
        if (internal.isAvailable(platformContext)) {
            String uuid = internal.readUUID(platformContext);
            if (io.autofire.client.japi.event.Utils.isNullOrEmpty(uuid))
                return readUUIDHelper(platformContext);
            else
                return internal.readUUID(platformContext);
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
        InternalStoragePersistenceImpl internal = new InternalStoragePersistenceImpl();
        if (internal.isAvailable(platformContext))
            return internal.writeUUID(platformContext, uuid) ||
                    writeUUIDHelper(platformContext, uuid);
        else
            return writeUUIDHelper(platformContext, uuid);
    }
}
