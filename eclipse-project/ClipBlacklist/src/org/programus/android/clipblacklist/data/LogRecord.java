package org.programus.android.clipblacklist.data;

import java.util.Date;

import org.programus.android.clipblacklist.util.ClipDataHelper;

import android.content.ClipData;
import android.os.Bundle;

/**
 * A log record
 * @author programus
 *
 */
public class LogRecord {
    /** Block blacklist type */
    public final static int LOG_TYPE_BLOCK = 1;
    private final static String[] LOG_TYPE_STR = {null, "block"};
    
    private final static String SAVE_KEY_ID = "%s.LogRecord.id";
    private final static String SAVE_KEY_TIME = "%s.LogRecord.time";
    private final static String SAVE_KEY_TYPE = "%s.LogRecord.type";
    private final static String SAVE_KEY_CLIP = "%s.LogRecord.blockedClip";
    private final static String SAVE_KEY_ITEM = "%s.LogRecord.blockedItem";

    private long id;
    private Date time;
    private int type;
    private ClipData blockedClip;
    private BlacklistItem blockedItem;
    
    /**
     * Save this instance into bundle.
     * @param bundle
     * @param key
     */
    public void save(Bundle bundle, String key) {
        bundle.putBoolean(key, true);
        bundle.putLong(String.format(SAVE_KEY_ID, key), this.getId());
        bundle.putLong(String.format(SAVE_KEY_TIME, key), this.getTimeAsLong());
        bundle.putInt(String.format(SAVE_KEY_TYPE, key), this.getType());
        bundle.putString(String.format(SAVE_KEY_CLIP, key), this.getBlockedClipAsString());
        this.getBlockedItem().save(bundle, String.format(SAVE_KEY_ITEM, key));
    }
    
    /**
     * load the LogRecord from bundle
     * @param bundle
     * @param key
     */
    public void load(Bundle bundle, String key) {
        if (bundle.containsKey(key)) {
            this.setId(bundle.getLong(String.format(SAVE_KEY_ID, key)));
            this.setTime(bundle.getLong(String.format(SAVE_KEY_TIME, key)));
            this.setType(bundle.getInt(String.format(SAVE_KEY_TYPE, key)));
            this.setBlockedClip(bundle.getString(String.format(SAVE_KEY_CLIP, key)));
            this.blockedItem = new BlacklistItem();
            this.blockedItem.load(bundle, String.format(SAVE_KEY_ITEM, key));
        }
    }
    
    /**
     * Return an instance with the current time as log time.
     * @return an instance
     */
    public static LogRecord getNowLog() {
        LogRecord record = new LogRecord();
        record.setTime(new Date());
        return record;
    }
    
    /**
     * Return an instance with the current time as log time.
     * @param clip
     * @param item
     * @return an instance
     */
    public static LogRecord getNowLog(ClipData clip, BlacklistItem item) {
        LogRecord record = getNowLog();
        record.setBlockedClip(clip);
        record.setBlockedItem(item);
        return record;
    }
    
    /**
     * ID used by system
     * @return ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * set ID
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * The time
     * @return the time
     */
    public Date getTime() {
        return time;
    }
    
    /**
     * 
     * @param time
     */
    public void setTime(Date time) {
        this.time = time;
    }
    
    /**
     * The time in long.
     * @return time in long
     */
    public long getTimeAsLong() {
        return time.getTime();
    }
    
    /**
     * Set time by a long value
     * @param time
     */
    public void setTime(long time) {
        this.time = new Date(time);
    }
    
    /**
     * Type of the log.
     * @return type of the log. Currently, only block.
     */
    public int getType() {
        return type;
    }
    
    /**
     * Set the type
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }
    
    /**
     * Return the type as string
     * @return the string
     */
    public String getTypeAsString() {
        return LOG_TYPE_STR[type];
    }

    /**
     * Return the ClipData instance that blocked.
     * @return blocked clip data
     */
    public ClipData getBlockedClip() {
        return blockedClip;
    }
    
    /**
     * Return the ClipData instance as a string
     * @return blocked clip data as a string
     */
    public String getBlockedClipAsString() {
        return ClipDataHelper.toString(ClipDataHelper.stringFromClipData(blockedClip));
    }

    /**
     * Set the blocked clip
     * @param blockedClip
     */
    public void setBlockedClip(ClipData blockedClip) {
        this.blockedClip = blockedClip;
    }

    /**
     * Set the blocked clip
     * @param blockedClip
     */
    public void setBlockedClip(String blockedClip) {
        this.blockedClip = ClipDataHelper.clipDataFromString(blockedClip);
    }

    /**
     * Get the item triggered the blocking
     * @return the blocked item
     */
    public BlacklistItem getBlockedItem() {
        return blockedItem;
    }

    /**
     * Set the item triggered the blocking
     * @param blockedItem
     */
    public void setBlockedItem(BlacklistItem blockedItem) {
        this.blockedItem = blockedItem;
    }

}
