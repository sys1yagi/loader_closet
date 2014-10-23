package com.cookpad.android.loadercloset;

public class LoaderResult<T, U> {

    private int loaderId;

    private T success;

    private U failed;

    private boolean isSuccess = false;

    public LoaderResult(int loaderId) {
        this.loaderId = loaderId;
    }

    public T getSuccess() {
        return success;
    }

    public void setSuccess(T success) {
        isSuccess = true;
        this.success = success;
    }

    public U getFailed() {
        return failed;
    }

    public void setFailed(U failed) {
        isSuccess = false;
        this.failed = failed;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public int getLoaderId() {
        return loaderId;
    }
}
