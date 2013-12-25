package org.programus.android.clipblacklist.widget;

import java.util.List;

import org.programus.android.clipblacklist.R;
import org.programus.android.clipblacklist.data.BlacklistItem;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Switch;

public class BlacklistAdapter extends ArrayAdapter<BlacklistItem> {
    private static class ViewHolder {
        public CheckedTextView mmText;
        public Switch mmSwitch;
    }
    private final static int LAYOUT_RESOURCE = R.layout.list_row;
    
    private Activity context;

    public BlacklistAdapter(Activity context, BlacklistItem[] objects) {
        super(context, LAYOUT_RESOURCE, objects);
        this.context = context;
    }

    public BlacklistAdapter(Activity context, List<BlacklistItem> objects) {
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
            viewHolder.mmText = (CheckedTextView) rowView.findViewById(R.id.rowContent);
            viewHolder.mmSwitch = (Switch) rowView.findViewById(R.id.rowEnabled);
            rowView.setTag(viewHolder);
        }
        
        ViewHolder holder = (ViewHolder) rowView.getTag();
        BlacklistItem item = this.getItem(position);
        holder.mmText.setText(item.getContent());
        holder.mmSwitch.setChecked(item.isEnabeld());
        
        return rowView;
    }

}
