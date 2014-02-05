package org.programus.android.clipblacklist.widget;

import org.programus.android.clipblacklist.R;
import org.programus.android.clipblacklist.data.BlacklistItem;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A class to display the clipboard data by using the layout.clip_item_detail
 * @author programus
 *
 */
public class BlacklistItemDetail {
    private ViewGroup mDescLayout;
    private ViewGroup mTextLayout;
    private ViewGroup mHtmlLayout;
    private ViewGroup mUriLayout;
    private ViewGroup mIntentLayout;
    private ViewGroup mCoerceLayout;
    private TextView mDesc;
    private TextView mMime;
    private TextView mText;
    private TextView mHtml;
    private TextView mUri;
    private TextView mIntent;
    private TextView mCoerceText;
    
    /**
     * Constructor.
     * @param parent the view include this sub layout
     */
    public BlacklistItemDetail(View parent) {
        this.init(parent);
    }
    
    private void init(View parent) {
        this.mDescLayout = (ViewGroup) parent.findViewById(R.id.clip_detail_description_layout);
        this.mTextLayout = (ViewGroup) parent.findViewById(R.id.clip_detail_text_layout);
        this.mHtmlLayout = (ViewGroup) parent.findViewById(R.id.clip_detail_html_layout);
        this.mUriLayout = (ViewGroup) parent.findViewById(R.id.clip_detail_uri_layout);
        this.mIntentLayout = (ViewGroup) parent.findViewById(R.id.clip_detail_intent_layout);
        this.mCoerceLayout = (ViewGroup) parent.findViewById(R.id.clip_detail_coerce_layout);
        
        this.mDesc = (TextView) parent.findViewById(R.id.clip_detail_description);
        this.mMime = (TextView) parent.findViewById(R.id.clip_detail_mime);
        this.mText = (TextView) parent.findViewById(R.id.clip_detail_text);
        this.mHtml = (TextView) parent.findViewById(R.id.clip_detail_html);
        this.mUri = (TextView) parent.findViewById(R.id.clip_detail_uri);
        this.mIntent = (TextView) parent.findViewById(R.id.clip_detail_intent);
        this.mCoerceText = (TextView) parent.findViewById(R.id.clip_detail_coerce);
    }
    
    private void clear() {
        this.mDescLayout.setVisibility(View.GONE);
        this.mCoerceLayout.setVisibility(View.GONE);
        this.mTextLayout.setVisibility(View.GONE);
        this.mHtmlLayout.setVisibility(View.GONE);
        this.mUriLayout.setVisibility(View.GONE);
        this.mIntentLayout.setVisibility(View.GONE);
        this.mDesc.setText("");
        this.mMime.setText("");
        this.mCoerceText.setText("");
        this.mText.setText("");
        this.mHtml.setText("");
        this.mUri.setText("");
        this.mIntent.setText("");
    }
    
    /**
     * Set a blacklist item to display
     * @param item
     */
    public void setItem(BlacklistItem item) {
        this.clear();
        if (item.isCoerceText()) {
            this.mCoerceLayout.setVisibility(View.VISIBLE);
            this.mCoerceText.setText(item.getContent());
        } else {
            this.setClipData(item.getRawContent(), false, false);
        }
    }
    
    /**
     * Set a clip data to display
     * @param clip
     * @param isCoerceText
     * @param showDescription 
     */
    public void setClipData(ClipData clip, boolean isCoerceText, boolean showDescription) {
        this.clear();
        if (showDescription) {
            this.mDescLayout.setVisibility(View.VISIBLE);
            ClipDescription desc = clip.getDescription();
            this.mDesc.setText(desc.getLabel());
            StringBuilder sb = new StringBuilder();
            int n = desc.getMimeTypeCount();
            for (int i = 0; i < n; i++) {
                sb.append(desc.getMimeType(i));
                if (i < n - 1) {
                    sb.append(",");
                }
            }
            this.mMime.setText(sb);
        }
        this.setItem(clip != null && clip.getItemCount() > 0 ? clip.getItemAt(0) : null, isCoerceText);
    }
    
    /**
     * Set a clip data item to display
     * @param item
     * @param isCoerceText
     */
    public void setItem(ClipData.Item item, boolean isCoerceText) {
        if (isCoerceText) {
            this.mCoerceLayout.setVisibility(View.VISIBLE);
            this.mCoerceText.setText(item.coerceToText(this.mCoerceText.getContext()));
        } else {
            if (item != null) {
                CharSequence text = item.getText();
                String html = item.getHtmlText();
                Uri uri = item.getUri();
                Intent intent = item.getIntent();
                if (text != null) {
                    this.mTextLayout.setVisibility(View.VISIBLE);
                    this.mText.setText(text);
                }
                if (html != null) {
                    this.mHtmlLayout.setVisibility(View.VISIBLE);
                    this.mHtml.setText(html);
                }
                if (uri != null) {
                    this.mUriLayout.setVisibility(View.VISIBLE);
                    this.mUri.setText(uri.toString());
                }
                if (intent != null) {
                    this.mIntentLayout.setVisibility(View.VISIBLE);
                    this.mIntent.setText(intent.toUri(Intent.URI_INTENT_SCHEME));
                }
            }
        }
    }
}