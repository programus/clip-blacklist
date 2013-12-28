package org.programus.android.clipblacklist.data;

import java.io.Serializable;

import android.content.SharedPreferences;

public class BlacklistItem implements Serializable {
    private static final long serialVersionUID = -4100928148072568582L;

    private final static String SAVE_KEY_CONTENT = "%s.BlacklistItem.content";
    private final static String SAVE_KEY_ENABLED = "%s.BlacklistItem.enabled";

    private String content;
    private boolean enabled;
    
    public BlacklistItem() { }
    public BlacklistItem(String content, boolean enabled) {
        this.content = content;
        this.enabled = enabled;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void save(SharedPreferences.Editor editor, String key) {
        editor.putBoolean(String.format(SAVE_KEY_ENABLED, key), this.enabled);
        editor.putString(String.format(SAVE_KEY_CONTENT, key), content);
    }
    
    public void load(SharedPreferences pref, String key) {
        this.enabled = pref.getBoolean(String.format(SAVE_KEY_ENABLED, key), false);
        this.content = pref.getString(String.format(SAVE_KEY_CONTENT, key), null);
    }
    
    public static BlacklistItem loadInstance(SharedPreferences pref, String key) {
        BlacklistItem item = new BlacklistItem();
        item.load(pref, key);
        return item;
    }
}
