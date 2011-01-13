/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.web;
import mobileup.network.*;
import mobileup.network.protocol.LineReceiver;
import mobileup.util.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.*;

public class HttpProtocol extends LineReceiver 
{
    Hashtable<String, String> headers = new Hashtable<String, String>();
    public String command;
    public String object;
    public String version;
    public Response response;
    private int state = 0;
    private Processor processor;

    public void onRawData(byte[] data, int offset,  int numRead) {
	if(processor != null) {
	    processor.processData(this, data, offset, numRead);
	}
    }

    @Override
    public void onConnectionLost() {
	Log.d("conn", "lost");
	if(processor != null) {
	    processor.processConnectionLost();
	}
    }
    public URL getURL() throws MalformedURLException{
        return new URL("http://dummy.url" + object);
    }

    public void onLineReceived(String line) {
	line = line.trim();
	if(state == 0) {  // Status line
	    response = new Response(this);
	    String[] args = line.split(" ");
	    command = args[0];
	    object = args[1];
	    version = args[2];  // HTTP/1.1
	    state = 1;
	} else if(state == 1) { // Headers
	    String[] items = line.split(":", 2);
	    if(items.length < 2) {
		state = 0;
		processRequest();
		setRawMode(true);
	    } else {
		headers.put(items[0].trim().toLowerCase(), items[1].trim());
	    }
	}
    }

    public void basicAuth(String base64Auth) {
        String authLine = "Basic " + base64Auth;
        if(!authLine.equals(headers.get("authorization"))) {
            throw new Http401();
        }
    }

    public void processRequest() {
        processor = route();
	try {
	    if(processor != null) {
                processor.process(this);
	    } else {
		throw new Http404();
	    }
        } catch(Http401 e) {
            response.status = "401 Authorization Required";
            response.headers.put("WWW-Authenticate", "Basic realm=\"SensorWeb\"");
            response.sendAll("Authorization Required");                
	} catch(Http404 e){
	    response.status = "404 Not Found";
	    response.sendAll("Not Found");
	} catch(Http500 e) {
	    response.status = "500 Server Error";
	    response.sendAll(e.getMessage());
	}
    }

    public Processor route() {
        return null;
    }
}
