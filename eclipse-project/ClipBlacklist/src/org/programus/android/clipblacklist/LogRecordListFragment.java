package org.programus.android.clipblacklist;

import org.programus.android.clipblacklist.data.LogRecord;
import org.programus.android.clipblacklist.util.ActivityLog;
import org.programus.android.clipblacklist.widget.LogListAdapter;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.ListView;


/**
 * A list fragment representing a list of Log Records. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link LogRecordDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class LogRecordListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks           mCallbacks               = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int                 mActivatedPosition       = ListView.INVALID_POSITION;
    
    private ActivityLog mLog;
    private LogListAdapter mAdapter;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         * @param record The log record in the selected item.
         */
        public void onItemSelected(LogRecord record);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
                                                 @Override
                                                 public void onItemSelected(LogRecord record) {}
                                             };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LogRecordListFragment() {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mLog == null) {
            this.mLog = ActivityLog.getInstance(this.getActivity());
        }
        this.setEmptyText(this.getString(R.string.empty_log));
        this.setListShown(false);
        this.mAdapter = new LogListAdapter(this.getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        this.setListAdapter(mAdapter);
        this.getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Uri uri = Uri.parse(String.format("%s/%s", ActivityLog.ClipBlacklistLogProvider.CONTENT_URI, id));
        Cursor c = this.getActivity().getContentResolver().query(uri, ActivityLog.ClipBlacklistLogProvider.ALL_COLS, null, null, null);
        if (c.moveToFirst()) {
            LogRecord record = ActivityLog.fetchLogRecord(c);

            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mCallbacks.onItemSelected(record);
        }
        c.close();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     * @param activateOnItemClick 
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return this.mLog.newCursorLoaderForAllLog();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.mAdapter.swapCursor(data);
        
        if (this.isResumed()) {
            this.setListShown(true);
        } else {
            this.setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.mAdapter.swapCursor(null);
    }
}
