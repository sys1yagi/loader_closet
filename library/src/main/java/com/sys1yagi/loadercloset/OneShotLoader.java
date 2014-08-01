package com.sys1yagi.loadercloset;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public abstract class OneShotLoader<T, U> extends AsyncTaskLoader<LoaderResult<T, U>> {

    private final static String TAG = "Async";

    LoaderResult result;

    LoaderResult takeOver;

    public OneShotLoader(Context context) {
        super(context);
    }

    abstract public LoaderResult loadInBackground(LoaderResult takeOver);

    @Override
    public LoaderResult loadInBackground() {
        return loadInBackground(takeOver);
    }

    @Override
    public void deliverResult(LoaderResult data) {
        result = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (result != null) {
            Log.d(TAG, "deliverResult:" + getId());
            deliverResult(result);
        } else {
            Log.d(TAG, "forceLoad:" + getId());
            forceLoad();
        }
    }

    protected LoaderResult<T, U> success(T success) {
        LoaderResult<T, U> result = new LoaderResult<T, U>(getId());
        result.setSuccess(success);
        return result;
    }

    protected LoaderResult<T, U> failed(U failed) {
        LoaderResult<T, U> result = new LoaderResult<T, U>(getId());
        result.setFailed(failed);
        return result;
    }

    public LoaderResult getTakeOver() {
        return takeOver;
    }

    public void setTakeOver(LoaderResult takeOver) {
        this.takeOver = takeOver;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.result = null;
    }
}
