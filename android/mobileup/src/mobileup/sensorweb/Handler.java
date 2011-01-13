/** %LICENSE% **/

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