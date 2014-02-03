package org.programus.android.clipblacklist.widget;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.programus.android.clipblacklist.R;
import org.programus.android.clipblacklist.data.LogRecord;
import org.programus.android.clipblacklist.util.ActivityLog;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * An list adapter to display log record
 * @author programus
 *
 */
public class LogListAdapter extends CursorAdapter {
    private static class ViewHolder {
        public TextView mmTime;
        public BlacklistItemPreviewer mmPreviewer;
    }
    
    private LayoutInflater mInflater;
    private static SimpleDateFormat mDateFormat;

    /**
     * Constructor.
     * @param context
     * @param c
     * @param flags
     */
    public LogListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (this.mInflater == null) {
            this.mInflater = LayoutInflater.from(context);
        }
        View row = this.mInflater.inflate(R.layout.log_row, null);
        ViewHolder holder = new ViewHolder();
        holder.mmTime = (TextView) row.findViewById(R.id.log_time);
        holder.mmPreviewer = new BlacklistItemPreviewer(row);
        row.setTag(holder);
        return row;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        LogRecord record = ActivityLog.fetchLogRecord(cursor);
        if (mDateFormat == null) {
            mDateFormat = new SimpleDateFormat(context.getString(R.string.log_time_format), Locale.getDefault());
        }
        holder.mmTime.setText(mDateFormat.format(record.getTime()));
        holder.mmPreviewer.setClipData(record.getBlockedClip(), record.getBlockedItem().isCoerceText());
    }

}
