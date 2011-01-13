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

import java.util.*;
import java.io.IOException;
import mobileup.util.Log;

public class Response
{
    public static final int ST_INIT = 0;
    public static final int ST_HEADER = 1;
    public static final int ST_CLOSED = 2;

    public String version = "HTTP/1.1";
    public String status = "200 OK";
    public String mimeType = "text/html";
    public Hashtable<String, String> headers = new Hashtable<String, String>();
    private HttpProtocol protocol;
    private boolean payload_sent = false;
    public int state = ST_INIT; // 0 init; 1 sent header; 2 closed

    public Response(HttpProtocol protocol) {
	this.protocol = protocol;
    }

    public void send(byte[] data, int offset, int length) {
        begin();
        if(state != ST_CLOSED) {
            protocol.write(data, offset, length);
        }
    }

    public void send(byte[] data) {
        send(data, 0, data.length);
    }

    public void send(String str) {
	send(str.getBytes());
    }

    public void sendAll(byte[] data) {
        begin(data.length);
        send(data);
        finish();
    }

    public void sendAll(String data) {
        sendAll(data.getBytes());
    }

    public void begin(){
        begin(-1);
    }

    public void begin(long dataLength) {
        if(state == ST_INIT) {
            if(dataLength >= 0) {
                headers.put("Content-Length", "" + dataLength);
            } 
	    if(mimeType != null) {
		headers.put("Content-Type", mimeType);
	    }
            protocol.write(String.format("%s %s\r\n", version, status));
            
            for(Map.Entry<String, String> entry: headers.entrySet()) {
                protocol.write(String.format("%s: %s\r\n",
                                             entry.getKey(), 
					 entry.getValue()));
            }
            protocol.write("\r\n");
            state = ST_HEADER;
        }
    }

    public void beginWebSocket(byte[] challenge) {
	if(state == ST_INIT) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("HTTP/1.1 101 Web Socket Protocol Handshake\r\n")
                .append("Upgrade: WebSocket\r\n")
                .append("Connection: Upgrade\r\n")
                .append("Server: SensorWebServer/1.0\r\n")
                .append("Sec-WebSocket-Origin: " + protocol.headers.get("origin") + "\r\n")
                .append("Sec-WebSocket-Location: " +
                        "ws://" + protocol.headers.get("host") + protocol.object
                        + "\r\n")
                .append("\r\n");
            protocol.write(buffer.toString());
            protocol.write(challenge);
	    state = ST_HEADER;
	}
    }

    public void finish() {
        state = ST_CLOSED;
        Log.d("Web response", "Finished");
        protocol.close();
    }
}
