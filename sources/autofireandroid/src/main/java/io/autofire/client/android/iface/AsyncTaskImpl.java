package io.autofire.client.android.iface;

import android.os.AsyncTask;

import io.autofire.client.japi.iface.Task;
import io.autofire.client.japi.iface.TaskCallback;

public class AsyncTaskImpl extends AsyncTask<String, Void, String> implements Task {
    private TaskCallback callback;

    public AsyncTaskImpl(TaskCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        return callback.doOperation();
    }

    @Override
    protected void onPostExecute(String result) {
        callback.onResult(result);
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    public void runMe() {
        execute();
    }
}
