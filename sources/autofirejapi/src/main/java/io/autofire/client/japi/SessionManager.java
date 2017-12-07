package io.autofire.client.japi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.autofire.client.japi.event.Deinit;
import io.autofire.client.japi.event.GameEvent;
import io.autofire.client.japi.event.Init;
import io.autofire.client.japi.event.Progress;
import io.autofire.client.japi.event.RawEvent;
import io.autofire.client.japi.event.Utils;
import io.autofire.client.japi.iface.BatchEncoderProvider;
import io.autofire.client.japi.iface.EnvironmentProvider;
import io.autofire.client.japi.iface.GUIDProvider;
import io.autofire.client.japi.iface.HTTPProvider;
import io.autofire.client.japi.iface.HelperHTTPResponse;
import io.autofire.client.japi.iface.HelperHTTPResponseHandler;
import io.autofire.client.japi.iface.LoggerProvider;
import io.autofire.client.japi.iface.PersistenceProvider;
import io.autofire.client.japi.iface.TaskCallback;
import io.autofire.client.japi.iface.TaskFactory;
import io.autofire.client.japi.util.MemPersistenceImpl;

public class SessionManager {
    private static final int MAX_SEND_INTERVAL_SEC = 90;
    private static final int HTTP_REQUEST_TIMEOUT_SEC = 10;
    private static final int HTTP_REQUEST_TIMEOUT_FLUSH_SEC = 4;

    private static Object platformContext;
    private static TaskFactory taskFactory = null;
    private static AtomicBoolean isTaskFactoryInitialized = new AtomicBoolean(false);
    private static LoggerProvider logger;
    private static EnvironmentProvider environment;
    private static PersistenceProvider persistence;
    private static GUIDProvider guid;
    private static BatchEncoderProvider encoder;
    private static HTTPProvider http;

    private static boolean isSetUp = false;
    private static boolean isInitialized = false;
    private static boolean isFlushing = false;
    private static boolean httpSending = false;
    // NOTE: set to 0 in order to send the INIT asap (i.e, bypass batching for INIT)
    private static long httpLastAttempt = GameEvent.nowFromEpoch();
    private static Header currentHeader;
    private static String currentSerializedHeader;
    private static HashSet<String> currentTags;
    private static String currentSerializedTags;

    private static int sendIntervalSec = MAX_SEND_INTERVAL_SEC;
    private static String apiURL = "https://service.autofire.io/api/v1";
    private static String dataPointsPath = "command/clients/datapoints";
    private static HashMap<String, String> requestHeaders;

    private static void writeErrLine(String what) {
        System.err.println(what);
    }

    private static void writeInitErrLine(String what) {
        writeErrLine("Autofire initialization error: " + what);
    }

    private static void writeInitSetupErrLine() {
        writeErrLine("Autofire initialization error: Not properly set up");
    }

    private static void defaultBeginTask(String name) {
    }

    private static void defaultEndTask(String result) {
    }

    private static class HandleResponseResult {
        boolean checkNext;
        boolean willFlush;

        HandleResponseResult(boolean checkNext, boolean willFlush) {
            this.checkNext = checkNext;
            this.willFlush = willFlush;
        }
    }

    private static synchronized HandleResponseResult handleHTTPResponseCritical(
            HelperHTTPResponse response) {
        boolean checkNext;

        if (isInitialized) {
            logger.logDebug(platformContext, "Autofire HTTP response", response.toString());

            httpSending = false;
            if (response.isDiscardable())
                checkNext = persistence.commitReadSerialized(platformContext);
            else {
                checkNext = false;
                if (isFlushing) {
                    isFlushing = false;
                    http.setRequestTimeout(platformContext, HTTP_REQUEST_TIMEOUT_SEC);
                }
            }
        } else {
            checkNext = false;

            writeInitErrLine("Cannot handle HTTP response" + response.toString());
        }

        return new HandleResponseResult(checkNext, isFlushing);
    }

