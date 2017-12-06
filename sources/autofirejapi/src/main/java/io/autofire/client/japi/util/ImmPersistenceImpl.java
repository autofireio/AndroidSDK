package io.autofire.client.japi.util;

import io.autofire.client.japi.iface.BatchPersistence;
import io.autofire.client.japi.iface.PersistenceProvider;
import io.autofire.client.japi.event.Utils;

public class ImmPersistenceImpl implements PersistenceProvider {
    private static long retentionInSecs = BatchPersistence.ONE_WEEK_IN_SECS;
    private static String version = "";
    private String uuid = "";
    private String batch = "";
    private long batchTs = 0L;
    private int q = 0;

    public boolean isAvailable(Object platformContext) {
        return true;
    }

    private void resetEvt() {
        batch = "";
        batchTs = 0L;
    }

    public void reset(Object platformContext) {
        uuid = "";
        resetEvt();
    }

    public String getAutofireVersion(Object platformContext) {
        return version;
    }

    public void setAutofireVersion(Object platformContext, String version) {
        ImmPersistenceImpl.version = version;
    }

    public void setGameId(Object platformContext, String gameId) {
    }

    public String readUUID(Object platformContext) {
        return uuid;
    }

    public boolean writeUUID(Object platformContext, String guid) {
        uuid = guid;
        return true;
    }

    public void setRetention(Object platformContext, long retentionInSecs) {
        if (retentionInSecs > 0L)
            ImmPersistenceImpl.retentionInSecs = retentionInSecs;
        else
            ImmPersistenceImpl.retentionInSecs = BatchPersistence.ONE_WEEK_IN_SECS;
    }

    public int writeSerialized(Object platformContext,
                               String separator,
                               String beginBatch,
                               String header,
                               String tags,
                               String beginEvents,
                               Iterable<String> gameEvents,
                               long timestamp,
                               boolean forceBegin,
                               boolean forceEnd) {
        StringBuilder evts = new StringBuilder(beginEvents);
        int i = 0;
        for (String gameEvent : gameEvents)
            if (!Utils.isNullOrEmpty(gameEvent)) {
                evts.append(gameEvent).append(separator);
                i++;
            }
        if (i > 0) {
            evts = new StringBuilder(evts.substring(0, evts.length() - 1));

            batch = beginBatch +
                    header + separator +
                    tags + separator +
                    evts;
            batchTs = timestamp;
            q++;
            if (q < 0)
                q = 1;

            return 1;
        }

        return 0;
    }

    private boolean isInRetention(long now, long timestamp) {
        return (now - timestamp) <= retentionInSecs;
    }

    public String readSerialized(Object platformContext,
                                 long timestamp,
                                 String endEvents,
                                 String endBatch,
                                 boolean forceAll) {
        if (Utils.isNullOrEmpty(batch))
            return "";

        if (!isInRetention(timestamp, batchTs))
            resetEvt();
        q--;

        return batch + endEvents + endBatch;
    }

    public boolean commitReadSerialized(Object platformContext) {
        if (q > 0)
            return true;

        resetEvt();
        return true;
    }

    public boolean persistToDisk(Object platformContext) {
        return true;
    }
}
