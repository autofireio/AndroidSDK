package io.autofire.client.japi.iface;

import io.autofire.client.japi.event.Utils;

public abstract class BatchPersistence implements PersistenceProvider {
    protected abstract String getBatch(Object platformContext, int key);

    protected abstract long getBatchTimestamp(Object platformContext, int key);

    protected abstract void setBatch(Object platformContext, int key, String value);

    protected abstract void setBatchWithTimestamp(Object platformContext, int key, String value,
                                                  long timestamp);

    protected abstract int getWriteBatchEvents(Object platformContext);

    protected abstract void setWriteBatchEvents(Object platformContext, int writeBatchEvents);

    protected abstract int getReadBatch(Object platformContext);

    protected abstract void setReadBatch(Object platformContext, int readBatch);

    protected abstract int getWriteBatch(Object platformContext);

    protected abstract void setWriteBatch(Object platformContext, int writeBatch);

    public static final long ONE_WEEK_IN_SECS = 7L * 24L * 3600L;
    protected static final int MAX_BATCHES = 20;

    protected static String gameId = "Unknown";
    protected static int maxEventsPerBatch = 32;
    protected static int maxBatches = 16;
    protected static long retentionInSecs = ONE_WEEK_IN_SECS;

    private int previousReadBatch = -1;

    public void setGameId(Object platformContext, String gameId) {
        BatchPersistence.gameId = gameId;
    }

    public void setRetention(Object platformContext, long retentionInSecs) {
        if (retentionInSecs > 0L)
            BatchPersistence.retentionInSecs = retentionInSecs;
        else
            BatchPersistence.retentionInSecs = ONE_WEEK_IN_SECS;
    }

    public static void setMaxEventsPerBatch(int maxEvents) {
        if (maxEvents >= 1)
            BatchPersistence.maxEventsPerBatch = maxEvents;
        else
            BatchPersistence.maxEventsPerBatch = 1;
    }

    public static void setMaxBatches(int maxBatches) {
        if (maxBatches >= 2 && maxBatches <= MAX_BATCHES)
            BatchPersistence.maxBatches = maxBatches;
        else if (maxBatches > MAX_BATCHES)
            BatchPersistence.maxBatches = MAX_BATCHES;
        else
            BatchPersistence.maxBatches = 2;
    }

    private void resetWriteBatchEvents(Object platformContext) {
        setWriteBatchEvents(platformContext, 0);
    }

    private void resetReadBatch(Object platformContext) {
        setReadBatch(platformContext, 0);
    }

    private void resetWriteBatch(Object platformContext) {
        setWriteBatch(platformContext, 0);
    }

    private void resetBatches(Object platformContext) {
        resetWriteBatchEvents(platformContext);
        resetReadBatch(platformContext);
        resetWriteBatch(platformContext);
    }

    public void reset(Object platformContext) {
        writeUUID(platformContext, "");
        resetBatches(platformContext);
    }

    private boolean isBatchEmpty(int batchEvents) {
        return batchEvents == 0;
    }

    private boolean isBatchFull(int batchEvents) {
        return batchEvents >= maxEventsPerBatch;
    }

    private int nextBatch(int batch) {
        return (batch + 1) % maxBatches;
    }

    private int diff(int writeBatch, int readBatch) {
        if (writeBatch >= readBatch)
            return writeBatch - readBatch;

        return (maxBatches - readBatch) + (writeBatch - 1);
    }

    private boolean isEmpty(int writeBatch, int readBatch) {
        return writeBatch == readBatch;
    }

    private boolean isEmpty(int writeBatch, int readBatch, int writeBatchEvents) {
        return isBatchEmpty(writeBatchEvents) && isEmpty(writeBatch, readBatch);
    }

    private boolean isFull(int writeBatch, int readBatch) {
        return nextBatch(writeBatch) == readBatch;
    }

    private boolean isFull(int writeBatch, int readBatch, int writeBatchEvents) {
        return isBatchFull(writeBatchEvents) && isFull(writeBatch, readBatch);
    }

    private boolean isInRetention(long now, long timestamp) {
        return (now - timestamp) <= retentionInSecs;
    }

    private int filterRetention(Object platformContext,
                                long timestamp,
                                int writeBatch,
                                Ref<Integer> readBatch,
                                Ref<Integer> writeBatchEvents) {
        int i = 0;
        boolean done = false;
        long rdTs;
        while (!done)
            if (isEmpty(writeBatch, readBatch.get(), writeBatchEvents.get()))
                done = true;
            else {
                rdTs = getBatchTimestamp(platformContext, readBatch.get());
                if (isInRetention(timestamp, rdTs))
                    done = true;
                else if (isEmpty(writeBatch, readBatch.get())) {
                    resetWriteBatchEvents(platformContext);
                    writeBatchEvents.set(0);
                    i++;
                    done = true;
                } else {
                    readBatch.set(nextBatch(readBatch.get()));
                    i++;
                }
            }
        if (i > 0)
            setReadBatch(platformContext, readBatch.get());

        return i;
    }

    private String batchOf(Object platformContext,
                           int writeBatch,
                           int writeBatchEvents,
                           String separator,
                           String beginBatch,
                           String header,
                           String tags,
                           String beginEvents) {
        String batchValue;
        if (writeBatchEvents == 0)
            batchValue = beginBatch +
                    header + separator +
                    tags + separator +
                    beginEvents;
        else
            batchValue = getBatch(platformContext, writeBatch);

        return batchValue;
    }

