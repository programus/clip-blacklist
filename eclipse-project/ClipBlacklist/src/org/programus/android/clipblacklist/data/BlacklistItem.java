package org.programus.android.clipblacklist.data;

import android.content.SharedPreferences;

public class BlacklistItem {
    private final static String SAVE_KEY_CONTENT = "%s.BlacklistItem.content";
    private final static String SAVE_KEY_ENABLED = "%s.BlacklistItem.enabled";

    private String content;
    private boolean enabeld;
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public boolean isEnabeld() {
        return enabeld;
    }
    public void setEnabeld(boolean enabeld) {
        this.enabeld = enabeld;
    }
    
    public void save(SharedPreferences.Editor editor, String key) {
        editor.putBoolean(String.format(SAVE_KEY_ENABLED, key), this.enabeld);
        editor.putString(String.format(SAVE_KEY_CONTENT, key), content);
    }
    
    public void load(SharedPreferences pref, String key) {
        this.enabeld = pref.getBoolean(String.format(SAVE_KEY_ENABLED, key), false);
        this.content = pref.getString(String.format(SAVE_KEY_CONTENT, key), null);
    }
    
    public static BlacklistItem loadInstance(SharedPreferences pref, String key) {
        BlacklistItem item = new BlacklistItem();
        item.load(pref, key);
        return item;
    }
}
