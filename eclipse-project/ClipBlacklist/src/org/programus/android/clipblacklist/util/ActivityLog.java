package org.programus.android.clipblacklist.util;

import org.programus.android.clipblacklist.data.BlacklistItem;
import org.programus.android.clipblacklist.data.LogRecord;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A log writer and reader to record all clipboard block activities.
 * @author programus
 *
 */
public class ActivityLog {
    private static class DbOpenHelper extends SQLiteOpenHelper {
        private static final String DB_NAME          = "clipblacklistlog.db";
        private static final int    DB_VERSION       = 1;

        private static final String TABLE_NAME           = "act_log";
        private static final String COL_ID               = "_id";
        private static final String COL_TYPE             = "act_type";
        private static final String COL_TIME             = "act_time";
        private static final String COL_CLIP             = "blocked_clip";
        private static final String COL_ITEM_COERCE      = "blocked_item_coerce_text";
        private static final String COL_ITEM_CONTENT     = "blocked_item_content";
        private static int          COL_ID_IDX           = -1;
        private static int          COL_TYPE_IDX         = -1;
        private static int          COL_TIME_IDX         = -1;
        private static int          COL_CLIP_IDX         = -1;
        private static int          COL_ITEM_COERCE_IDX  = -1;
        private static int          COL_ITEM_CONTENT_IDX = -1;
    

        public DbOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createLogTable = "create table " + TABLE_NAME + " (" + COL_ID + " integer primary key autoincrement not null unique, " + COL_TYPE + " integer not null, " + COL_TIME + " integer not null, " + COL_CLIP + " text not null, " + COL_ITEM_COERCE + " integer not null," + COL_ITEM_CONTENT + " text not null" + ");";
            db.execSQL(createLogTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(this.getClass().getName(), String.format("Upgrading database from ver %d to ver %d...", oldVersion, newVersion));
            db.execSQL("drop table if exists " + TABLE_NAME);
            this.onCreate(db);
        }

    }
    private Context mContext;
    private SQLiteOpenHelper mSqlite;
    
    private static ActivityLog mInstance;

    private ActivityLog(Context context) {
        this.mContext = context.getApplicationContext();
        this.mSqlite = new DbOpenHelper(mContext);
    }
    
    /**
     * Get the singleton instance of ActivityLog. If the instance had been created, the context parameter would be ignored.
     * @param context
     * @return the singleton instance
     */
    public static synchronized ActivityLog getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ActivityLog(context);
        }
        
        return mInstance;
    }
    
    /**
     * Log a block record.
     * @param clip
     * @param item
     */
    public void block(ClipData clip, BlacklistItem item) {
        LogRecord record = LogRecord.getNowLog(clip, item);
        record.setType(LogRecord.LOG_TYPE_BLOCK);
        this.insertLogRecord(record);
    }
    
    /**
     * Fetch an instance of {@link LogRecord} from a valid cursor.
     * @param cursor
     * @return the fetched LogRecord
     */
    public static LogRecord fetchLogRecord(Cursor cursor) {
        if (DbOpenHelper.COL_ID_IDX < 0) {
            DbOpenHelper.COL_ID_IDX = cursor.getColumnIndex(DbOpenHelper.COL_ID);
        }
        if (DbOpenHelper.COL_TIME_IDX < 0) {
            DbOpenHelper.COL_TIME_IDX = cursor.getColumnIndex(DbOpenHelper.COL_ID);
        }
        if (DbOpenHelper.COL_TYPE_IDX < 0) {
            DbOpenHelper.COL_TYPE_IDX = cursor.getColumnIndex(DbOpenHelper.COL_ID);
        }
        if (DbOpenHelper.COL_CLIP_IDX < 0) {
            DbOpenHelper.COL_CLIP_IDX = cursor.getColumnIndex(DbOpenHelper.COL_ID);
        }
        if (DbOpenHelper.COL_ITEM_COERCE_IDX < 0) {
            DbOpenHelper.COL_ITEM_COERCE_IDX = cursor.getColumnIndex(DbOpenHelper.COL_ID);
        }
        if (DbOpenHelper.COL_ITEM_CONTENT_IDX < 0) {
            DbOpenHelper.COL_ITEM_CONTENT_IDX = cursor.getColumnIndex(DbOpenHelper.COL_ID);
        }
        LogRecord record = new LogRecord();
        record.setId(cursor.getLong(DbOpenHelper.COL_ID_IDX));
        record.setTime(cursor.getLong(DbOpenHelper.COL_TIME_IDX));
        record.setType(cursor.getInt(DbOpenHelper.COL_TYPE_IDX));
        String clipString = cursor.getString(DbOpenHelper.COL_CLIP_IDX);
        record.setBlockedClip(ClipDataHelper.clipDataFromString(clipString));
        boolean isCoerce = cursor.getInt(DbOpenHelper.COL_ITEM_COERCE_IDX) != 0;
        String content = cursor.getString(DbOpenHelper.COL_ITEM_CONTENT_IDX);
        BlacklistItem item = new BlacklistItem(true);
        if (isCoerce) {
            item.setContent(content);
        } else {
            item.setRawContent(content);
        }
        record.setBlockedItem(item);

        return record;
    }
    
    private void insertLogRecord(LogRecord record) {
        ContentValues values = new ContentValues();
        values.put(DbOpenHelper.COL_TYPE, record.getType());
        values.put(DbOpenHelper.COL_TIME, record.getTimeAsLong());
        values.put(DbOpenHelper.COL_CLIP, record.getBlockedClipAsString());
        BlacklistItem item = record.getBlockedItem();
        boolean coerce = item.isCoerceText();
        values.put(DbOpenHelper.COL_ITEM_COERCE, coerce);
        values.put(DbOpenHelper.COL_ITEM_CONTENT, coerce ? item.getContent() : item.getRawContentAsString());

        SQLiteDatabase db = this.mSqlite.getWritableDatabase();
        try {
            db.insertOrThrow(DbOpenHelper.TABLE_NAME, null, values);
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), "Log record insert failed. ");
            e.printStackTrace();
        }
        db.close();
    }
}
