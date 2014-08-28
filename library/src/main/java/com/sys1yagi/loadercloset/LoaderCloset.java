package com.sys1yagi.loadercloset;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class LoaderCloset implements LoaderManager.LoaderCallbacks<LoaderResult> {

    int idCounter = -1;

    private LoaderManager loaderManager;

    private UiThreadParallelReceiver uiThreadParallelReceiver;

    private UiThreadSerialReceiver uiThreadSerialReceiver;

    private Map<Integer, Pair<Loader, UiThreadReceiver>> loaders
            = new HashMap<Integer, Pair<Loader, UiThreadReceiver>>();

    private Map<Integer, Pair<Loader, UiThreadReceiver>> parallelLoaders
            = new HashMap<Integer, Pair<Loader, UiThreadReceiver>>();

    private Map<Integer, Pair<Loader, UiThreadReceiver>> serialLoaders
            = new TreeMap<Integer, Pair<Loader, UiThreadReceiver>>();

    public LoaderCloset(LoaderManager loaderManager) {
        this.loaderManager = loaderManager;
    }

    public void registerLoader(Loader loader, UiThreadReceiver receiver) {
        register(loaders, loader, receiver);
    }

    public void registerLoaderForParallel(Loader loader) {
        register(parallelLoaders, loader, EmptyUiThreadReceiver.getInstance());
    }

    public void registerLoaderForSerial(Loader loader) {
        register(serialLoaders, loader, EmptyUiThreadReceiver.getInstance());
    }

    private void register(Map<Integer, Pair<Loader, UiThreadReceiver>> map, Loader loader,
            UiThreadReceiver receiver) {
        idCounter++;
        map.put(idCounter, new Pair(loader, receiver));
    }

    public void start() {
        for (Map.Entry<Integer, Pair<Loader, UiThreadReceiver>> entry : loaders.entrySet()) {
            loaderManager.initLoader(entry.getKey(), null, this);
        }
    }

    public void reload() {
        for (Map.Entry<Integer, Pair<Loader, UiThreadReceiver>> entry : loaders.entrySet()) {
            Loader loader = entry.getValue().first;
            loader.reset();
            loader.startLoading();
        }
    }

    public void reloadSerialize(UiThreadSerialReceiver receiver) {
        receiver.clear();
        uiThreadSerialReceiver = receiver;
        for (Integer key : serialLoaders.keySet()) {
            uiThreadSerialReceiver.addSerialLoaderId(key);
        }
        int loaderId = receiver.getNextLoaderId();
        if (serialLoaders.containsKey(loaderId)) {
            Loader loader = serialLoaders.get(loaderId).first;
            loader.reset();
            loader.startLoading();
        } else {
            uiThreadSerialReceiver.finishAllLoader();
        }
    }

    public void startNextPage(PageableLoader loader, UiThreadPageableReceiver receiver) {
        if (!loader.loadNextPage()) {
            receiver.onEndOfData();
        }
    }

    public void startParallel(UiThreadParallelReceiver receiver) {
        uiThreadParallelReceiver = receiver;
        for (Map.Entry<Integer, Pair<Loader, UiThreadReceiver>> entry : parallelLoaders
                .entrySet()) {
            uiThreadParallelReceiver.register(entry.getValue().first);
            loaderManager.initLoader(entry.getKey(), null, this);
        }
    }

    public void startSerial(UiThreadSerialReceiver receiver) {
        uiThreadSerialReceiver = receiver;
        for (Integer key : serialLoaders.keySet()) {
            uiThreadSerialReceiver.addSerialLoaderId(key);
        }
        int loaderId = receiver.getNextLoaderId();
        if (serialLoaders.containsKey(loaderId)) {
            loaderManager.initLoader(loaderId, null, this);
        } else {
            uiThreadSerialReceiver.finishAllLoader();
        }
    }

    void nextSerial(UiThreadSerialReceiver receiver, LoaderResult takeover) {
        int loaderId = receiver.getNextLoaderId();
        if (serialLoaders.containsKey(loaderId)) {
            Loader loader = serialLoaders.get(loaderId).first;
            ((OneShotLoader) loader).setPreviousResult(takeover);
            loaderManager.initLoader(loaderId, null, this);
        } else {
            uiThreadSerialReceiver.finishAllLoader();
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onLoadFinished(Loader loader, LoaderResult result) {
        if (uiThreadParallelReceiver != null && parallelLoaders.containsKey(loader.getId())) {
            uiThreadParallelReceiver.finish(loader, result);
        } else if (uiThreadSerialReceiver != null && serialLoaders.containsKey(loader.getId())) {
            if (uiThreadSerialReceiver.finish(loader, result)) {
                nextSerial(uiThreadSerialReceiver, result);
            }
        } else {
            UiThreadReceiver receiver = loaders.get(loader.getId()).second;
            if (result.isSuccess()) {
                receiver.onLoadFinished(loader, result.getSuccess());
            } else {
                receiver.onLoadFailed(loader, result.getFailed());
            }
        }
    }

    @Override
    public Loader onCreateLoader(int loaderId, Bundle bundle) {
        Map<Integer, Pair<Loader, UiThreadReceiver>> loaders = Collections.emptyMap();

        if (this.loaders.containsKey(loaderId)) {
            loaders = this.loaders;
        } else if (parallelLoaders.containsKey(loaderId)) {
            loaders = parallelLoaders;
        } else if (serialLoaders.containsKey(loaderId)) {
            loaders = serialLoaders;
        }

        return loaders.get(loaderId).first;
    }
}
