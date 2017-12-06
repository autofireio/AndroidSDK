package io.autofire.client.japi.iface;

public interface PersistenceProvider {
    boolean isAvailable(Object platformContext);

    void reset(Object platformContext);

    String getAutofireVersion(Object platformContext);

    void setAutofireVersion(Object platformContext, String version);

    void setGameId(Object platformContext, String gameId);

    String readUUID(Object platformContext);

    boolean writeUUID(Object platformContext, String guid);

    void setRetention(Object platformContext, long retentionInSecs);

    int writeSerialized(Object platformContext,
                        String separator,
                        String beginBatch,
                        String header,
                        String tags,
                        String beginEvents,
                        Iterable<String> gameEvents,
                        long timestamp,
                        boolean forceBegin,
                        boolean forceEnd);

    String readSerialized(Object platformContext,
                          long timestamp,
                          String endEvents,
                          String endBatch,
                          boolean forceAll);

    boolean commitReadSerialized(Object platformContext);

    boolean persistToDisk(Object platformContext);
}
