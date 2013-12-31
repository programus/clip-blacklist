package org.programus.android.clipblacklist.data;

import java.io.Serializable;

import org.programus.android.clipblacklist.util.ClipDataHelper;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

/**
 * A class to store the data in the blacklist.
 * @author programus
 *
 */
public class BlacklistItem implements Serializable {
    private static final long serialVersionUID = -4100928148072568582L;

    private final static String SAVE_KEY_RAW = "%s.BlacklistItem.raw";
    private final static String SAVE_KEY_CONTENT = "%s.BlacklistItem.content";
    private final static String SAVE_KEY_ENABLED = "%s.BlacklistItem.enabled";
    
    public final static String TYPE_COERCE_TEXT = "CoerceText";

    private ClipData rawContent;
    private String content;
    private boolean enabled;
    
    private String rawCache;
    
    /**
     * Default constructor
     */
    public BlacklistItem() { }

    /**
     * Constructor
     * @param content
     * @param enabled
     */
    public BlacklistItem(final String content, final boolean enabled) {
        this.content = content;
        this.enabled = enabled;
    }
    
    /**
     * Constructor
     * @param clip
     * @param enabled
     */
    public BlacklistItem(final ClipData clip, final boolean enabled) {
    	this.setRawContent(clip);
    	this.setEnabled(enabled);
    }
    
    /**
     * 
     * @param enabled
     */
    public BlacklistItem(final boolean enabled) {
    	this((ClipData) null, enabled);
    }
    
    /**
     * Return the raw {#link ClipData} instance.
     * @return the raw{#link ClipData} instance
     */
    public ClipData getRawContent() {
		return rawContent;
	}
    
    public String getRawContentAsString() {
    	return rawCache;
    }

    /**
     * Set the raw {#link ClipData} instance
     * @param rawContent the raw {#link ClipData} instance
     */
	public void setRawContent(ClipData rawContent) {
		this.rawContent = rawContent;
		this.rawCache = ClipDataHelper.stringFromClipData(rawContent).toString();
	}

	public boolean isCoerceText() {
		return this.rawContent == null || this.content != null;
	}
	/**
     * Return the content
     * @return the content
     */
    public String getContent() {
        return content;
    }
    /**
     * Set the content
     * @param content the content
     */
    public void setContent(final String content) {
        this.content = content;
    }
    /**
     * Return true if enabled
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    /**
     * Set enabled
     * @param enabled
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getTypes() {
    	return this.isCoerceText() ? TYPE_COERCE_TEXT : ClipDataHelper.getTypeString(ClipDataHelper.getItemTypes(this.getRawContent().getItemAt(0)));
    }
    
    public String getDiaplayText() {
    	String ret = null;
    	if (this.isCoerceText()) {
    		ret = this.getContent();
    	} else {
    		ClipData.Item item = this.rawContent.getItemAt(0);
    		CharSequence text = item.getText();
    		String html = item.getHtmlText();
    		Intent intent = item.getIntent();
    		Uri uri = item.getUri();
    		if (intent != null) {
    			ret = intent.toUri(Intent.URI_INTENT_SCHEME);
    		} else if (uri != null) {
    			ret = uri.toString();
    		} else if (html != null) {
    			ret = html;
    		} else if (text != null) {
    			ret = text.toString();
    		}
    	}
    	
    	return ret;
    }
    
    /**
     * Save the item into {@link SharedPreferences}.
     * @param editor
     * @param key the key to store this item
     */
    public void save(final SharedPreferences.Editor editor, final String key) {
        editor.putBoolean(String.format(SAVE_KEY_ENABLED, key), this.enabled);
        editor.putString(String.format(SAVE_KEY_CONTENT, key), content);
        editor.putString(String.format(SAVE_KEY_RAW, key), this.rawCache);
    }
    
    /**
     * Load the item from {@link SharedPreferences}.
     * @param pref
     * @param key
     */
    public void load(final SharedPreferences pref, final String key) {
        this.enabled = pref.getBoolean(String.format(SAVE_KEY_ENABLED, key), false);
        this.content = pref.getString(String.format(SAVE_KEY_CONTENT, key), null);
        this.rawCache = pref.getString(String.format(SAVE_KEY_RAW, key), null);
        this.rawContent = ClipDataHelper.clipDataFromString(rawCache);
    }
    
    /**
     * Return the item loaded from {@link SharedPreferences}.
     * @param pref
     * @param key
     * @return the item loaded if exists or a new item.
     */
    public static BlacklistItem loadInstance(final SharedPreferences pref, final String key) {
        BlacklistItem item = new BlacklistItem();
        item.load(pref, key);
        return item;
    }
}
