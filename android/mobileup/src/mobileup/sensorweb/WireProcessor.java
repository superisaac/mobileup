/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/

package mobileup.sensorweb;
import mobileup.web.*;
import mobileup.util.*;
import java.util.*;
import mobileup.sensorweb.rpc.*;
import mobileup.json.*;

public class WireProcessor extends WebSocketProcessor
{
    private Set<String> subscribed = new HashSet<String>();
    public void sendQueue(String qname, String[] args) {
        Response response = protocol.response;
        response.send(dataFrameHead, 0, 1);
        response.send("ntf ");
	response.send(qname);
        for(String arg: args) {
            response.send(" ");
            response.send(arg);
        }
        response.send(dataFrameTail, 0, 1);
    }

    public void sendMessage(String command, String[] args, String payload) {
        Response response = protocol.response;
        response.send(dataFrameHead, 0, 1);
        response.send(command);
        for(String arg: args) {
            response.send(" ");
            response.send(arg);
        }
        if(payload != null) {
            response.send("\n");
            response.send(payload);
        }
        response.send(dataFrameTail, 0, 1);
    }

    public void sendMessage(String command, String[] args) {
        sendMessage(command, args, null);
    }

    @Override
    public void processConnectionLost() {
	Log.w("WireProcessor", "process connection lost");
	QueueManager qm = QueueManager.getInstance();
	for(String device: subscribed) {
	    Queue q = qm.get(device, false);
	    if(q != null) {
		q.unsub(this);
	    }
	}
	subscribed = new HashSet<String>();
    }

    protected void onMessage(String packet) {
        Message msg = new Message(packet);
	QueueManager qm = QueueManager.getInstance();
	if(msg.command.equals("sub")) {
            String device = msg.args[0];
	    Queue q = qm.get(device);
	    q.sub(this);
	    subscribed.add(device);
        } else if(msg.command.equals("unsub")) {
            String device = msg.args[0];

	    Queue q = qm.get(device, false);
	    if(q != null) {
		q.unsub(this);
		subscribed.remove(device);
	    } else {
		Log.e("Wire Processor", "Illegal device " + device);
	    }
	} else if(msg.command.equals("put")) {
            String device = msg.args[0];
	    Queue q = qm.get(device);
	    if(q != null) {
		q.put(msg.args);
	    }
	} else if(msg.command.equals("call")) {
	    assert(msg.args.length >= 2);
	    String callID = msg.args[0];
	    String objectName = msg.args[1];
	    String methodName = "";
	    if(msg.args.length >= 3) {
		methodName = msg.args[2];
	    }
	    Handler handler = HandlerFactory.instance.getHandler(objectName);

	    if(handler != null) {
		handler.processor = this;
		handler.callID = callID;
		JSONArray args = null;
		if(msg.payload != null && msg.payload.length() > 0) {
		    try {
			args = new JSONArray(msg.payload);
		    } catch(JSONException e) {
			handler.sendError("JSONerror");
			return;
		    }
		}
		try {
		    Object res = handler.called(methodName, args);
		    if(res != null) {
			try {
			    handler.sendJSONReturn(handler.getValueWriter(res));
			} catch(JSONException e) {
			    e.printStackTrace();
			    handler.sendError("JSON Error");
			}
		    }
		} catch(HandlerError e) {
		    handler.sendError(e.getMessage());
		}
	    } else {
		System.err.println("No such method " + methodName);
	    }
	}
    }
}

