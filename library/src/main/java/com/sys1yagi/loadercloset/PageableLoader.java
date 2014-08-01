package com.sys1yagi.loadercloset;

import android.content.Context;

public abstract class PageableLoader<T, U> extends OneShotLoader<T, U> {

    public PageableLoader(Context context) {
        super(context);
    }

    public abstract boolean prepareNextPage();

    public boolean loadNextPage() {
        if (prepareNextPage()) {
            forceLoad();
            return true;
        } else {
            return false;
        }
    }
}