    private static void handleHTTPResponse(HelperHTTPResponse response) {
        long now = GameEvent.nowFromEpoch();

        HandleResponseResult result = handleHTTPResponseCritical(response);

        if (result.checkNext)
            sendBatch(now, result.willFlush);
    }

    private static class ResponseHandler implements HelperHTTPResponseHandler {
        public void handleResponse(Object platformContext, HelperHTTPResponse response) {
            handleHTTPResponse(response);
        }
    }

    private static ResponseHandler responseHandler = new ResponseHandler();

    public static synchronized void setEnvironmentProvider(Object platformContext,
                                                           EnvironmentProvider environment) {
        if (environment == null) {
            writeInitErrLine("Null EnvironmentProvider");
            return;
        }
        SessionManager.environment = environment;
    }

    public static synchronized void setProviders(Object platformContext,
                                                 TaskFactory taskFactory,
                                                 LoggerProvider logger,
                                                 EnvironmentProvider environment,
                                                 PersistenceProvider persistence,
                                                 GUIDProvider guid,
                                                 BatchEncoderProvider encoder,
                                                 HTTPProvider http) {
        SessionManager.platformContext = platformContext;

        if (taskFactory == null) {
            writeInitErrLine("Null TaskFactory");
            return;
        }
        if (SessionManager.taskFactory != null)
            SessionManager.taskFactory.shutdown();
        taskFactory.start();
        SessionManager.taskFactory = taskFactory;

        if (logger == null) {
            writeInitErrLine("Null LoggerProvider");
            return;
        }
        SessionManager.logger = logger;

        if (environment == null) {
            writeInitErrLine("Null EnvironmentProvider");
            return;
        }
        SessionManager.environment = environment;

        if (persistence != null && SessionManager.persistence != null) {
            SessionManager.logger.logDebug(platformContext,
                    "Autofire IPersistenceProvider changed",
                    "Resetting - Possible loss of events");
            String playerId = SessionManager.persistence.readUUID(platformContext);
            SessionManager.persistence.reset(platformContext);
            SessionManager.persistence.writeUUID(platformContext, playerId);
        } else if (persistence == null) {
            writeInitErrLine("Null PersistenceProvider");
            return;
        } else if (SessionManager.persistence != null)
            SessionManager.persistence.persistToDisk(platformContext);
        SessionManager.persistence = persistence;
        if (!SessionManager.persistence.isAvailable(platformContext))
            SessionManager.persistence = new MemPersistenceImpl();

        if (guid == null) {
            writeInitErrLine("Null GUIDProvider");
            return;
        }
        SessionManager.guid = guid;

        if (encoder == null) {
            writeInitErrLine("Null BatchEncoderProvider");
            return;
        }
        SessionManager.encoder = encoder;

        if (http == null) {
            writeInitErrLine("Null HTTPProvider");
            return;
        }
        SessionManager.http = http;
        SessionManager.http.setRequestTimeout(platformContext, HTTP_REQUEST_TIMEOUT_SEC);

        isSetUp = true;
        isTaskFactoryInitialized.set(true);
    }

    public static synchronized void setRetention(long retentionInSecs) {
        if (isSetUp)
            persistence.setRetention(platformContext, retentionInSecs);
        else
            writeInitErrLine("Cannot set retention");
    }

    public static synchronized void setSendInterval(int intervalSecs) {
        if (intervalSecs <= MAX_SEND_INTERVAL_SEC)
            sendIntervalSec = intervalSecs;
        else
            sendIntervalSec = MAX_SEND_INTERVAL_SEC;
    }

    public static synchronized void setApiURL(String apiURL) {
        if (!Utils.isNullOrEmpty(apiURL))
            SessionManager.apiURL = apiURL;
    }

    public static synchronized void setDataPointsPath(String dataPointsPath) {
        if (!Utils.isNullOrEmpty(dataPointsPath))
            SessionManager.dataPointsPath = dataPointsPath;
    }

