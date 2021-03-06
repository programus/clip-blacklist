package org.programus.android.clipblacklist.util;

import java.net.URISyntaxException;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;


/**
 * A helper class to process {@link ClipData} related function.
 * @author programus
 *
 */
public class ClipDataHelper {
	private final static char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', };
	
	private final static int NUM_LEN = 8;
	
	private final static int FLAG_NULL = -1;
	
	private final static int FLAG_INT = -2;
	
	private final static int FLAG_SPANNED = -3;
	
	/** Empty String array */
    public final static String[] EMPTY_STR_ARRAY = new String[0];

	private static ClipData emptyClip;
	
	/** Flag for ClipData Item Type: Text */
	public final static int ITEM_TYPE_TEXT = 1;
	/** Flag for ClipData Item Type: HTML */
	public final static int ITEM_TYPE_HTML = 1 << 1;
	/** Flag for ClipData Item Type: URI */
	public final static int ITEM_TYPE_URI = 1 << 2;
	/** Flag for ClipData Item Type: Intent */
	public final static int ITEM_TYPE_INTENT = 1 << 3;
	
	/** Readable String for ClipData Item Type: Text */
	public final static String TYPE_TEXT = "TXT";
	/** Readable String for ClipData Item Type: HTML */
	public final static String TYPE_HTML = "HTML";
	/** Readable String for ClipData Item Type: URI */
	public final static String TYPE_URI = "URI";
	/** Readable String for ClipData Item Type: Intent */
	public final static String TYPE_INTENT = "INTENT";
	
	/**
	 * Return the item types flag of the specified item. 
	 * @param item
	 * @return the item types flag
	 * @see #ITEM_TYPE_TEXT
	 * @see #ITEM_TYPE_HTML
	 * @see #ITEM_TYPE_URI
	 * @see #ITEM_TYPE_INTENT
	 */
	public static int getItemTypes(ClipData.Item item) {
		int types = 0;
		if (item.getText() != null) {
			types |= ITEM_TYPE_TEXT;
		}
		if (item.getHtmlText() != null) {
			types |= ITEM_TYPE_HTML;
		}
		if (item.getUri() != null) {
			types |= ITEM_TYPE_URI;
		}
		if (item.getIntent() != null) {
			types |= ITEM_TYPE_INTENT;
		}
		
		return types;
	}
	
