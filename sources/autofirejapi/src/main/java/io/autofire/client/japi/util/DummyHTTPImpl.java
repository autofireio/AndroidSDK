package io.autofire.client.japi.util;

import java.util.Map;

import io.autofire.client.japi.iface.HTTPProvider;
import io.autofire.client.japi.iface.HelperHTTPResponse;
import io.autofire.client.japi.iface.HelperHTTPResponseHandler;

public class DummyHTTPImpl implements HTTPProvider {
    private static boolean online = true;
    private static int flakyEvery = 0;
    private static int notFoundEvery = 0;
    private static int n = 0;

    public static void setOnline(boolean online) {
        DummyHTTPImpl.online = online;
    }

    public static void setFlakyEvery(int flakyEvery) {
        DummyHTTPImpl.flakyEvery = flakyEvery;
    }

    public static void setNotFoundEvery(int notFoundEvery) {
        DummyHTTPImpl.notFoundEvery = notFoundEvery;
    }

    public boolean isOnline(Object platformContext) {
        return online;
    }

    public void setRequestTimeout(Object platformContext, int secs) {
    }

    private void HandleHTTPResponse(Object platformContext,
                                    HelperHTTPResponseHandler responseHandler,
                                    int code,
                                    String body) {
        HelperHTTPResponse response = new HelperHTTPResponse(code, body);
        responseHandler.handleResponse(platformContext, response);
    }

    public void postData(Object platformContext,
                         HelperHTTPResponseHandler responseHandler,
                         String url,
                         String contentType,
                         String acceptType,
                         Map<String, String> headers,
                         String body) {
        if (!online)
            HandleHTTPResponse(platformContext, responseHandler, 0, null);
        else {
            n++;
            if (flakyEvery > 0 && n % flakyEvery == 0)
                HandleHTTPResponse(platformContext, responseHandler,
                        500, "Internal Server Error");
            else if (notFoundEvery > 0 && n % notFoundEvery == 0)
                HandleHTTPResponse(platformContext, responseHandler,
                        404, "Not Found");
            else
                HandleHTTPResponse(platformContext, responseHandler,
                        200, "OK");
        }
    }
}
