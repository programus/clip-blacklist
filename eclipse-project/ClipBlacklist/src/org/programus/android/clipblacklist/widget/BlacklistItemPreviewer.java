package org.programus.android.clipblacklist.widget;

import org.programus.android.clipblacklist.R;
import org.programus.android.clipblacklist.data.BlacklistItem;

import android.content.ClipData;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A class to preview the clipboard data by using the layout.clip_item_preview
 * @author programus
 *
 */
public class BlacklistItemPreviewer {
    private ViewGroup mDetailsLayout;
    private TextView mTextLabel;
    private TextView mHtmlLabel;
    private TextView mUriLabel;
    private TextView mIntentLabel;
    private TextView mText;
    private TextView mHtml;
    private TextView mUri;
    private TextView mIntent;

    private ViewGroup mCoerceLayout;
    private TextView mCoerceText;
    
    private ColorStateList mDefColor;
    private int disabledPromptColor;
    
    /**
     * Constructor.
     * @param parent the view include this sub layout
     */
    public BlacklistItemPreviewer(View parent) {
        this.init(parent);
        this.mDefColor = this.mTextLabel.getTextColors();
        disabledPromptColor = this.mTextLabel.getContext().getResources().getColor(R.color.clip_data_table_prompt_disabled_bg);
    }
    
    private void init(View parent) {
        this.mDetailsLayout = (ViewGroup) parent.findViewById(R.id.clip_preview_details_layout);
        
        this.mTextLabel = (TextView) parent.findViewById(R.id.clip_preview_text_label);
        this.mHtmlLabel = (TextView) parent.findViewById(R.id.clip_preview_html_label);
        this.mUriLabel = (TextView) parent.findViewById(R.id.clip_preview_uri_label);
        this.mIntentLabel = (TextView) parent.findViewById(R.id.clip_preview_intent_label);
        
        this.mText = (TextView) parent.findViewById(R.id.clip_preview_text);
        this.mHtml = (TextView) parent.findViewById(R.id.clip_preview_html);
        this.mUri = (TextView) parent.findViewById(R.id.clip_preview_uri);
        this.mIntent = (TextView) parent.findViewById(R.id.clip_preview_intent);

        this.mCoerceLayout = (ViewGroup) parent.findViewById(R.id.clip_preview_coerce_layout);
        
        this.mCoerceText = (TextView) parent.findViewById(R.id.clip_preview_coerce);
    }
    
    private void clear() {
        this.mCoerceText.setText("");
        this.mTextLabel.setBackgroundResource(R.color.clip_data_table_disabled_bg);
        this.mTextLabel.setTextColor(disabledPromptColor);
        this.mText.setBackgroundResource(R.color.clip_data_table_disabled_bg);
        this.mText.setText("");
        this.mHtmlLabel.setBackgroundResource(R.color.clip_data_table_disabled_bg);
        this.mHtmlLabel.setTextColor(disabledPromptColor);
        this.mHtml.setBackgroundResource(R.color.clip_data_table_disabled_bg);
        this.mHtml.setText("");
        this.mUriLabel.setBackgroundResource(R.color.clip_data_table_disabled_bg);
        this.mUriLabel.setTextColor(disabledPromptColor);
        this.mUri.setBackgroundResource(R.color.clip_data_table_disabled_bg);
        this.mUri.setText("");
        this.mIntentLabel.setBackgroundResource(R.color.clip_data_table_disabled_bg);
        this.mIntentLabel.setTextColor(disabledPromptColor);
        this.mIntent.setBackgroundResource(R.color.clip_data_table_disabled_bg);
        this.mIntent.setText("");
    }
    
    /**
     * Set a blacklist item to display
     * @param item
     */
    public void setItem(BlacklistItem item) {
        this.clear();
        if (item.isCoerceText()) {
            this.mDetailsLayout.setVisibility(View.GONE);
            this.mCoerceLayout.setVisibility(View.VISIBLE);
            this.mCoerceText.setText(item.getContent());
        } else {
            this.setClipData(item.getRawContent(), false);
        }
    }
    
    /**
     * Set a clip data to display
     * @param clip
     * @param isCoerceText
     */
    public void setClipData(ClipData clip, boolean isCoerceText) {
        this.setItem(clip.getItemCount() > 0 ? clip.getItemAt(0) : null, isCoerceText);
    }
    
    /**
     * Set a clip data item to display
     * @param item
     * @param isCoerceText
     */
    public void setItem(ClipData.Item item, boolean isCoerceText) {
        this.clear();
        if (isCoerceText) {
            this.mDetailsLayout.setVisibility(View.GONE);
            this.mCoerceLayout.setVisibility(View.VISIBLE);
            this.mCoerceText.setText(item.coerceToText(this.mCoerceText.getContext()));
        } else {
            this.mDetailsLayout.setVisibility(View.VISIBLE);
            this.mCoerceLayout.setVisibility(View.GONE);
            if (item != null) {
                CharSequence text = item.getText();
                String html = item.getHtmlText();
                Uri uri = item.getUri();
                Intent intent = item.getIntent();
                if (text != null) {
                    this.mTextLabel.setBackgroundResource(R.color.clip_data_table_prompt_bg);
                    this.mTextLabel.setTextColor(mDefColor);
                    this.mText.setBackgroundResource(0);
                    this.mText.setText(text);
                }
                if (html != null) {
                    this.mHtmlLabel.setBackgroundResource(R.color.clip_data_table_prompt_bg);
                    this.mHtmlLabel.setTextColor(mDefColor);
                    this.mHtml.setBackgroundResource(0);
                    this.mHtml.setText(html);
                }
                if (uri != null) {
                    this.mUriLabel.setBackgroundResource(R.color.clip_data_table_prompt_bg);
                    this.mUriLabel.setTextColor(mDefColor);
                    this.mUri.setBackgroundResource(0);
                    this.mUri.setText(uri.toString());
                }
                if (intent != null) {
                    this.mIntentLabel.setBackgroundResource(R.color.clip_data_table_prompt_bg);
                    this.mIntentLabel.setTextColor(mDefColor);
                    this.mIntent.setBackgroundResource(0);
                    this.mIntent.setText(intent.toUri(Intent.URI_INTENT_SCHEME));
                }
            }
        }
    }
}
