/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/

package mobileup.sensorweb;
import mobileup.json.*;

public abstract class Handler
{

    public WireProcessor processor;
    public String callID;

    public void sendJSONReturn(JSONWriter w){
	processor.sendMessage("call.return",
			      new String[]{callID}, 
			      w.toString());
    }

    public JSONWriter getValueWriter(Object v) throws JSONException {
	return new JSONStringer().object().key("v").value(v).endObject();
    }

    protected void sendError(String msg)   {
	try {
	    processor.sendMessage("call.error",
				  new String[]{callID},
				  new JSONStringer()
				  .object()
				  .key("message").value(msg)
				  .endObject().toString());
	}catch(JSONException e) {
	    processor.sendMessage("call.error",
				  new String[]{callID},
				  "{message: "+ JSONObject.quote(e.toString()) + "}");
	}
    }

    public abstract Object called(String methodName, JSONArray args);
}