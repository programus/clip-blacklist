package org.programus.android.clipblacklist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.programus.android.clipblacklist.data.BlacklistItem;
import org.programus.android.clipblacklist.widget.BlacklistAdapter;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity{
    
    private List<BlacklistItem> mContents = new ArrayList<BlacklistItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.loadContents();
        BlacklistAdapter adapter = new BlacklistAdapter(this, this.mContents);
        this.setListAdapter(adapter);

        final ListView listView = this.getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private void loadContents() {
        this.mContents.clear();
        for (char c = 0x20; c < 0x7f; c++) {
            char[] l = new char[5];
            Arrays.fill(l, c);
            BlacklistItem item = new BlacklistItem();
            item.setContent(new String(l));
            item.setEnabeld(c % 3 == 0);
            this.mContents.add(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this.getApplicationContext(), String.valueOf(this.getListView().getCheckedItemCount()), Toast.LENGTH_LONG).show();
        return super.onOptionsItemSelected(item);
    }
}
