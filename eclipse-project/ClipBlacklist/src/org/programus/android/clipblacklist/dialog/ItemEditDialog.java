package org.programus.android.clipblacklist.dialog;

import org.programus.android.clipblacklist.R;
import org.programus.android.clipblacklist.data.BlacklistItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * The dialog to input the content of a blacklist item. This dialog would be used for both append new and edit existed items. 
 * @author programus
 *
 */
public class ItemEditDialog extends DialogFragment {
    /**
     * An interface to process item edited when finished editing. The activity uses this dialog is recommended to implement this interface. 
     */
    public static interface FinishedEditCallback {
        /**
         * In this method, you can process the item edited or add some post process of editting.
         * @param dialog the edit dialog
         * @param item the item edited
         * @param appendNew indicate whether this item is new created
         */
        public void onFinishedEdit(DialogFragment dialog, BlacklistItem item, boolean appendNew);
    }
    
    private final static String KEY_ITEM = "dialog.item";

    private BlacklistItem mItem;
    
    private TextView mConentTextView;
    private CheckBox mEnabledCheckBox;
    
    private FinishedEditCallback mCallback;
    
    /**
     * Create a new dialog with the specified item. 
     * @param item the item you want to edit. Use null if you want to create a new one.
     * @return a dialog instance. You can call {@link #show(android.app.FragmentManager, String)} to display it.
     */
    public static ItemEditDialog newInstance(BlacklistItem item) {
        ItemEditDialog dialog = new ItemEditDialog();
        if (item != null) {
            Bundle args = new Bundle();
            args.putSerializable(KEY_ITEM, item);
            dialog.setArguments(args);
        }
        return dialog;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = this.getArguments();
        final boolean appendNew = args == null || !args.containsKey(KEY_ITEM);
        this.mItem = !appendNew ? (BlacklistItem) args.getSerializable(KEY_ITEM) : new BlacklistItem(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        LayoutInflater inflater = this.getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit, null);
        this.initFromView(view);
        this.setViewFromItem();
        builder
            .setView(view)
            .setTitle(R.string.dialog_prompt)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setItemFromView();
                    if (mCallback != null) {
                        mCallback.onFinishedEdit(ItemEditDialog.this, mItem, appendNew);
                    }
                }
            })
            .setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mCallback = (FinishedEditCallback) activity;
        } catch (ClassCastException e) {
            Log.w(this.getClass().getName(), activity.toString() + " does not implement FinishedEditCallback.");
        }
    }

    private void initFromView(View view) {
        mConentTextView = (TextView) view.findViewById(R.id.dialog_content);
        mEnabledCheckBox = (CheckBox) view.findViewById(R.id.dialog_item_enabled);
    }
    
    private void setItemFromView() {
        mItem.setContent(this.mConentTextView.getText().toString());
        mItem.setEnabled(this.mEnabledCheckBox.isChecked());
    }
    
    private void setViewFromItem() {
        mConentTextView.setText(mItem.getContent());
        mEnabledCheckBox.setChecked(mItem.isEnabled());
    }
}
