package io.autofire.client.japi.iface;

import java.util.Map;

public interface HTTPProvider {
    boolean isOnline(Object platformContext);

    void setRequestTimeout(Object platformContext, int secs);

    void postData(Object platformContext,
                  HelperHTTPResponseHandler responseHandler,
                  String url,
                  String contentType,
                  String acceptType,
                  Map<String, String> headers,
                  String body);
}