    private void appendEvent(Ref<String> batchValue,
                             Ref<Integer> writeBatchEvents,
                             String gameEvent,
                             String separator) {
        if (!Utils.isNullOrEmpty(gameEvent)) {
            if (writeBatchEvents.get() > 0)
                batchValue.set(batchValue.get() + separator);
            batchValue.set(batchValue.get() + gameEvent);
            writeBatchEvents.set(writeBatchEvents.get() + 1);
        }
    }

    private String sealBatch(Object platformContext,
                             int readBatch,
                             String endEvents,
                             String endBatch) {
        String last = endEvents + endBatch;
        String batchValue = getBatch(platformContext, readBatch);
        if (Utils.isNullOrEmpty(batchValue))
            return "";

        if (!batchValue.endsWith(last)) {
            batchValue += last;
            setBatch(platformContext, readBatch, batchValue);
        }

        return batchValue;
    }

    private void incWriteBatch(Object platformContext,
                               long timestamp,
                               Ref<Integer> writeBatch,
                               Ref<Integer> readBatch,
                               Ref<Integer> writeBatchEvents) {
        if (isFull(writeBatch.get(), readBatch.get())) {
            int removed =
                    filterRetention(platformContext, timestamp, writeBatch.get(), readBatch, writeBatchEvents);
            if (removed == 0)
                readBatch.set(nextBatch(readBatch.get()));
        }
        writeBatch.set(nextBatch(writeBatch.get()));
        writeBatchEvents.set(0);
    }

    private void setBatches(Object platformContext,
                            int previousWriteBatch,
                            int currentWriteBatch,
                            int previousReadBatch,
                            int currentReadBatch,
                            int previousWriteBatchEvents,
                            int currentWriteBatchEvents) {
        if (previousWriteBatch != currentWriteBatch)
            setWriteBatch(platformContext, currentWriteBatch);
        if (previousReadBatch != currentReadBatch)
            setReadBatch(platformContext, currentReadBatch);
        if (previousWriteBatchEvents != currentWriteBatchEvents)
            setWriteBatchEvents(platformContext, currentWriteBatchEvents);
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
        try {
            int wr0 = getWriteBatch(platformContext);
            Ref<Integer> wr = new Ref<Integer>(wr0);
            int rd0 = getReadBatch(platformContext);
            Ref<Integer> rd = new Ref<Integer>(rd0);
            int wrEvents0 = getWriteBatchEvents(platformContext);
            Ref<Integer> wrEvents = new Ref<Integer>(wrEvents0);
            int result = 0;

            if (forceBegin && isBatchEmpty(wrEvents.get()))
                forceBegin = false;
            if (forceBegin) {
                incWriteBatch(platformContext, timestamp, wr, rd, wrEvents);
                result = 1;
            }

            Ref<String> batchValue = new Ref<String>(
                    batchOf(platformContext, wr.get(), wrEvents.get(),
                            separator, beginBatch, header, tags, beginEvents));
            for (String gameEvent : gameEvents) {
                appendEvent(batchValue, wrEvents, gameEvent, separator);

                if (isBatchFull(wrEvents.get()))
                    forceEnd = true;
                if (forceEnd) {
                    setBatchWithTimestamp(platformContext, wr.get(), batchValue.get(), timestamp);
                    incWriteBatch(platformContext, timestamp, wr, rd, wrEvents);
                    batchValue.set(
                            batchOf(platformContext, wr.get(), wrEvents.get(),
                                    separator, beginBatch, header, tags, beginEvents));
                    forceEnd = false;
                    result = 1;
                }
            }
            if (wrEvents.get() > 0)
                setBatchWithTimestamp(platformContext, wr.get(), batchValue.get(), timestamp);

            setBatches(platformContext, wr0, wr.get(), rd0, rd.get(), wrEvents0, wrEvents.get());

            return result;
        } catch (Exception e) {
            resetBatches(platformContext);

            return -1;
        }
    }

    public String readSerialized(Object platformContext,
                                 long timestamp,
                                 String endEvents,
                                 String endBatch,
                                 boolean forceAll) {
        try {
            int wr0 = getWriteBatch(platformContext);
            Ref<Integer> wr = new Ref<Integer>(wr0);
            int rd0 = getReadBatch(platformContext);
            Ref<Integer> rd = new Ref<Integer>(rd0);
            int wrEvents0 = getWriteBatchEvents(platformContext);
            Ref<Integer> wrEvents = new Ref<Integer>(wrEvents0);
            String result;

            filterRetention(platformContext, timestamp, wr.get(), rd, wrEvents);

            if (isEmpty(wr.get(), rd.get()) && (!forceAll || isBatchEmpty(wrEvents.get())))
                return "";

            result = sealBatch(platformContext, rd.get(), endEvents, endBatch);
            previousReadBatch = rd.get();

            if (isEmpty(wr.get(), rd.get()))
                incWriteBatch(platformContext, timestamp, wr, rd, wrEvents);

            setBatches(platformContext, wr0, wr.get(), rd0, rd.get(), wrEvents0, wrEvents.get());

            return result;
        } catch (Exception e) {
            resetBatches(platformContext);

            return "";
        }
    }

    public boolean commitReadSerialized(Object platformContext) {
        try {
            int wr0 = getWriteBatch(platformContext);
            Ref<Integer> wr = new Ref<Integer>(wr0);
            int rd0 = getReadBatch(platformContext);
            Ref<Integer> rd = new Ref<Integer>(rd0);

            if (rd.get() != previousReadBatch)
                return true;

            if (isEmpty(wr.get(), rd.get()))
                resetWriteBatchEvents(platformContext);
            else
                rd.set(nextBatch(rd.get()));

            setBatches(platformContext, wr0, wr.get(), rd0, rd.get(), -1, -1);

            return true;
        } catch (Exception e) {
            resetBatches(platformContext);

            return false;
        }
    }
}