    private static String getUUID(String hint) {
        String uuid = persistence.readUUID(platformContext);

        if (!Utils.isNullOrEmpty(hint)) {
            if (!hint.equals(uuid))
                persistence.writeUUID(platformContext, hint);

            return hint;
        } else if (!Utils.isNullOrEmpty(uuid))
            return uuid;

        uuid = guid.newGUID(platformContext);
        persistence.writeUUID(platformContext, uuid);

        return uuid;
    }

    private static void initializeHeaderParameters(String gameId, String playerId) {
        currentSerializedHeader = encoder.encodeHeader(platformContext, currentHeader.toRaw());
        currentSerializedTags = encoder.encodeTags(platformContext, currentTags);

        requestHeaders = new HashMap<String, String>();
        requestHeaders.put("X-Autofire-Game-Id", gameId);
        requestHeaders.put("X-Autofire-Player-Id", playerId);
    }

    private static synchronized void initializeCritical(String gameId, String playerId,
                                                        long initTimestamp, String atLevel,
                                                        Map<String, String> headerFeatures,
                                                        Set<String> tags) {
        if (isSetUp) {
            persistence.setGameId(platformContext, gameId);
            String ver = persistence.getAutofireVersion(platformContext);

            // NOTE: handle Autofire version update here
            if (Utils.isNullOrEmpty(ver) || !ver.equals(Version.VERSION)) {
                String guid = persistence.readUUID(platformContext);
                persistence.reset(platformContext);
                if (!Utils.isNullOrEmpty(guid))
                    persistence.writeUUID(platformContext, guid);
            }

            if (!ver.equals(Version.VERSION))
                persistence.setAutofireVersion(platformContext, Version.VERSION);

            playerId = getUUID(playerId);
            if (Utils.isNullOrEmpty(playerId))
                playerId = "nobody";

            logger.logDebug(platformContext, "Autofire initializing with Game Id", gameId);
            logger.logDebug(platformContext, "Autofire initializing with Player Id", playerId);

            currentHeader = new Header(platformContext, environment,
                    headerFeatures, initTimestamp, atLevel);
            currentTags = new HashSet<String>();
            if (tags != null)
                currentTags = new HashSet<String>(tags);
            initializeHeaderParameters(gameId, playerId);
            isInitialized = true;
        } else
            writeInitSetupErrLine();
    }

    public static void initialize(Initializer initializer) {
        final String taskName = "initialize";
        final String gameId = initializer.gameId;
        final String playerId = initializer.playerId;
        final long initializeTimestamp = initializer.timestamp;
        final String atLevel = "";
        final Map<String, String> headerFeatures =
                new HashMap<String, String>(initializer.headers);
        final Set<String> tags =
                new HashSet<String>(initializer.tags);

        if (isTaskFactoryInitialized.get())
            taskFactory
                    .mk(taskName, new TaskCallback() {
                        @Override
                        public String doOperation() {
                            defaultBeginTask(taskName);

                            initializeCritical(gameId, playerId,
                                    initializeTimestamp, atLevel,
                                    headerFeatures, tags);

                            long now = GameEvent.nowFromEpoch();
                            GameEvent init = new Init().withTimestamp(initializeTimestamp);
                            sendEventWrapper(init, now);

                            return taskName;
                        }

                        @Override
                        public void onResult(String result) {
                            defaultEndTask(result);
                        }
                    })
                    .runMe();
        else
            writeInitSetupErrLine();
    }

    private static class SendBatchResult {
        boolean willPost;
        String url;
        String payload;

        SendBatchResult(boolean willPost, String url, String payload) {
            this.willPost = willPost;
            this.url = url;
            this.payload = payload;
        }
    }

    private static synchronized SendBatchResult sendBatchCritical(long now, boolean willFlush) {
        boolean willPost = false;
        String dataPointsURL = "";
        String payload = "";

        if (isInitialized && !httpSending && http.isOnline(platformContext)) {
            dataPointsURL = apiURL + "/" + dataPointsPath;
            payload = persistence.readSerialized(platformContext,
                    now,
                    encoder.getEventsEnd(platformContext),
                    encoder.getBatchEnd(platformContext),
                    willFlush);
            if (!Utils.isNullOrEmpty(payload)) {
                willPost = true;
                httpLastAttempt = GameEvent.nowFromEpoch();

                logger.logDebug(platformContext, "Autofire HTTP request payload", payload);
            }
            httpSending = willPost;
        }

        return new SendBatchResult(willPost, dataPointsURL, payload);
    }

