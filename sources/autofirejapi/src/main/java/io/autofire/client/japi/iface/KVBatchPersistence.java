package io.autofire.client.japi.iface;

public abstract class KVBatchPersistence extends BatchPersistence {
    protected static final String DEFAULT_STRING = "";
    protected static final int DEFAULT_INT = 0;

    protected abstract String getString(Object platformContext,
                                        String key,
                                        boolean isAbsolute);

    protected abstract void setString(Object platformContext,
                                      String key, String value,
                                      boolean isAbsolute);

    protected abstract int getInt(Object platformContext,
                                  String key,
                                  boolean isAbsolute);

    protected abstract void setInt(Object platformContext,
                                   String key, int value,
                                   boolean isAbsolute);

    protected String getBatchKey(int key) {
        return "batch" + key;
    }

    protected String getBatchTimestampKey(int key) {
        return "batch" + key + ".ts";
    }

    public String getAutofireVersion(Object platformContext) {
        return getString(platformContext, "ver", false);
    }

    public void setAutofireVersion(Object platformContext, String version) {
        setString(platformContext, "ver", version, false);
    }

    public String readUUID(Object platformContext) {
        return getString(platformContext, "uuid", true);
    }

    public boolean writeUUID(Object platformContext, String uuid) {
        boolean ret = true;

        try {
            setString(platformContext, "uuid", uuid, true);
        } catch (Exception e) {
            ret = false;
        }

        return ret;
    }

    protected String getBatch(Object platformContext, int key) {
        return getString(platformContext, getBatchKey(key), false);
    }

    protected long getBatchTimestamp(Object platformContext, int key) {
        return Long.parseLong(getString(platformContext,
                getBatchTimestampKey(key), false));
    }

    protected void setBatch(Object platformContext, int key, String value) {
        setString(platformContext, getBatchKey(key), value, false);
    }

    protected void setBatchWithTimestamp(Object platformContext, int key, String value,
                                         long timestamp) {
        setBatch(platformContext, key, value);
        setString(platformContext,
                getBatchTimestampKey(key), String.valueOf(timestamp), false);
    }

    protected int getWriteBatchEvents(Object platformContext) {
        return getInt(platformContext, "writeBatchEvents", false);
    }

    protected void setWriteBatchEvents(Object platformContext, int writeBatchEvents) {
        setInt(platformContext, "writeBatchEvents", writeBatchEvents, false);
    }

    protected int getReadBatch(Object platformContext) {
        return getInt(platformContext, "readBatch", false);
    }

    protected void setReadBatch(Object platformContext, int readBatch) {
        setInt(platformContext, "readBatch", readBatch, false);
    }

    protected int getWriteBatch(Object platformContext) {
        return getInt(platformContext, "writeBatch", false);
    }

    protected void setWriteBatch(Object platformContext, int writeBatch) {
        setInt(platformContext, "writeBatch", writeBatch, false);
    }
}
