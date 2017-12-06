package io.autofire.client.japi.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.autofire.client.japi.event.Utils;
import io.autofire.client.japi.iface.HTTPProvider;
import io.autofire.client.japi.iface.HelperHTTPResponse;
import io.autofire.client.japi.iface.HelperHTTPResponseHandler;

public class BasicHTTPImpl implements HTTPProvider {
    private int timeoutHint = 10;

    public boolean isOnline(Object platformContext) {
        return true;
    }

    public void setRequestTimeout(Object platformContext, int secs) {
        if (secs > 0)
            timeoutHint = secs;
    }

    // See:
    //   http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java
    //   http://www.rgagnon.com/javadetails/java-HttpUrlConnection-with-GZIP-encoding.html
    //   http://android-developers.blogspot.in/2011/09/androids-http-clients.html
    public HelperHTTPResponse httpRequest(String url,
                                          String method,
                                          Map<String, String> headers,
                                          String body) {
        try {
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

            conn.setConnectTimeout(timeoutHint * 1000);
            conn.setReadTimeout(timeoutHint * 1000);

            conn.setRequestMethod(method);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                conn.setRequestProperty(key, value);
            }

            if (body != null) {
                conn.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(body);
                wr.flush();
                wr.close();
            }

            int responseCode = conn.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();

            return new HelperHTTPResponse(responseCode, response.toString());
        } catch (SocketTimeoutException e) {
            return new HelperHTTPResponse(
                    HttpURLConnection.HTTP_CLIENT_TIMEOUT, "Timeout Exception");
        } catch (IOException e) {
            return new HelperHTTPResponse(
                    0, "I/O Error");
        }
    }

    public void get(Object platformContext,
                    HelperHTTPResponseHandler responseHandler,
                    String url,
                    String acceptType,
                    Map<String, String> headers) {
        Map<String, String> getHeaders;
        if (headers == null)
            getHeaders = new HashMap<String, String>();
        else
            getHeaders = new HashMap<String, String>(headers);
        if (!Utils.isNullOrEmpty(acceptType))
            getHeaders.put("Accept", acceptType);

        HelperHTTPResponse response = httpRequest(url, "GET", getHeaders,
                null);
        responseHandler.handleResponse(platformContext, response);
    }

    public void postForm(Object platformContext,
                         HelperHTTPResponseHandler responseHandler,
                         String url,
                         String acceptType,
                         Map<String, String> headers,
                         Map<String, String> body) {
        try {
            Map<String, String> postHeaders;
            if (headers == null)
                postHeaders = new HashMap<String, String>();
            else
                postHeaders = new HashMap<String, String>(headers);
            postHeaders.put("Content-Type", "application/x-www-form-urlencoded");
            if (!Utils.isNullOrEmpty(acceptType))
                postHeaders.put("Accept", acceptType);

            Set<String> keys = body.keySet();
            Iterator<String> keyIter = keys.iterator();
            StringBuilder data = new StringBuilder();
            for (int i = 0; keyIter.hasNext(); i++) {
                String key = keyIter.next();
                if (i != 0)
                    data.append("&");
                data.append(key).append("=").append(URLEncoder.encode(body.get(key), "UTF-8"));
            }

            HelperHTTPResponse response = httpRequest(url, "POST", postHeaders,
                    data.toString());
            responseHandler.handleResponse(platformContext, response);
        } catch (UnsupportedEncodingException e) {
            HelperHTTPResponse response = new HelperHTTPResponse(
                    0, "Unsupported Encoding");
            responseHandler.handleResponse(platformContext, response);
        }
    }

    public void postData(Object platformContext,
                         HelperHTTPResponseHandler responseHandler,
                         String url,
                         String contentType,
                         String acceptType,
                         Map<String, String> headers,
                         String body) {

        Map<String, String> postHeaders;
        if (headers == null)
            postHeaders = new HashMap<String, String>();
        else
            postHeaders = new HashMap<String, String>(headers);
        if (!Utils.isNullOrEmpty(contentType))
            postHeaders.put("Content-Type", contentType);
        if (!Utils.isNullOrEmpty(acceptType))
            postHeaders.put("Accept", acceptType);

        HelperHTTPResponse response = httpRequest(url, "POST", postHeaders, body);
        responseHandler.handleResponse(platformContext, response);
    }
}
