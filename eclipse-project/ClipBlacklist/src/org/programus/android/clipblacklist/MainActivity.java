package org.programus.android.clipblacklist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.programus.android.clipblacklist.data.BlacklistItem;
import org.programus.android.clipblacklist.widget.BlacklistAdapter;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;
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
//        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
            private int mmPrevCheckedCount;

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                Toast.makeText(getApplicationContext(), String.valueOf(listView.getCheckedItemCount()), Toast.LENGTH_LONG).show();
                return true;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.findItem(R.id.action_edit);
                item.setVisible(listView.getCheckedItemCount() == 1);
                return true;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(String.valueOf(checkedCount));
                if (this.mmPrevCheckedCount == 1 || checkedCount == 1) {
                    mode.invalidate();
                }
                this.mmPrevCheckedCount = checkedCount;
            }
        });
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
