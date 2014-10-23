package com.cookpad.android.loadercloset;

import android.support.v4.content.Loader;

public class EmptyUiThreadReceiver implements UiThreadReceiver {

    private final static EmptyUiThreadReceiver INSTANCE = new EmptyUiThreadReceiver();

    public static EmptyUiThreadReceiver getInstance() {
        return INSTANCE;
    }

    @Override
    public void onLoadFinished(Loader loader, Object result) {
        throw new IllegalAccessError(
                "Should not call. this receiver is used for LoaderCloset.startParallel() or LoaderCloset.startSerial()");
    }

    @Override
    public void onLoadFailed(Loader loader, Object failed) {
        throw new IllegalAccessError(
                "Should not call. this receiver is used for LoaderCloset.startParallel() or LoaderCloset.startSerial()");
    }
}
