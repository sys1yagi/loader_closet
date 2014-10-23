package com.cookpad.android.loadercloset;

import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public abstract class UiThreadParallelReceiver {

    private final static String TAG = "Async";

    private List<Loader> counter = new ArrayList<Loader>(5);

    private List<Loader> loaders = new ArrayList<Loader>(5);

    private List<LoaderResult> results = new ArrayList<LoaderResult>(5);

    void register(Loader loader) {
        counter.add(loader);
    }

    void finish(Loader loader, LoaderResult result) {
        if (counter.isEmpty()) {
            return;
        }
        counter.remove(loader);
        loaders.add(loader);
        results.add(result);
        if (counter.isEmpty()) {
            Collections.sort(loaders, loaderComparator);
            Collections.sort(results, loaderResultComparator);
            onLoadFinished(loaders, results);
        }
    }

    private Comparator<Loader> loaderComparator = new Comparator<Loader>() {
        @Override
        public int compare(Loader lhs, Loader rhs) {
            return lhs.getId() - rhs.getId();
        }
    };

    private Comparator<LoaderResult> loaderResultComparator = new Comparator<LoaderResult>() {
        @Override
        public int compare(LoaderResult lhs, LoaderResult rhs) {
            return lhs.getLoaderId() - rhs.getLoaderId();
        }
    };

    public abstract void onLoadFinished(List<Loader> loaders, List<LoaderResult> results);

}
