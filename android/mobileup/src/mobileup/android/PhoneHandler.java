/** %LICENSE% **/
package mobileup.android;

import android.provider.ContactsContract.Data;
import android.database.Cursor;
import android.content.ContentResolver;
import android.content.Context;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.telephony.TelephonyManager;

import mobileup.sensorweb.Handler;
import mobileup.sensorweb.HandlerError;
import mobileup.json.*;
import android.telephony.SmsManager;

public class PhoneHandler extends Handler
{
    private Context context;
    public PhoneHandler(Context context) {
	this.context = context;
    }

    public Object called(String methodName, JSONArray args) {
	if(methodName.equals("listcontact")){
	    return listContact();
	} else if(methodName.equals("sendsms")) {
	    return sendSMS(args);
	} else if(methodName.equals("getinfo")) {
	    return getInfo(args);
	} else if(methodName.equals("listsms")) {
	    return listSMS(args);
	}
	return null;
    }

    private Object getInfo(JSONArray args) {
	JSONObject obj = new JSONObject();
	TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	String phoneNo = tm.getLine1Number();
	if(phoneNo == null) {
	    phoneNo = "";
	}
	try {
	    obj.put("phonNo", phoneNo);
	} catch(JSONException e) {
	    Log.e("PhoneHandler", "send phone failed");
	}
	return obj;
    }

    private Object sendSMS(JSONArray args) {
	if(args == null || args.length() < 2) {
	    throw new HandlerError("Wrong number of arguments");
	}
	String phoneNo;
	String text;
	try {
	    phoneNo = args.getString(0);
	    text = args.getString(1);
	} catch(JSONException e) {
	    throw new HandlerError("Arguments Error");
	}
	if(phoneNo == null || phoneNo.length() <= 0) {
	    throw new HandlerError("Illegal dest phone number");
	}
	if(text == null || text.length() <= 0) {
	    throw new HandlerError("Illegal text");
	}
	
	SmsManager sm = SmsManager.getDefault();
	Intent i = new Intent("mobileup.sensorweb.IGNORE_ME");
	PendingIntent dummyEvent = PendingIntent.getBroadcast(
							      context, 0, i, 0);

	sm.sendTextMessage(phoneNo, null, text,
			   dummyEvent, dummyEvent);
	return "OK";
    }
    private Object listSMS(JSONArray args) {
	ContentResolver cr = context.getContentResolver();
	int offset = 0; //args.getInt(0);
	int limit = 20; //args.getInt(1);
	try {
	    if(args != null) {
		if(args.length() == 1) {
		    limit = args.getInt(0);
		} else if(args.length() >= 2) {
		    offset = args.getInt(0);
		    limit = args.getInt(1);
		}
	    }
	} catch(JSONException e) {
	    throw new HandlerError("Illegal arguments of listSMS");
	}

	String[] projection = new String[] {
	    "_id", "address", "date", "read", "body",
	};
	Uri smsurl = Uri.parse("content://sms/inbox");
	// Make the query. 
	Cursor cur = cr.query(smsurl,
			      projection, // Which columns to return 
			      null,       // Which rows to return (all rows)
			      null,       // Selection arguments (none)
			      // Put the results in ascending order by name
			      "date desc");

	int idColumn = cur.getColumnIndex("_id"); 
        int addrColumn = cur.getColumnIndex("address");
	int dateColumn = cur.getColumnIndex("date");
	int readColumn = cur.getColumnIndex("read");
	int bodyColumn = cur.getColumnIndex("body");
	JSONArray smsList = new JSONArray();
	int cnt = 0;
	if(cur.moveToPosition(offset)) {
	    do {
		String addr = cur.getString(addrColumn);
		long date = cur.getLong(dateColumn);
		int read = cur.getInt(readColumn);
		String body = cur.getString(bodyColumn);
		JSONObject entry = new JSONObject();
		try {
		    entry.put("addr", addr);
		} catch (JSONException e) {
		    Log.e("Phone", "json error on addr");
		}
		try {
		    entry.put("date", date);
		} catch (JSONException e) {
		    Log.e("Phone", "json error on date");
		}
		try {
		    entry.put("read", read);
		} catch (JSONException e) {
		    Log.e("Phone", "json error on read");
		}
		try {
		    entry.put("body", body);
		} catch (JSONException e) {
		    Log.e("Phone", "json error on body");
		}
		smsList.put(entry);
	    } while(cur.moveToNext() && (cnt++) < limit);
	} 
	return smsList;
    }
    private Object listContact() {
	ContentResolver cr = context.getContentResolver();

	String[] projection = new String[] {
	    Data._ID,
	    "label",
	    "number"
	};
	// Make the query. 
	Cursor cur = cr.query(Data.CONTENT_URI,
			      projection, // Which columns to return 
			      null,       // Which rows to return (all rows)
			      null,       // Selection arguments (none)
			      // Put the results in ascending order by name
			      "label ASC");

	int nameColumn = cur.getColumnIndex("label"); 
        int phoneColumn = cur.getColumnIndex("number");
	JSONArray contactList = new JSONArray();
	if(cur.moveToFirst()) {
	    do {
		String name = cur.getString(nameColumn);
		String phone = cur.getString(phoneColumn);
		if(phone != null) {
		    JSONObject entry = new JSONObject();
		    try {
			entry.put("name", name);
		    } catch (JSONException e) {
			Log.e("Phone", "json error on name" + name + " " + phone);
		    }
		    try {
			entry.put("phone", phone);
		    } catch (JSONException e) {
			Log.e("Phone", "json error on phone " + name + " " + phone);
		    }
		    contactList.put(entry);
		}
	    } while(cur.moveToNext());
	} 
	return contactList;
    }
}