package org.programus.android.clipblacklist.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.programus.android.clipblacklist.MainActivity;
import org.programus.android.clipblacklist.data.BlacklistItem;

import android.app.Notification;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ClipMonitorService extends Service {
    public final static String KEY_FLAG = "ClipboardBlacklist.Service.Flag";
    public final static int FLAG_BOOT = 0;
    public final static int FLAG_REFRESH_BLACKLIST = 1;

    private final static String FILE_NAME = "ClipboardBlacklist.Clip";
    private ClipboardManager mClipManager;
    private static int notificationId = 5;
	/** a notification to keep the service alive when task manager try to kill it */
    private static Notification notification;
    private List<BlacklistItem> blacklist; 
    boolean monitoring = false;

    private ClipboardManager.OnPrimaryClipChangedListener clipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            if (blacklist == null) {
                blacklist = getEnabledBlacklist(blacklist);
            }
            ClipboardManager cm = getClipboardManager();
            ClipData cd = cm.getPrimaryClip();
            if (isBlackClip(cd, blacklist)) {
                ClipData prevCd = loadClipData();
                Log.d(this.getClass().getName(), "loaded prev clip:" + prevCd.getItemCount());
                cm.setPrimaryClip(prevCd);
                Log.d(this.getClass().getName(), "replaced clip");
            } else {
                saveClipData(cd);
            }
        }
    };

    private ClipboardManager getClipboardManager() {
        if (mClipManager == null) {
            mClipManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        }
        return mClipManager;
    }
    
    private List<BlacklistItem> getEnabledBlacklist(List<BlacklistItem> list) {
        List<BlacklistItem> all = MainActivity.loadBlacklist(getApplicationContext(), null);
        if (list == null) {
            list = new ArrayList<BlacklistItem>();
        }
        for (BlacklistItem item : all) {
            if (item.isEnabled()) {
                list.add(item);
            }
        }
        return list;
    }
    
    private boolean isBlackClip(ClipData cd, List<BlacklistItem> list) {
        boolean ret = false;
        ClipData.Item item = cd.getItemAt(0);
        if (item != null) {
            CharSequence cs = item.coerceToText(getApplicationContext());
            if (cs != null) {
                String content = cs.toString();
                for (BlacklistItem bi : list) {
                    if (bi.isEnabled() && content.equals(bi.getContent())) {
                        ret = true;
                        break;
                    }
                }
            }
        }
        return ret;
    }
    
    private void saveClipData(ClipData cd) {
        Parcel parcel = Parcel.obtain();
        cd.writeToParcel(parcel, 0);
        byte[] data = parcel.marshall();
        FileOutputStream out = null;
        try {
            out = this.openFileOutput(FILE_NAME, MODE_PRIVATE);
            out.write(data);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(this.getClass().getName(), "saved clip: " + cd);
    }
    
    private ClipData loadClipData() {
        ClipData cd = null;
        FileInputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = this.openFileInput(FILE_NAME);
            Parcel parcel = Parcel.obtain();
            out = new ByteArrayOutputStream(in.available());
            for (int n = in.read(); n >= 0; n = in.read()) {
                out.write(n);
            }
            out.flush();
            byte[] data = out.toByteArray();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            cd = ClipData.CREATOR.createFromParcel(parcel);
            parcel.recycle();
        } catch (FileNotFoundException e) {
            Log.w(this.getClass().getName(), "Clip data storing file does not exist.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        Log.d(this.getClass().getName(), "loaded clip data: " + cd);
        
        return cd == null ? ClipData.newPlainText("", "") : cd;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mClipManager = this.getClipboardManager();
		if (notification == null) {
		    // build a notification to show nothing just to keep this service alive
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getApplicationContext());
			notification = builder.build();
		}
    }

    private void startMonitorClipboard() {
        if (!this.monitoring) {
            ClipboardManager cm = this.getClipboardManager();
            cm.removePrimaryClipChangedListener(clipChangedListener);
            cm.addPrimaryClipChangedListener(clipChangedListener);
            this.monitoring = true;
        }
    }
    
    private void stopMonitorClipboard() {
        if (this.monitoring) {
            ClipboardManager cm = this.getClipboardManager();
            cm.removePrimaryClipChangedListener(clipChangedListener);
            this.monitoring = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        int flag = FLAG_BOOT;
        if (intent != null) {
            flag = intent.getIntExtra(KEY_FLAG, FLAG_BOOT);
        }
        Log.d(this.getClass().getName(), "Service started: " + flag);
        switch (flag) {
        case FLAG_BOOT:
        case FLAG_REFRESH_BLACKLIST:
            this.blacklist = this.getEnabledBlacklist(this.blacklist);
            if (this.blacklist.isEmpty()) {
                this.stopMonitorClipboard();
                this.stopSelf();
            } else {
                this.startMonitorClipboard();
            }
            break;
        }
        this.startForeground(notificationId, notification);
        return START_STICKY;
    }

}
