package org.programus.android.clipblacklist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.programus.android.clipblacklist.data.BlacklistItem;
import org.programus.android.clipblacklist.dialog.ItemEditDialog;
import org.programus.android.clipblacklist.widget.BlacklistAdapter;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity implements ItemEditDialog.FinishedEditCallback {
    
    private List<BlacklistItem> mContents = new ArrayList<BlacklistItem>();
    private BlacklistAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.loadContents();
        mAdapter = new BlacklistAdapter(this, this.mContents);
        this.setListAdapter(mAdapter);

        final ListView listView = this.getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
            private int mmPrevCheckedCount;

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean processed = true;
                int id = item.getItemId();
                switch (id) {
                case R.id.action_delete:
                    deleteCheckedItems();
                    mode.finish();
                    break;
                case R.id.action_sel_all:
                    checkAllItems();
                    break;
                case R.id.action_on:
                case R.id.action_off:
                    setCheckedItemsEnabled(id == R.id.action_on);
                    break;
                case R.id.action_edit:
                    promptEditCheckedItem();
                    mode.finish();
                    break;
                default:
                    processed = false;
                    break;
                }
                return processed;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                saveContents();
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
            item.setEnabled(c % 3 == 0);
            this.mContents.add(item);
        }
    }
    
    private void saveContents() {
        
    }
    
    private interface CheckedItemCallback {
        public void process(List<BlacklistItem> contents, int position);
    }
    
    private void promptEditItem(final int position) {
        BlacklistItem item = position >= 0 ? this.mContents.get(position) : null;
        ItemEditDialog dialog = ItemEditDialog.newInstance(item);
        dialog.show(getFragmentManager(), "edit_item");
    }
    
    private void promptEditCheckedItem() {
        if (this.getListView().getCheckedItemCount() == 1) {
            this.iterateCheckItems(new CheckedItemCallback() {
                @Override
                public void process(List<BlacklistItem> contents, int position) {
                    promptEditItem(position);
                }
            }, false);
        }
    }

    @Override
    public void onFinishedEdit(DialogFragment dialog, BlacklistItem item, boolean appendNew) {
        if (appendNew) {
            this.mContents.add(item);
        }
        this.mAdapter.notifyDataSetChanged();
    }
    
    private void iterateCheckItems(CheckedItemCallback action, boolean save) {
        SparseBooleanArray sba = this.getListView().getCheckedItemPositions();
        for (int i = sba.size() - 1; i >= 0; i--) {
            if (sba.valueAt(i)) {
                action.process(this.mContents, sba.keyAt(i));
            }
        }
        if (save) {
            this.saveContents();
        }
        this.mAdapter.notifyDataSetChanged();
    }
    
    private void deleteCheckedItems() {
        this.iterateCheckItems(new CheckedItemCallback() {
            @Override
            public void process(List<BlacklistItem> contents, int position) {
                contents.remove(position);
            }
        }, false);
    }
    
    private void checkAllItems() {
        ListView lv = this.getListView();
        for (int i = 0; i < lv.getCount(); i++) {
            lv.setItemChecked(i, true);
        }
    }
    
    private void setCheckedItemsEnabled(final boolean enabled) {
        this.iterateCheckItems(new CheckedItemCallback() {
            @Override
            public void process(List<BlacklistItem> contents, int position) {
                contents.get(position).setEnabled(enabled);
            }
        }, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean processed = true;
        int id = item.getItemId();
        switch (id) {
        case R.id.action_add:
            this.promptEditItem(-1);
            break;
        default:
            processed = false;
        }
        return processed;
    }
}
