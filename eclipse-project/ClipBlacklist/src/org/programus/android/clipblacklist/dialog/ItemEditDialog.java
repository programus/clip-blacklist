package org.programus.android.clipblacklist.dialog;

import java.net.URISyntaxException;

import org.programus.android.clipblacklist.R;
import org.programus.android.clipblacklist.data.BlacklistItem;
import org.programus.android.clipblacklist.util.ClipDataHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
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
    
    private int mTypes;
    private boolean mIsCoerceText;
    
    private RadioButton mCoerceToTextType;
    private ViewGroup mCoerceToTextLayout;
    private TextView mContentTextView;
    private CheckBox mEnabledCheckBox;
    
    private RadioButton mDetailType;
    private ViewGroup mDetailLayout;
    private CheckBox mClipTextCheck;
    private ViewGroup mClipTextLayout;
    private TextView mClipText;
    private TextView mClipTextMsg;
    private CheckBox mClipHtmlCheck;
    private ViewGroup mClipHtmlLayout;
    private TextView mClipHtml;
    private TextView mClipHtmlMsg;
    private CheckBox mClipUriCheck;
    private ViewGroup mClipUriLayout;
    private TextView mClipUri;
    private TextView mClipUriMsg;
    private CheckBox mClipIntentCheck;
    private ViewGroup mClipIntentLayout;
    private TextView mClipIntent;
    private TextView mClipIntentMsg;
    
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
            item.save(args, KEY_ITEM);
            dialog.setArguments(args);
        }
        return dialog;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = this.getArguments();
        final boolean appendNew = args == null || !args.containsKey(KEY_ITEM);
        this.mItem = new BlacklistItem(true);
        if (!appendNew) {
            this.mItem.load(args, KEY_ITEM);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        LayoutInflater inflater = this.getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit, null);
        this.initFromView(view);
        this.setupListeners();
        this.setViewFromItem();
        builder
            .setView(view)
            .setTitle(R.string.dialog_prompt)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null);
        final AlertDialog d = builder.create();
        d.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button okButton = d.getButton(Dialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (setItemFromView()) {
                            if (mCallback != null) {
                                mCallback.onFinishedEdit(ItemEditDialog.this, mItem, appendNew);
                            }
                            d.dismiss();
                        }
                    }
                });
            }
        });
        return d;
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
        this.mCoerceToTextType = (RadioButton) view.findViewById(R.id.coerce_to_text_type);
        this.mCoerceToTextLayout = (ViewGroup) view.findViewById(R.id.clip_coerce_to_text_layout);
        this.mContentTextView = (TextView) view.findViewById(R.id.dialog_content);
        this.mEnabledCheckBox = (CheckBox) view.findViewById(R.id.dialog_item_enabled);
        
        this.mDetailType = (RadioButton) view.findViewById(R.id.detail_type);
        this.mDetailLayout = (ViewGroup) view.findViewById(R.id.clip_detail_layout);
        this.mClipTextCheck = (CheckBox) view.findViewById(R.id.clip_text_check);
        this.mClipTextLayout = (ViewGroup) view.findViewById(R.id.clip_text_layout);
        this.mClipText = (TextView) view.findViewById(R.id.clip_text);
        this.mClipTextMsg = (TextView) view.findViewById(R.id.clip_text_message);
        this.mClipHtmlCheck = (CheckBox) view.findViewById(R.id.clip_html_check);
        this.mClipHtmlLayout = (ViewGroup) view.findViewById(R.id.clip_html_layout);
        this.mClipHtml = (TextView) view.findViewById(R.id.clip_html);
        this.mClipHtmlMsg = (TextView) view.findViewById(R.id.clip_html_message);
        this.mClipUriCheck = (CheckBox) view.findViewById(R.id.clip_uri_check);
        this.mClipUriLayout = (ViewGroup) view.findViewById(R.id.clip_uri_layout);
        this.mClipUri = (TextView) view.findViewById(R.id.clip_uri);
        this.mClipUriMsg = (TextView) view.findViewById(R.id.clip_uri_message);
        this.mClipIntentCheck = (CheckBox) view.findViewById(R.id.clip_intent_check);
        this.mClipIntentLayout = (ViewGroup) view.findViewById(R.id.clip_intent_layout);
        this.mClipIntent = (TextView) view.findViewById(R.id.clip_intent);
        this.mClipIntentMsg = (TextView) view.findViewById(R.id.clip_intent_message);
    }
    
    private void setupListeners() {
        this.mCoerceToTextType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsCoerceText = isChecked;
                if (isChecked) {
                    ClipData.Item item = getItemFromDetailPart(false);
                    if (item != null && mContentTextView.getText().length() <= 0) {
                        mContentTextView.setText(item.coerceToText(getActivity()));
                    }
                }
                updateCheckAndVisibility(mIsCoerceText, mTypes);
                if (isChecked) {
                    mContentTextView.requestFocus();
                }
            }
        });
        this.mDetailType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsCoerceText = !isChecked;
                updateCheckAndVisibility(mIsCoerceText, mTypes);
            }
        });
        this.mClipTextCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTypes = isChecked ? mTypes | ClipDataHelper.ITEM_TYPE_TEXT : mTypes & (~ClipDataHelper.ITEM_TYPE_TEXT);
                updateCheckAndVisibility(mIsCoerceText, mTypes);
                if (isChecked) {
                    mClipText.requestFocus();
                }
            }
        });
        this.mClipHtmlCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTypes = isChecked ? mTypes | ClipDataHelper.ITEM_TYPE_HTML : mTypes & (~ClipDataHelper.ITEM_TYPE_HTML);
                updateCheckAndVisibility(mIsCoerceText, mTypes);
                if (isChecked) {
                    mClipHtml.requestFocus();
                }
            }
        });
        this.mClipUriCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTypes = isChecked ? mTypes | ClipDataHelper.ITEM_TYPE_URI : mTypes & (~ClipDataHelper.ITEM_TYPE_URI);
                updateCheckAndVisibility(mIsCoerceText, mTypes);
                if (isChecked) {
                    mClipUri.requestFocus();
                }
            }
        });
        this.mClipIntentCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTypes = isChecked ? mTypes | ClipDataHelper.ITEM_TYPE_INTENT : mTypes & (~ClipDataHelper.ITEM_TYPE_INTENT);
                updateCheckAndVisibility(mIsCoerceText, mTypes);
                if (isChecked) {
                    mClipIntent.requestFocus();
                }
            }
        });
    }
    
    private void updateCheckAndVisibility(boolean isCoerceText, int typeFlag) {
        this.mCoerceToTextType.setChecked(isCoerceText);
        this.mCoerceToTextLayout.setVisibility(isCoerceText ? View.VISIBLE : View.GONE);
        this.mDetailType.setChecked(!isCoerceText);
        this.mDetailLayout.setVisibility(isCoerceText ? View.GONE : View.VISIBLE);
        if (!isCoerceText) {
            this.mClipTextCheck.setChecked((typeFlag & ClipDataHelper.ITEM_TYPE_TEXT) != 0);
            this.mClipTextLayout.setVisibility(this.mClipTextCheck.isChecked() ? View.VISIBLE : View.GONE);
            this.mClipHtmlCheck.setChecked((typeFlag & ClipDataHelper.ITEM_TYPE_HTML) != 0);
            this.mClipHtmlLayout.setVisibility(this.mClipHtmlCheck.isChecked() ? View.VISIBLE : View.GONE);
            this.mClipUriCheck.setChecked((typeFlag & ClipDataHelper.ITEM_TYPE_URI) != 0);
            this.mClipUriLayout.setVisibility(this.mClipUriCheck.isChecked() ? View.VISIBLE : View.GONE);
            this.mClipIntentCheck.setChecked((typeFlag & ClipDataHelper.ITEM_TYPE_INTENT) != 0);
            this.mClipIntentLayout.setVisibility(this.mClipIntentCheck.isChecked() ? View.VISIBLE : View.GONE);
        }
        this.clearMessages();
    }
    
    private void clearMessages() {
        this.mClipTextMsg.setText("");
        this.mClipHtmlMsg.setText("");
        this.mClipUriMsg.setText("");
        this.mClipIntentMsg.setText("");
    }
    
    private boolean setItemFromView() {
        this.clearMessages();
        boolean noError = true;
        mItem.setEnabled(this.mEnabledCheckBox.isChecked());
        if (this.mIsCoerceText) {
            mItem.setContent(this.mContentTextView.getText().toString());
        } else {
            ClipData.Item item = this.getItemFromDetailPart(true);
            noError = item != null;
            if (noError) {
                this.mItem.setRawContent(item);
            }
        }
        return noError;
    }
    
    private ClipData.Item getItemFromDetailPart(boolean showMsg) {
        ClipData.Item item = null;
        boolean noError = true;
        boolean hasText = (this.mTypes & ClipDataHelper.ITEM_TYPE_TEXT) != 0;
        CharSequence text = hasText ? this.mClipText.getText() : null;
        if (hasText && text.length() <= 0) {
            if (showMsg) {
                this.mClipTextMsg.setText(R.string.err_empty);
            }
            noError = false;
        }
        boolean hasHtml = (this.mTypes & ClipDataHelper.ITEM_TYPE_HTML) != 0;
        String html = hasHtml ? this.mClipHtml.getText().toString() : null;
        if (hasHtml && html.length() <= 0) {
            if (showMsg) {
                this.mClipHtmlMsg.setText(R.string.err_empty);
            }
            noError = false;
        }
        boolean hasUri = (this.mTypes & ClipDataHelper.ITEM_TYPE_URI) != 0;
        CharSequence uriText = hasUri ? this.mClipUri.getText() : null;
        if (hasUri && uriText.length() <= 0) {
            if (showMsg) {
                this.mClipUriMsg.setText(R.string.err_empty);
            }
            noError = false;
        }
        Uri uri = hasUri && noError ? Uri.parse(uriText.toString()) : null;
        boolean hasIntent = (this.mTypes & ClipDataHelper.ITEM_TYPE_INTENT) != 0;
        CharSequence intentText = hasIntent ? this.mClipIntent.getText() : null;
        if (hasIntent && intentText.length() <= 0) {
            if (showMsg) {
                this.mClipIntentMsg.setText(R.string.err_empty);
            }
            noError = false;
        }
        Intent intent = null;
        if (hasIntent && noError) {
            try {
                intent = Intent.parseUri(this.mClipIntent.getText().toString(), Intent.URI_INTENT_SCHEME);
            } catch (URISyntaxException e) {
                try {
                    intent = Intent.parseUri(intentText.toString(), 0);
                } catch (URISyntaxException ex) {
                    if (showMsg) {
                        this.mClipIntentMsg.setText(R.string.err_invalid_intent);
                    }
                    noError = false;
                }
            }
        }
        if (!(hasText || hasHtml || hasUri || hasIntent)) {
            if (showMsg) {
                this.mClipTextMsg.setText(R.string.err_no_detail_content);
                this.mClipHtmlMsg.setText(R.string.err_no_detail_content);
                this.mClipUriMsg.setText(R.string.err_no_detail_content);
                this.mClipIntentMsg.setText(R.string.err_no_detail_content);
            }
            noError = false;
        }
        if (noError) {
            item = new ClipData.Item(text, html, intent, uri);
        }
        return item;
    }
    
    private void setViewFromItem() {
        this.mIsCoerceText = this.mItem.isCoerceText();
        this.mTypes = this.mItem.getTypesFlag();
        this.mEnabledCheckBox.setChecked(mItem.isEnabled());
        this.mContentTextView.setText(mItem.getContent());
        if (!this.mIsCoerceText) {
            ClipData clip = this.mItem.getRawContent();
            if (clip.getItemCount() > 0) {
                ClipData.Item item = clip.getItemAt(0);
                if (item.getText() != null) {
                    this.mClipText.setText(item.getText());
                }
                if (item.getHtmlText() != null) {
                    this.mClipHtml.setText(item.getHtmlText());
                }
                if (item.getUri() != null) {
                    this.mClipUri.setText(item.getUri().toString());
                }
                if (item.getIntent() != null) {
                    this.mClipIntent.setText(item.getIntent().toUri(Intent.URI_INTENT_SCHEME).toString());
                }
            }
        }
        this.updateCheckAndVisibility(this.mIsCoerceText, this.mTypes);
    }
}