	/**
	 * Return the readable string for specified item types flag.
	 * @param types
	 * @return the readable string
	 */
	public static String getTypeString(int types) {
		String[] typeStrings = {TYPE_TEXT, TYPE_HTML, TYPE_URI, TYPE_INTENT};
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < typeStrings.length; i++) {
			if (((types >>> i) & 0x01) == 1) {
				sb.append(typeStrings[i]).append(' ');
			}
		}
		return sb.toString();
	}
	
	/**
	 * Return the hex string for specified integer. 
	 * @param i
	 * @param minWidth
	 * @return the hex string like 0f3e
	 */
	public static String hexStringFromInt(int i, int minWidth) {
		final int len = 8;
		char[] buf = new char[len];
		int cursor = len;
		
		do {
			buf[--cursor] = DIGITS[i & 0x0f];
		} while ((i >>>= 4) != 0 || (len - cursor < minWidth));
		
		return new String(buf, cursor, len - cursor);
	}
	
	/**
	 * Convert the hex string to the integer value.
	 * @param str
	 * @return the integer value for the specified hex string
	 */
	public static int intFromHexString(CharSequence str) {
		int n = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int v = c <= '9' ? c - '0' : c - 'a' + 10;
			n <<= 4;
			n |= (v & 0x0f);
		}
		return n;
	}
	
	/**
	 * toString method for values include null
	 * @param cs
	 * @return the string
	 */
	public static String toString(CharSequence cs) {
		return cs == null ? null : cs.toString();
	}
	
	private static CharSequence cut(StringBuilder sb, int start, int end) {
		CharSequence cs = sb.subSequence(start, end);
		sb.delete(start, end);
		return cs;
	}
	
	private static void append(StringBuilder sb, CharSequence... data) {
		for (CharSequence d : data) {
			if (d != null) {
			    if (d instanceof Spanned) {
			        String s = Html.toHtml((Spanned) d);
			        sb.append(hexStringFromInt(FLAG_SPANNED, NUM_LEN));
			        append(sb, s);
			    } else {
                    int len = d.length();
                    sb.append(hexStringFromInt(len, NUM_LEN)).append(d);
			    }
			} else {
				sb.append(hexStringFromInt(FLAG_NULL, NUM_LEN));
			}
		}
	}
	
	private static void appendInt(StringBuilder sb, int i) {
		sb.append(hexStringFromInt(FLAG_INT, NUM_LEN)).append(hexStringFromInt(i, NUM_LEN));
	}
	
	private static int extractInt(StringBuilder data) {
		CharSequence flag = cut(data, 0, NUM_LEN);
		int n = intFromHexString(flag);
		if (n == FLAG_INT) {
			CharSequence cs = cut(data, 0, NUM_LEN);
			return intFromHexString(cs);
		} else {
			throw new IllegalArgumentException(String.format("%s is not a valid number string.", data.toString()));
		}
	}
	
	private static CharSequence extract(StringBuilder data) {
	    return extract(data, false);
	}
	
	private static CharSequence extract(StringBuilder data, boolean isSpanned) {
		CharSequence len = cut(data, 0, NUM_LEN);
		int l = intFromHexString(len);
		CharSequence ret = null;
		switch(l) {
		case FLAG_NULL:
			ret = null;
			break;
		case FLAG_SPANNED:
		    ret = extract(data, true);
		    break;
		default:
			ret = cut(data, 0, l);
			if (isSpanned) {
			    ret = Html.fromHtml(ret.toString());
			}
		}
		return ret;
	}
	
	/**
	 * Return an instance of {@link ClipData} with empty data.
	 * @return empty ClipData instance
	 */
	public static ClipData getEmptyClipData() {
		if (emptyClip == null) {
	    	ClipData.Item item = new ClipData.Item((String)null);
	    	emptyClip = new ClipData(null, EMPTY_STR_ARRAY, item);
		}
		return emptyClip;
	}
	
	/**
	 * Convert {@link ClipData} to String for storing.
	 * @param clip
	 * @return String for specifed clipdata
	 */
	public static CharSequence stringFromClipData(ClipData clip) {
		StringBuilder sb = new StringBuilder();
		if (clip != null) {
			append(sb, "clip");
			appendClipDescription(sb, clip.getDescription());
			int n = clip.getItemCount();
			appendInt(sb, n);
			for (int i = 0; i < n; i++) {
				appendItem(sb, clip.getItemAt(i));
			}
		} else {
			append(sb, (String) null);
		}
		return sb;
	}
	
	/**
	 * Restore {@link ClipData} from String
	 * @param s
	 * @return ClipData instance
	 */
	public static ClipData clipDataFromString(CharSequence s) {
		ClipData cd = null;
		if (s != null) {
            StringBuilder sb = new StringBuilder(s);
            CharSequence prefix = extract(sb);
            if (prefix != null) {
                ClipDescription desc = clipDescriptionFromString(sb);
                int n = extractInt(sb);
                ClipData.Item[] items = new ClipData.Item[n];
                for (int i = 0; i < n; i++) {
                    items[i] = itemFromString(sb);
                }
                if (n > 0) {
                    cd = new ClipData(desc, items[0]);
                }
                for (int i = 1; i < n; i++) {
                    cd.addItem(items[i]);
                }
            }
		}
		return cd;
	}
	
	private static void appendClipDescription(StringBuilder sb, ClipDescription desc) {
		if (desc != null) {
			append(sb, 
					"desc", 
					desc.getLabel());
			int n = desc.getMimeTypeCount();
			appendInt(sb, n);
			for (int i = 0; i < n; i++) {
				append(sb, desc.getMimeType(i));
			}
		} else {
			append(sb, (String) null);
		}
	}
	
	private static ClipDescription clipDescriptionFromString(StringBuilder s) {
		CharSequence prefix = extract(s);
		ClipDescription desc = null;
		if (prefix != null) {
			String label = toString(extract(s));
			int n = extractInt(s);
			String[] mimeTypes = new String[n];
			for (int i = 0; i < n; i++) {
				mimeTypes[i] = toString(extract(s));
			}
			desc = new ClipDescription(label, mimeTypes);
		}
		return desc;
	}
	
	private static void appendItem(StringBuilder sb, ClipData.Item item) {
		if (item != null) {
			append(sb, 
					"item",
					item.getText(), 
					item.getHtmlText());
			appendIntent(sb, item.getIntent());
			appendUri(sb, item.getUri());
		} else {
			append(sb, (String) null);
		}
	}
	
	private static ClipData.Item itemFromString(StringBuilder s) {
		CharSequence prefix = extract(s);
		ClipData.Item item = null;
		if (prefix != null) {
			item = new ClipData.Item((extract(s)), toString(extract(s)), intentFromString(s), uriFromString(s));
		}
		return item;
	}
	
	private static void appendUri(StringBuilder sb, Uri uri) {
	    append(sb, uri == null ? null : uri.toString());
	}
	
	private static Uri uriFromString(StringBuilder s) {
	    String str = toString(extract(s));
	    Uri uri = str == null ? null : Uri.parse(str);
		return uri;
	}
	
	private static void appendIntent(StringBuilder sb, Intent intent) {
	    if (intent != null) {
            String uri = intent.toUri(Intent.URI_INTENT_SCHEME);
            String action = intent.getAction();
            append(sb, "intent", action, uri);
	    } else {
	        append(sb, (String) null);
	    }
	}
	
	private static Intent intentFromString(StringBuilder s) {
		CharSequence prefix = extract(s);
		Intent intent = null;
		if (prefix != null) {
			try {
			    String action = toString(extract(s));
			    String uri = toString(extract(s));
				intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME);
				intent.setAction(action);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return intent;
	}
}
