/** %LICENSE% **/
package mobileup.sensorweb.rpc;
import mobileup.sensorweb.Handler;
import mobileup.sensorweb.HandlerError;
import java.io.*;
import mobileup.json.*;

public class SystemHandler extends Handler
{
    public static String docRoot = "";

    public Object called(String methodName, JSONArray args) {
	if(methodName.equals("listdir")) {
	    return listDir(args);
	}
	return null;
    }

    private Object listDir(JSONArray args) {
	String dirname = docRoot;
	try {
	    dirname = new File(docRoot).getCanonicalPath();
	}catch(IOException e) {
	    throw new HandlerError("IO Exception: " + e.toString());
	}

	if(args != null && args.length() >= 1) {
	    try {
		dirname +=  File.separator + args.getString(0);
	    } catch(JSONException e) {
		throw new HandlerError("JSONException: " + e.toString());
	    }
	}

	JSONArray fileList = new JSONArray();
	File dir = new File(dirname);
	if(dir.isDirectory()) {
	    String[] fl = dir.list();
	    if(fl == null) {
		fl = new String[]{};
	    }
	    for(int i=0; i< fl.length; i++) {
		File f = new File(dirname + File.separator + fl[i]);
		JSONObject entry = new JSONObject();
		try {
		    entry.put("fileName", fl[i]);
		}catch(JSONException e) {
		}

		try {
		    entry.put("isDir", f.isDirectory());
		}catch(JSONException e) {
		}

		try{
		    entry.put("lastModified", f.lastModified());
		} catch(JSONException e) {
		}

		try {
		    entry.put("size", f.length());
		} catch(JSONException e) {
		}
		fileList.put(entry);
	    }
	} else {
	    throw new HandlerError(dirname + " Is not dir!");
	}
	return fileList;
    }
}