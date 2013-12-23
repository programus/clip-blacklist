package org.programus.android.clipblacklist;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends ListActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
