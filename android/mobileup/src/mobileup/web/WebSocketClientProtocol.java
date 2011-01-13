/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.web;

import java.net.URI;
import mobileup.web.HttpClientProtocol;
import mobileup.util.Log;

public class WebSocketClientProtocol extends HttpClientProtocol
{
    public String sec_key1 = "12998 5 Y3 1  .P00";
    public String sec_key2 = "4 @1  46546xW%0l 1 5";
    public byte[] challenge = new byte[]{0x01, 0x02, 0x13, 0x04, 0x05, 0x06, 0x07, 0x28};
    private int dataState = -1;

    private String remaining = "";
    private byte[] clgRspBytes = new byte[16];
    private int  clgRspBytesIndex = 0;

    public final static byte[] dataFrameHead = new byte[]{0};
    public final static byte[] dataFrameTail = new byte[]{-1};

    public WebSocketClientProtocol(URI url) {
	super("GET", url);
	requestHeaders.put("Origin", "http://" + url.getHost());
	requestHeaders.put("Upgrade", "WebSocket");
	requestHeaders.put("Connection", "Upgrade");
	requestHeaders.put("Sec-WebSocket-Key1", sec_key1);
	requestHeaders.put("Sec-WebSocket-Key2", sec_key2);
    }

    @Override
    public void onConnectionMade() {
	super.onConnectionMade();
	write(challenge);
    }

    @Override
    public void onRawData(byte[] data, int offset, int numRead) {
        int startPos = 0;
        for(int i=offset; i < offset + numRead; i++) {
            byte c = data[i];
	    switch(dataState) {
	    case -1:
                clgRspBytes[clgRspBytesIndex++] = c;
                if(clgRspBytesIndex >= 16) {
                    dataState = 0;
                    clgRspBytesIndex = 0;
		    Log.d("WebSocketClientProtocol", "Get Challenge response ");
		    onopen();
                }
		break;
	    case 0:
                if(c != 0) {
                    dataState = 5; // Error
                    return;
                } else {
                    dataState = 1;
                    startPos = i + 1;
                }
		break;
	    case 1:
                if(c == -1||c == 0xff) {
                    String msg = new String(data, startPos, i - startPos);
                    if(remaining.length() > 0) {
                        onmessage(remaining + msg);
                    } else {
                        onmessage(msg);
                    }
                    remaining = "";
                    dataState = 0;
                }
		break;
	    default:
		Log.e("WebSocketClientProtocol", "Bad data");
		onerror();
		onclose();
		close();
            }
        }
        if(dataState == 1) {
            remaining += new String(data, startPos, numRead - startPos);
        }
    }

    @Override
    public void onConnectionLost() {
	onclose();
    }

    public void onerror() {
	
    }

    public void onmessage(String message) {
	//
    }

    public void onopen() {
	//
    }

    public void onclose() {
	//
    }

    public void send(final byte[] message) {
        write(dataFrameHead, 0, 1);
        write(message);
        write(dataFrameTail, 0, 1);
    }

    public void send(String message) {
	try {
	    send(message.getBytes("UTF-8"));
	}catch(java.io.UnsupportedEncodingException e) {
	    e.printStackTrace();
	    send(message.getBytes());
	}
    }

}