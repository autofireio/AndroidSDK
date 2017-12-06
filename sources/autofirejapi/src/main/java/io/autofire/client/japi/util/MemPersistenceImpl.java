package io.autofire.client.japi.util;

import io.autofire.client.japi.iface.BatchPersistence;

public class MemPersistenceImpl extends BatchPersistence {
    private static String version = "";
    private String uuid = "";
    private String[] batches = new String[MAX_BATCHES];
    private long[] batchTimestamps = new long[MAX_BATCHES];
    private int readBatch = 0;
    private int writeBatch = 0;
    private int writeBatchEvents = 0;

    public boolean isAvailable(Object platformContext) {
        return true;
    }

    public String getAutofireVersion(Object platformContext) {
        return version;
    }

    public void setAutofireVersion(Object platformContext, String version) {
        MemPersistenceImpl.version = version;
    }

    public String readUUID(Object platformContext) {
        return uuid;
    }

    public boolean writeUUID(Object platformContext, String uuid) {
        this.uuid = uuid;

        return true;
    }

    protected String getBatch(Object platformContext, int key) {
        return batches[key];
    }

    protected long getBatchTimestamp(Object platformContext, int key) {
        return batchTimestamps[key];
    }

    protected void setBatch(Object platformContext, int key, String value) {
        batches[key] = value;
    }

    protected void setBatchWithTimestamp(Object platformContext, int key, String value,
                                         long timestamp) {
        setBatch(platformContext, key, value);
        batchTimestamps[key] = timestamp;
    }

    protected int getWriteBatchEvents(Object platformContext) {
        return writeBatchEvents;
    }

    protected void setWriteBatchEvents(Object platformContext, int writeBatchEvents) {
        this.writeBatchEvents = writeBatchEvents;
    }

    protected int getReadBatch(Object platformContext) {
        return readBatch;
    }

    protected void setReadBatch(Object platformContext, int readBatch) {
        this.readBatch = readBatch;
    }

    protected int getWriteBatch(Object platformContext) {
        return writeBatch;
    }

    protected void setWriteBatch(Object platformContext, int writeBatch) {
        this.writeBatch = writeBatch;
    }

    public boolean persistToDisk(Object platformContext) {
        return true;
    }
}
