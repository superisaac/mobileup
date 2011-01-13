/** %LICENSE% **/

package mobileup.util;
import java.util.*;

public class MimeManager
{
    private static Map<String, String> mimeMap = new HashMap<String, String>();
    private static void prepare() {
	mimeMap.put("html", "text/html");
	mimeMap.put("xml", "text/xml");
	mimeMap.put("js", "text/javascript");
	mimeMap.put("css", "text/css");
	mimeMap.put("txt", "text/plain");
	mimeMap.put("jpeg", "image/jpeg");
	mimeMap.put("jpg", "image/jpeg");
	mimeMap.put("png", "image/png");
	mimeMap.put("gif", "image/gif");
        mimeMap.put("ico", "image/ico");
	mimeMap.put("mp3", "audio/mp3");
    }

    public static String getMimeType(String filename) {
	if(mimeMap.size() == 0) {
	    prepare();
	}

	int idx = filename.lastIndexOf(".");
	if(idx >= 0) {
	    String ext = filename.substring(idx + 1).toLowerCase();
	    String mt = mimeMap.get(ext);
	    if(mt != null) {
		return mt;
	    }
	}
	return "application/octet-stream";
    }
}