    private static void sendBatch(long now, boolean willFlush) {
        SendBatchResult result = sendBatchCritical(now, willFlush);

        if (result.willPost)
            http.postData(platformContext,
                    responseHandler, result.url,
                    encoder.getContentType(platformContext), "application/json",
                    requestHeaders,
                    result.payload);
    }

    private static synchronized boolean sendEventsCritical(Iterable<RawEvent> rawEvents,
                                                           long lastEventTimestamp,
                                                           long now) {
        boolean willSend = false;

        if (isInitialized) {
            boolean forceBegin = false;
            String atLevel = null;
            List<String> serializedEvents = new ArrayList<String>();
            for (RawEvent rawEvent : rawEvents) {
                if (rawEvent.name.equals(Init.INIT_NAME))
                    forceBegin = true;
                else if (rawEvent.name.equals(Progress.PROGRESS_NAME)) {
                    String level = rawEvent.nominals.get(Progress.PROGRESS_LEVEL_NAME);
                    if (!Utils.isNullOrEmpty(level))
                        atLevel = level;
                }
                serializedEvents.add(encoder.encodeEvent(platformContext, rawEvent));
            }
            boolean forceEnd = now - httpLastAttempt > sendIntervalSec;

            int appendResult = persistence.writeSerialized(platformContext,
                    encoder.getSeparator(platformContext),
                    encoder.getBatchBegin(platformContext),
                    currentSerializedHeader,
                    currentSerializedTags,
                    encoder.getEventsBegin(platformContext),
                    serializedEvents,
                    lastEventTimestamp,
                    forceBegin,
                    forceEnd);
            willSend = appendResult == 1;

            if (!Utils.isNullOrEmpty(atLevel)) {
                currentHeader.atLevel = atLevel;
                currentSerializedHeader = encoder.encodeHeader(platformContext,
                        currentHeader.toRaw());
            }
        } else
            writeInitErrLine("Cannot send events");

        return willSend;
    }

    private static boolean sendEventWrapper(GameEvent gameEvent, long now) {
        List<RawEvent> rawEvents = new ArrayList<RawEvent>();
        rawEvents.add(gameEvent.toRaw());
        boolean willSend = sendEventsCritical(rawEvents, gameEvent.getTimestamp(), now);

        if (willSend)
            sendBatch(now, false);

        return willSend;
    }

    public static void sendEvents(Iterable<GameEvent> gameEvents) {
        final String taskName = "sendEvents";
        final long now = GameEvent.nowFromEpoch();
        final List<RawEvent> rawEvents = new ArrayList<RawEvent>();
        long lastTimestamp = now;

        for (GameEvent gameEvent : gameEvents) {
            rawEvents.add(gameEvent.toRaw());
            lastTimestamp = gameEvent.getTimestamp();
        }
        final long lastTimestamp1 = lastTimestamp;

        if (isTaskFactoryInitialized.get())
            taskFactory
                    .mk(taskName, new TaskCallback() {
                        @Override
                        public String doOperation() {
                            defaultBeginTask(taskName);

                            boolean willSend = sendEventsCritical(rawEvents, lastTimestamp1, now);

                            if (willSend)
                                sendBatch(now, false);

                            return taskName;
                        }

                        @Override
                        public void onResult(String result) {
                            defaultEndTask(result);
                        }
                    })
                    .runMe();
        else
            writeInitSetupErrLine();
    }

    public static void sendEvent(GameEvent gameEvent) {
        List<GameEvent> gameEvents = new ArrayList<GameEvent>();
        gameEvents.add(gameEvent);
        sendEvents(gameEvents);
    }

