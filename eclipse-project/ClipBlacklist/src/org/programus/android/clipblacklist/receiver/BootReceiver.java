package org.programus.android.clipblacklist.receiver;

import java.util.List;

import org.programus.android.clipblacklist.MainActivity;
import org.programus.android.clipblacklist.data.BlacklistItem;
import org.programus.android.clipblacklist.service.ClipMonitorService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * An broadcast receiver to receive the boot broadcast so that the service
 * could be started on boot.
 * 
 * @author programus
 * 
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        List<BlacklistItem> blacklist = MainActivity.loadBlacklist(context, null);
        if (blacklist != null && blacklist.size() > 0) {
            Intent sIntent = new Intent(context, ClipMonitorService.class);
            sIntent.putExtra(ClipMonitorService.KEY_FLAG, ClipMonitorService.FLAG_BOOT);
            context.startService(sIntent);
        }
    }
}
