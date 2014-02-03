package org.programus.android.clipblacklist.util;

import org.programus.android.clipblacklist.data.BlacklistItem;
import org.programus.android.clipblacklist.data.LogRecord;

import android.content.ClipData;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.util.Log;


/**
 * A log writer and reader to record all clipboard block activities.
 * @author programus
 *
 */
public class ActivityLog {
    
    /**
     * Content Provider for Log db
     * @author programus
     *
     */
    public static class ClipBlacklistLogProvider extends ContentProvider {
        /** Authority */
        public final static String AUTHORITY = "org.programus.android.clipblacklist.util.ActivityLog.ClipBlacklistLogProvider";
        /** Base path (Table Name) */
        private final static String BASE_PATH = "act_log";
        /** For matcher */
        public final static int LOG_DB = 100;
        /** For matcher */
        public final static int LOG_DB_ID = 200;
        /** Content URI */
        public final static Uri CONTENT_URI = Uri.parse(String.format("content://%s/%s", AUTHORITY, BASE_PATH));
        public final static String CONTENT_TYPE = String.format("%s/%s", ContentResolver.CURSOR_DIR_BASE_TYPE, "logs");
        public final static String CONTENT_ITEM_TYPE = String.format("%s/%s", ContentResolver.CURSOR_ITEM_BASE_TYPE, "log");

        /** Column _id */
        public static final String   _ID                  = "_id";
        /** Column act_type */
        public static final String   TYPE                 = "act_type";
        /** Column act_time */
        public static final String   TIME                 = "act_time";
        /** Column blocked_clip */
        public static final String   CLIP                 = "blocked_clip";
        /** Column blocked_item_coerce_text */
        public static final String   ITEM_COERCE          = "blocked_item_coerce_text";
        /** Column blocked_item_content */
        public static final String   ITEM_CONTENT         = "blocked_item_content";

        /** All column array */
        public static final String[] ALL_COLS = {
            _ID, TYPE, TIME, CLIP, ITEM_COERCE, ITEM_CONTENT
        };
        public final static int          COL_ID_IDX           = 0;
        public final static int          COL_TYPE_IDX         = 1;
        public final static int          COL_TIME_IDX         = 2;
        public final static int          COL_CLIP_IDX         = 3;
        public final static int          COL_ITEM_COERCE_IDX  = 4;
        public final static int          COL_ITEM_CONTENT_IDX = 5;

        private static final String DB_NAME    = "clipblacklistlog.db";
        private static final int    DB_VERSION = 1;

        private SQLiteOpenHelper mDbHelper;
        
        private final static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        
        static {
            sUriMatcher.addURI(AUTHORITY, BASE_PATH, LOG_DB);
            sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", LOG_DB_ID);
        }

        @Override
        public boolean onCreate() {
            this.mDbHelper  = new SQLiteOpenHelper(this.getContext(), DB_NAME, null, DB_VERSION) {
                @Override
                public void onCreate(SQLiteDatabase db) {
                    String createLogTable = "create table " + BASE_PATH + 
                        " (" + 
                        _ID + " integer primary key autoincrement not null unique, " + 
                        TYPE + " integer not null, " + 
                        TIME + " integer not null, " + 
                        CLIP + " text not null, " + 
                        ITEM_COERCE + " integer not null," + 
                        ITEM_CONTENT + " text not null" + ");";
                    db.execSQL(createLogTable);
                }

                @Override
                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                    Log.w(this.getClass().getName(), String.format("Upgrading database from ver %d to ver %d...", oldVersion, newVersion));
                    db.execSQL("drop table if exists " + BASE_PATH);
                    this.onCreate(db);
                }

            };
            return true;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            Cursor c = null;
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            queryBuilder.setTables(BASE_PATH);
            switch (sUriMatcher.match(uri)) {
            case LOG_DB_ID:
                queryBuilder.appendWhere(String.format("%s=%s", _ID, uri.getLastPathSegment()));
                break;
            case LOG_DB:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
            SQLiteDatabase db = this.mDbHelper.getReadableDatabase();
            c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            c.setNotificationUri(this.getContext().getContentResolver(), uri);
            return c;
        }

