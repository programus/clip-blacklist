package org.programus.android.clipblacklist.data;

import java.io.Serializable;

import org.programus.android.clipblacklist.util.ClipDataHelper;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

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
    
    /** Readable string for coerce text clip data */
    public final static String TYPE_COERCE_TEXT = "CoerceText";
    
    private static final String CD_LABEL = "Blacklist Clip";
    
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
    
    /**
     * Return the converted string of the stored clip data.
     * @return the converted string
     */
    public String getRawContentAsString() {
    	return rawCache;
    }

    /**
     * Set the raw {@link ClipData} instance
     * @param rawContent the raw {@link ClipData} instance
     */
	public void setRawContent(ClipData rawContent) {
		this.rawContent = rawContent;
		this.rawCache = ClipDataHelper.stringFromClipData(rawContent).toString();
	}
	
	/**
	 * Set the raw {@link ClipData} instance by specifying only item.
	 * @param item
	 */
	public void setRawContent(ClipData.Item item) {
	    ClipDescription cd = new ClipDescription(CD_LABEL, ClipDataHelper.EMPTY_STR_ARRAY);
	    this.setRawContent(new ClipData(cd, item));
	}

	/**
	 * Indicate whether this blacklist item is judged by coerce text instead of accurate clip data.
	 * @return true if judge by coerce text
	 */
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
    
    /**
     * Return the types in string.
     * @return the types
     */
    public String getTypes() {
    	return this.isCoerceText() ? TYPE_COERCE_TEXT : ClipDataHelper.getTypeString(ClipDataHelper.getItemTypes(this.getRawContent().getItemAt(0)));
    }
    
    /**
     * Return the types in integer flag.
     * If coerce to text, -1 will be returned.
     * @return the types in integer
     */
    public int getTypesFlag() {
        return this.isCoerceText() ? -1 : ClipDataHelper.getItemTypes(this.getRawContent().getItemAt(0));
    }
    
    /**
     * Return the content text. If there is intent in clip data, display intent, or display uri if uri included, or html and text at last.
     * @return the content in string.
     */
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
        editor.putBoolean(key, true);
        editor.putBoolean(String.format(SAVE_KEY_ENABLED, key), this.enabled);
        editor.putString(String.format(SAVE_KEY_CONTENT, key), content);
        editor.putString(String.format(SAVE_KEY_RAW, key), this.rawCache);
    }
    
    /**
     * Save the item into {@link Bundle}. 
     * @param bundle
     * @param key
     */
    public void save(final Bundle bundle, final String key) {
        bundle.putBoolean(key, true);
        bundle.putBoolean(String.format(SAVE_KEY_ENABLED, key), this.enabled);
        bundle.putString(String.format(SAVE_KEY_CONTENT, key), this.content);
        bundle.putString(String.format(SAVE_KEY_RAW, key), this.rawCache);
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
     * Load the item from {@link Bundle}.
     * @param bundle
     * @param key
     */
    public void load(final Bundle bundle, final String key) {
        this.enabled = bundle.getBoolean(String.format(SAVE_KEY_ENABLED, key), false);
        this.content = bundle.getString(String.format(SAVE_KEY_CONTENT, key), null);
        this.rawCache = bundle.getString(String.format(SAVE_KEY_RAW, key), null);
        this.rawContent = ClipDataHelper.clipDataFromString(this.rawCache);
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
