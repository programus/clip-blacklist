package org.programus.android.clipblacklist;

import java.util.ArrayList;
import java.util.List;

import org.programus.android.clipblacklist.data.BlacklistItem;
import org.programus.android.clipblacklist.dialog.ItemEditDialog;
import org.programus.android.clipblacklist.service.ClipMonitorService;
import org.programus.android.clipblacklist.util.ClipDataHelper;
import org.programus.android.clipblacklist.widget.BlacklistAdapter;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * The Main Setting Activity.
 * @author programus
 *
 */
public class MainActivity extends ListActivity implements ItemEditDialog.FinishedEditCallback {
    
    /**
     * the name for the {@link SharedPreferences}.
     */
    public final static String PREFS_NAME = "org.programus.android.clipblacklist.Preferences";
    private final static String KEY_LIST_COUNT = "blacklist.count";
    private final static String KEY_LIST_FORMAT = "blacklist.item(%d)";
    
    private List<BlacklistItem> mContents = new ArrayList<BlacklistItem>();
    private int mEditingIndex = -1;
    private BlacklistAdapter mAdapter;
    private ClipboardManager mClipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.loadContents();
        mAdapter = new BlacklistAdapter(this, this.mContents);
        this.setListAdapter(mAdapter);
        
        this.mClipboardManager = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);

        final ListView listView = this.getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                promptEditItem(position);
            }
        });
        
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
        
        this.startMonitorService();
    }
    
    /**
     * Load the black list from {@link SharedPreferences}.
     * @param context
     * @param list the list you want to reuse. It will be cleared before fill the contents.
     * @return the black list. It will be the same instance as the list you specified if any or a new {@link ArrayList} instance. 
     */
    public static List<BlacklistItem> loadBlacklist(final Context context, List<BlacklistItem> list) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int count = settings.getInt(KEY_LIST_COUNT, 0);
        if (list == null) {
            list = new ArrayList<BlacklistItem>(count);
        } else {
            list.clear();
        }
        for (int i = 0; i < count; i++) {
            list.add(BlacklistItem.loadInstance(settings, String.format(KEY_LIST_FORMAT, i)));
        }
        
        return list;
    }
    
    private void startMonitorService() {
        Intent intent = new Intent(this, ClipMonitorService.class);
        intent.putExtra(ClipMonitorService.KEY_FLAG, ClipMonitorService.FLAG_REFRESH_BLACKLIST);
        this.startService(intent);
    }

    private void loadContents() {
        loadBlacklist(this, this.mContents);
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        }
    }
    
    private void saveContents() {
        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(KEY_LIST_COUNT, this.mContents.size());
        int i = 0;
        for (BlacklistItem item : this.mContents) {
            item.save(editor, String.format(KEY_LIST_FORMAT, i++));
        }
        editor.apply();
        this.startMonitorService();
    }
    
    private interface CheckedItemCallback {
        public void process(List<BlacklistItem> contents, int position);
    }
    
    private void addBlacklistItem() {
    	if (this.hasClip()) {
    		this.selectAdd();
    	} else {
    		this.promptEditItem(-1);
    	}
    }
    
    private void selectAdd() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder
    		.setTitle(R.string.new_item_prompt)
    		.setItems(R.array.new_item_array, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					boolean clear = false;
					switch (which) {
					case 0:
						clear = true;
					case 1:
						addCurrentClipboardIntoList(clear);
						break;
					case 2:
					default:
						promptEditItem(-1);
						break;
					}
				}
			});
    	builder.create().show();
    }
    
    private boolean hasClip() {
    	boolean ret = false;
    	ClipData cd = this.mClipboardManager.getPrimaryClip();
    	if (cd != null && cd.getItemCount() > 0) {
    		ClipData.Item item = cd.getItemAt(0);
    		ret = 
    				item.getText() != null ||
    				item.getHtmlText() != null ||
    				item.getUri() != null ||
    				item.getIntent() != null;
    	}
    	
    	return ret;
    }
    
    private void addCurrentClipboardIntoList(boolean clear) {
    	ClipboardManager cm = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
    	ClipData cd = cm.getPrimaryClip();
    	if (this.hasClip()) {
    		this.addClipDataIntoList(cd);
    		this.mAdapter.notifyDataSetChanged();
    		this.saveContents();
    		if (clear) {
	    		this.clearClipboard();
    		}
    	} else {
    		Toast.makeText(this, R.string.no_clip, Toast.LENGTH_LONG).show();
    		this.promptEditItem(-1);
    	}
    }
    
    private void addClipDataIntoList(ClipData cd) {
    	if (cd.getItemCount() > 0) {
    		ClipData.Item item = cd.getItemAt(0);
    		ClipData store = new ClipData(cd.getDescription(), item);
			this.mContents.add(new BlacklistItem(store, true));
    	}
    }
    
    private void clearClipboard() {
    	this.mClipboardManager.setPrimaryClip(ClipDataHelper.getEmptyClipData());
    }
    
    private void promptEditItem(final int position) {
        BlacklistItem item = position >= 0 ? this.mContents.get(position) : null;
        this.mEditingIndex = position;
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
        Log.d(this.getClass().getName(), "EditingIndex: " + mEditingIndex + "/AppendNew: " + appendNew);
        if (appendNew) {
            this.mContents.add(item);
        } else {
            this.mContents.set(mEditingIndex, item);
        }
        Log.d(this.getClass().getName(), this.mContents.toString());
        this.saveContents();
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
        this.saveContents();
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
        	this.addBlacklistItem();
            break;
        default:
            processed = false;
        }
        return processed;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.saveContents();
    }

    @Override
    protected void onResume() {
        this.loadContents();
        super.onResume();
    }
}
