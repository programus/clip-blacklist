package org.programus.android.clipblacklist;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.programus.android.clipblacklist.data.LogRecord;
import org.programus.android.clipblacklist.widget.BlacklistItemDetail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Log Record detail screen. This fragment is
 * either contained in a {@link LogRecordListActivity} in two-pane mode (on
 * tablets) or a {@link LogRecordDetailActivity} on handsets.
 */
public class LogRecordDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String     ARG_ITEM_RECORD = "item_record";
    
    private LogRecord mRecord;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LogRecordDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_RECORD)) {
            if (this.mRecord == null) {
                this.mRecord = new LogRecord();
            }
            this.mRecord.load(getArguments(), ARG_ITEM_RECORD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_logrecord_detail, container, false);

        if (mRecord != null) {
            this.fillView(rootView);
        }

        return rootView;
    }
    
    private void fillView(View v) {
        this.fillTime(v);
        this.fillItem(v);
        this.fillClip(v);
    }
    
    private void fillTime(View v) {
        TextView time = (TextView) v.findViewById(R.id.log_detail_time);
        SimpleDateFormat fmt = new SimpleDateFormat(this.getString(R.string.log_detail_time_format), Locale.getDefault());
        time.setText(fmt.format(this.mRecord.getTime()));
    }
    
    private void fillItem(View v) {
        View itemDetail = v.findViewById(R.id.log_detail_item);
        BlacklistItemDetail detail = new BlacklistItemDetail(itemDetail);
        detail.setItem(this.mRecord.getBlockedItem());
    }
    
    private void fillClip(View v) {
        View clipDetail = v.findViewById(R.id.log_detail_clip);
        BlacklistItemDetail detail = new BlacklistItemDetail(clipDetail);
        detail.setClipData(this.mRecord.getBlockedClip(), false, true);
    }
}
