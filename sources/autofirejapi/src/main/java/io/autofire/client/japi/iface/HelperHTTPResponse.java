package io.autofire.client.japi.iface;

public class HelperHTTPResponse {
    public int code;
    public String body;

    public HelperHTTPResponse(int code, String body) {
        this.code = code;
        this.body = body;
    }

    public final boolean succeeded() {
        return code >= 200 && code < 300;
    }

    public boolean isDiscardable() {
        return succeeded() || (code == 400 || code == 404);
    }

    @Override
    public String toString() {
        String b = body;
        if (b == null)
            b = "";

        return "{\t\"code\": " + code + ",\t\"body\": \"" + b + "\"\t}";
    }
}
