package org.programus.android.clipblacklist.util;

import java.util.Arrays;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Comparator {
    public static boolean equals(Intent a, Intent b) {
        boolean ret = a == b;
        if (!ret && a != null && b != null && a.filterEquals(b)) {
            Bundle ba = a.getExtras();
            Bundle bb = b.getExtras();
            ret = equals(ba, bb);
        }
        
        return ret;
    }
    
    public static boolean equals(Parcelable a, Parcelable b) {
        boolean ret = a == b;
        if (!ret && a != null && b != null) {
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
    
    public static boolean approxEquals(ClipData a, ClipData b) {
    	Log.d(Comparator.class.getSimpleName(), "approx eq: " + a + " vs " + b);
    	boolean ret = a == b;
    	if (!ret && a != null && b != null && a.getItemCount() > 0 && b.getItemCount() > 0) {
    		ClipData.Item ia = a.getItemAt(0);
    		ClipData.Item ib = b.getItemAt(0);
    		ret = equals(ia, ib);
    	}
    	Log.d(Comparator.class.getSimpleName(), "result: " + ret);
    	
    	return ret;
    }
    
    public static boolean equals(ClipData a, ClipData b) {
    	boolean ret = a == b;
    	if (!ret && a != null && b != null && a.getItemCount() == b.getItemCount()) {
    		ret = equals(a.getDescription(), b.getDescription());
    		if (ret) {
    			for (int i = 0; i < a.getItemCount(); i++) {
    				if (!equals(a.getItemAt(i), b.getItemAt(i))) {
    					ret = false;
    					break;
    				}
    			}
    		}
    	}
    	return ret;
    }
    
    public static boolean equals(ClipDescription a, ClipDescription b) {
    	boolean ret = a == b;
    	if (!ret && a != null && b != null && a.getMimeTypeCount() == b.getMimeTypeCount()) {
    		ret = equals(a.getLabel(), b.getLabel());
    		if (ret) {
    			for (int i = 0; i < a.getMimeTypeCount(); i++) {
    				if (!equals(a.getMimeType(i), b.getMimeType(i))) {
    					ret = false;
    					break;
    				}
    			}
    		}
    	}
    	
    	return ret;
    }
    
    public static boolean equals(ClipData.Item a, ClipData.Item b) {
    	boolean ret = a == b;
    	if (a != null && b != null) {
    		ret = 
    				equals(a.getText(), b.getText()) && 
    				equals(a.getHtmlText(), b.getHtmlText()) &&
    				equals(a.getIntent(), b.getIntent()) &&
    				equals(a.getUri(), b.getUri());
    	}
    	return ret;
    }
    
    public static boolean equals(CharSequence a, CharSequence b) {
    	boolean ret = a == b;
    	if (!ret && a != null && b != null) {
    		ret = a.toString().equals(b.toString());
    	}
    	return ret;
    }
    
    public static boolean equals(Uri a, Uri b) {
    	boolean ret = a == b;
    	if (!ret && a != null && b != null) {
    		ret = a.equals(b);
    	}
    	
    	return ret;
    }
}
