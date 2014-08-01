package com.sys1yagi.loadercloset;

import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;


public abstract class UiThreadSerialReceiver {

    List<Integer> serialLoaderIds = new ArrayList<Integer>(5);

    List<Loader> loaders = new ArrayList<Loader>(5);

    List<LoaderResult> results = new ArrayList<LoaderResult>(5);

    void addSerialLoaderId(int loaderId) {
        serialLoaderIds.add(loaderId);
    }

    int getNextLoaderId() {
        if (serialLoaderIds.isEmpty()) {
            return -1;
        }
        int loaderId = serialLoaderIds.get(0);
        serialLoaderIds.remove(0);
        return loaderId;
    }

    boolean finish(Loader loader, LoaderResult result) {
        if (!result.isSuccess()) {
            onLoadFailed(loaders, loader, result);
            return false;
        }
        loaders.add(loader);
        results.add(result);
        return true;
    }

    void finishAllLoader() {
        onLoadFinished(loaders, results);
    }

    public abstract void onLoadFinished(List<Loader> loaders, List<LoaderResult> results);

    public abstract void onLoadFailed(List<Loader> completedLoaders, Loader failLoader,
            LoaderResult fail);

}