    private static synchronized void persistToDiskCritical() {
        if (isInitialized)
            persistence.persistToDisk(platformContext);
        else
            writeInitErrLine("Cannot persist to disk");
    }

    public static void persistToDisk() {
        final String taskName = "persistToDisk";

        if (isTaskFactoryInitialized.get())
            taskFactory
                    .mk(taskName, new TaskCallback() {
                        @Override
                        public String doOperation() {
                            defaultBeginTask(taskName);

                            persistToDiskCritical();

                            return taskName;
                        }

                        @Override
                        public void onResult(String result) {
                            defaultEndTask(result);
                        }
                    })
                    .runMe();
        else
            writeInitSetupErrLine();
    }

    private static void prepareFlush() {
        isFlushing = true;
        persistence.persistToDisk(platformContext);
        http.setRequestTimeout(platformContext, HTTP_REQUEST_TIMEOUT_FLUSH_SEC);
    }

    private static synchronized void flushEventsCritical() {
        if (isInitialized)
            prepareFlush();
        else
            writeInitErrLine("Cannot flush events");
    }

    public static void flushEvents() {
        final String taskName = "flushEvents";
        final long now = GameEvent.nowFromEpoch();

        if (isTaskFactoryInitialized.get())
            taskFactory
                    .mk(taskName, new TaskCallback() {
                        @Override
                        public String doOperation() {
                            defaultBeginTask(taskName);

                            flushEventsCritical();

                            sendBatch(now, true);

                            return taskName;
                        }

                        @Override
                        public void onResult(String result) {
                            defaultEndTask(result);
                        }
                    })
                    .runMe();
        else
            writeInitSetupErrLine();
    }

    private static synchronized void deinitializeCritical(GameEvent deinit) {
        if (isInitialized) {
            String serializedEvent = encoder.encodeEvent(platformContext, deinit.toRaw());

            // NOTE: best effort
            List<String> gameEvents = new ArrayList<String>();
            gameEvents.add(serializedEvent);
            persistence.writeSerialized(platformContext,
                    encoder.getSeparator(platformContext),
                    encoder.getBatchBegin(platformContext),
                    currentSerializedHeader,
                    currentSerializedTags,
                    encoder.getEventsBegin(platformContext),
                    gameEvents,
                    deinit.getTimestamp(),
                    false,
                    false);
            prepareFlush();
            // NOTE: don't set isInitialized to false, due to pending batches over HTTP
        } else
            writeErrLine("Autofire de-initialization error: Not initialized");
    }

    public static void deinitialize(Initializer initializer) {
        final String taskName = "deinitialize";
        final long now = GameEvent.nowFromEpoch();

        final GameEvent deinit;
        if (initializer == null)
            deinit = new Deinit();
        else
            deinit = new Deinit().withTimestamp(initializer.timestamp);

        if (isTaskFactoryInitialized.get())
            taskFactory
                    .mk(taskName, new TaskCallback() {
                        @Override
                        public String doOperation() {
                            defaultBeginTask(taskName);

                            deinitializeCritical(deinit);

                            sendBatch(now, true);

                            return taskName;
                        }

                        @Override
                        public void onResult(String result) {
                            defaultEndTask(result);
                        }
                    })
                    .runMe();
        else
            writeInitSetupErrLine();
    }

    private static synchronized void shutdownTasksCritical() {
        if (isSetUp) {
            taskFactory.shutdown();
            isTaskFactoryInitialized.set(false);
        }
    }

    public static void shutdown() {
        final String taskName = "shutdown";

        if (isTaskFactoryInitialized.get())
            taskFactory
                    .mk(taskName, new TaskCallback() {
                        @Override
                        public String doOperation() {
                            defaultBeginTask(taskName);

                            shutdownTasksCritical();

                            return taskName;
                        }

                        @Override
                        public void onResult(String result) {
                            defaultEndTask(result);
                        }
                    })
                    .runMe();
        else
            writeInitSetupErrLine();
    }
}
