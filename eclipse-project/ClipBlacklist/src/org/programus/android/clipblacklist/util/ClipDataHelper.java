package org.programus.android.clipblacklist.util;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;


public class ClipDataHelper {
	private final static char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', };
	
	private final static int NUM_LEN = 8;
	
	private final static int FLAG_NULL = -1;
	
	private final static int FLAG_INT = -2;
	
	public static String hexStringFromInt(int i, int minWidth) {
		final int len = 8;
		char[] buf = new char[len];
		int cursor = len;
		
		do {
			buf[--cursor] = DIGITS[i & 0x0f];
		} while ((i >>>= 4) != 0 || (len - cursor < minWidth));
		
		return new String(buf, cursor, len - cursor);
	}
	
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
	
	private static String toString(CharSequence cs) {
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
				int len = d.length();
				sb.append(hexStringFromInt(len, NUM_LEN)).append(d);
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
		CharSequence len = cut(data, 0, NUM_LEN);
		int l = intFromHexString(len);
		CharSequence ret = null;
		switch(l) {
		case FLAG_NULL:
			ret = null;
			break;
		default:
			ret = cut(data, 0, l);
		}
		return ret;
	}
	
	private static List<CharSequence> extract(StringBuilder data, int limit) {
		List<CharSequence> ret = new ArrayList<CharSequence>();
		if (limit < 0) {
			limit = Integer.MAX_VALUE;
		}
		CharSequence tmp = null;
		for (int i = 0; i < limit && data.length() > 0; i++) {
			tmp = extract(data);
			ret.add(tmp);
		}
		
		return ret;
	}
	
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
	
	public static ClipData clipDataFromString(CharSequence s) {
		StringBuilder sb = new StringBuilder(s);
		CharSequence prefix = extract(sb);
		ClipData cd = null;
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
			item = new ClipData.Item(toString(extract(s)), toString(extract(s)), intentFromString(s), uriFromString(s));
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
