package com.sys1yagi.loadercloset;

public interface UiThreadPageableReceiver<T, U> extends UiThreadReceiver<T, U> {

    public void onEndOfData();
}
