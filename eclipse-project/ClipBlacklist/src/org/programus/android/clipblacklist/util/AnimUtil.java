package org.programus.android.clipblacklist.util;

import org.programus.android.clipblacklist.R;

import android.app.Activity;
import android.app.ActivityOptions;
import android.os.Bundle;

/**
 * Animation Utility
 * @author programus
 *
 */
public class AnimUtil {

    /**
     * Animation for back.
     * @param owner
     */
    public static void transBack(Activity owner) {
        owner.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
    
    /**
     * Return the forward bundle for startActivity() method.
     * @param owner
     * @return forward bundle
     */
    public static Bundle getTransFwdBundle(Activity owner) {
        return ActivityOptions.makeCustomAnimation(owner, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
    }
}
