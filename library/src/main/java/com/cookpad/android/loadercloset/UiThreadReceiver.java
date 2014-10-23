package com.cookpad.android.loadercloset;

import android.support.v4.content.Loader;


public interface UiThreadReceiver<T, U> {

    public void onLoadFinished(Loader loader, T result);

    public void onLoadFailed(Loader loader, U failed);
}
