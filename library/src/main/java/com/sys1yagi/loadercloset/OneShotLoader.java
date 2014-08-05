package com.sys1yagi.loadercloset;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.concurrent.CountDownLatch;

public abstract class OneShotLoader<T, U> extends AsyncTaskLoader<LoaderResult<T, U>> {

    LoaderResult result;

    LoaderResult previousResult;

    CountDownLatch countDownLatch;

    public OneShotLoader(Context context) {
        super(context);
        countDownLatch = new CountDownLatch(1);
    }

    abstract public void loadInBackground(LoaderResult previousResult);

    @Override
    public LoaderResult loadInBackground() {
        loadInBackground(previousResult);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return result;
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
            deliverResult(result);
        } else {
            forceLoad();
        }
    }

    protected void success(T success) {
        result = new LoaderResult<T, U>(getId());
        result.setSuccess(success);
        countDownLatch.countDown();
    }

    protected void failure(U failure) {
        result = new LoaderResult<T, U>(getId());
        result.setFailed(failure);
        countDownLatch.countDown();
    }

    public LoaderResult getPreviousResult() {
        return previousResult;
    }

    public void setPreviousResult(LoaderResult previousResult) {
        this.previousResult = previousResult;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.result = null;
    }
}
