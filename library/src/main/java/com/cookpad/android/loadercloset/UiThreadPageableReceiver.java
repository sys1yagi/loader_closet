package com.cookpad.android.loadercloset;

public interface UiThreadPageableReceiver<T, U> extends UiThreadReceiver<T, U> {

    public void onEndOfData();
}
