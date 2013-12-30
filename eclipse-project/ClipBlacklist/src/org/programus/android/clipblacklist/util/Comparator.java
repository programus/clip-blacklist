package org.programus.android.clipblacklist.util;

import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Comparator {
    public static boolean equals(Intent a, Intent b) {
        boolean ret = false;
        if (a != null && b != null && a.filterEquals(b)) {
            Bundle ba = a.getExtras();
            Bundle bb = b.getExtras();
            ret = equals(ba, bb);
        }
        
        return ret;
    }
    
    public static boolean equals(Parcelable a, Parcelable b) {
        boolean ret = false;
        if (a != null && b != null) {
            Parcel pa = Parcel.obtain();
            Parcel pb = Parcel.obtain();
            a.writeToParcel(pa, 0);
            b.writeToParcel(pb, 0);
            byte[] da = pa.marshall();
            byte[] db = pb.marshall();
            ret = Arrays.equals(da, db);
            pa.recycle();
            pb.recycle();
        }
        return ret;
    }
}
