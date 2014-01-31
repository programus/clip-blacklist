package org.programus.android.clipblacklist.widget;

import java.util.List;

import org.programus.android.clipblacklist.R;
import org.programus.android.clipblacklist.data.BlacklistItem;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

/**
 * The adapter for black list which manage a {@link BlacklistItem} list.
 * @author programus
 */
public class BlacklistAdapter extends ArrayAdapter<BlacklistItem> {
    private static class ViewHolder {
        public BlacklistItemPreviewer mmPreviwer;
        public CompoundButton mmEnabled;
    }
    private final static int LAYOUT_RESOURCE = R.layout.list_row;
    
    private static CompoundButton.OnCheckedChangeListener mSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            BlacklistItem item = (BlacklistItem) buttonView.getTag();
            item.setEnabled(isChecked);
        }
    };
    
    private Activity context;

    /**
     * Constructor.
     * @param context
     * @param objects
     */
    public BlacklistAdapter(final Activity context, final BlacklistItem[] objects) {
        super(context, LAYOUT_RESOURCE, objects);
        this.context = context;
    }

    /**
     * Constructor.
     * @param context
     * @param objects
     */
    public BlacklistAdapter(final Activity context, final List<BlacklistItem> objects) {
        super(context, LAYOUT_RESOURCE, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = this.context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_row, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mmPreviwer = new BlacklistItemPreviewer(rowView);
            viewHolder.mmEnabled = (CompoundButton) rowView.findViewById(R.id.rowEnabled);
            viewHolder.mmEnabled.setOnCheckedChangeListener(mSwitchListener);
            rowView.setTag(viewHolder);
        }
        
        ViewHolder holder = (ViewHolder) rowView.getTag();
        BlacklistItem item = this.getItem(position);
        holder.mmPreviwer.setItem(item);
        holder.mmEnabled.setTag(item);
        holder.mmEnabled.setChecked(item.isEnabled());
        
        return rowView;
    }

}