        @Override
        public String getType(Uri uri) {
            return null;
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            Uri ret = null;
            switch (sUriMatcher.match(uri)) {
            case LOG_DB:
            {
                SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
                long id = db.insert(BASE_PATH, null, values);
                this.getContext().getContentResolver().notifyChange(uri, null);
                ret = Uri.withAppendedPath(uri, String.valueOf(id));
                db.close();
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
            return ret;
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            int count = 0;
            SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
            switch (sUriMatcher.match(uri)) {
            case LOG_DB_ID:
                String idWhere = String.format("%s=%s", _ID, uri.getLastPathSegment());
                count = db.delete(BASE_PATH, idWhere + (selection == null || selection.trim().length() <= 0 ? "" : " and " + selection), selectionArgs);
                break;
            case LOG_DB:
                count = db.delete(BASE_PATH, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
            this.getContext().getContentResolver().notifyChange(uri, null);
            db.close();
            return count;
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            int count = 0;
            SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
            switch (sUriMatcher.match(uri)) {
            case LOG_DB_ID:
                String idWhere = String.format("%s=%s", _ID, uri.getLastPathSegment());
                count = db.update(BASE_PATH, values, idWhere + (selection == null || selection.trim().length() <= 0 ? "" : " and " + selection), selectionArgs);
                break;
            case LOG_DB:
                count = db.update(BASE_PATH, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
            this.getContext().getContentResolver().notifyChange(uri, null);
            db.close();
            return count;
        }
    }

    private Context mContext;
    
    private static ActivityLog mInstance;

    private ActivityLog(Context context) {
        this.mContext = context.getApplicationContext();
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
     * Get all log record from database.
     * @return all log record.
     */
    public Cursor queryAllLog() {
        return this.mContext.getContentResolver().query(ClipBlacklistLogProvider.CONTENT_URI, ClipBlacklistLogProvider.ALL_COLS, null, null, String.format("%s %s", ClipBlacklistLogProvider.TIME, "DESC"));
    }
    
    /**
     * Return a CursorLoader that could retrieve all data.
     * @return CursorLoader
     */
    public CursorLoader newCursorLoaderForAllLog() {
        return new CursorLoader(this.mContext, ClipBlacklistLogProvider.CONTENT_URI, ClipBlacklistLogProvider.ALL_COLS, null, null, String.format("%s %s", ClipBlacklistLogProvider.TIME, "DESC"));
    }
    
    /**
     * Fetch an instance of {@link LogRecord} from a valid cursor.
     * @param cursor
     * @return the fetched LogRecord
     */
    public static LogRecord fetchLogRecord(Cursor cursor) {
        LogRecord record = new LogRecord();
        record.setId(cursor.getLong(ClipBlacklistLogProvider.COL_ID_IDX));
        record.setTime(cursor.getLong(ClipBlacklistLogProvider.COL_TIME_IDX));
        record.setType(cursor.getInt(ClipBlacklistLogProvider.COL_TYPE_IDX));
        String clipString = cursor.getString(ClipBlacklistLogProvider.COL_CLIP_IDX);
        record.setBlockedClip(ClipDataHelper.clipDataFromString(clipString));
        boolean isCoerce = cursor.getInt(ClipBlacklistLogProvider.COL_ITEM_COERCE_IDX) != 0;
        String content = cursor.getString(ClipBlacklistLogProvider.COL_ITEM_CONTENT_IDX);
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
        values.put(ClipBlacklistLogProvider.TYPE, record.getType());
        values.put(ClipBlacklistLogProvider.TIME, record.getTimeAsLong());
        values.put(ClipBlacklistLogProvider.CLIP, record.getBlockedClipAsString());
        BlacklistItem item = record.getBlockedItem();
        boolean coerce = item.isCoerceText();
        values.put(ClipBlacklistLogProvider.ITEM_COERCE, coerce);
        values.put(ClipBlacklistLogProvider.ITEM_CONTENT, coerce ? item.getContent() : item.getRawContentAsString());
        
        this.mContext.getContentResolver().insert(ClipBlacklistLogProvider.CONTENT_URI, values);
    }
}
