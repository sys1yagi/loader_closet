package com.cookpad.android.loadercloset.sample.activities;

import com.sys1yagi.loadercloset.sample.R;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends ActionBarActivity {

    enum Pages {
        ONE_SHOT_LOADER_ACTIVITY("OneShotLoader with Activity") {
            @Override
            public void open(ActionBarActivity activity) {
                super.open(activity);
            }
        },
        ONE_SHOT_LOADER_FRAGMENT("OneShotLoader with Fragment") {
            @Override
            public void open(ActionBarActivity activity) {
                super.open(activity);
            }
        },;

        private String title;

        Pages(String title) {
            this.title = title;
        }

        public void open(ActionBarActivity activity) {
            //
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, -1);
        for (Pages pages : Pages.values()) {
            adapter.add(pages.title);
        }
        listView.setAdapter(adapter);
    }
}
