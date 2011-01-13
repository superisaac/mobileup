/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.android;

import java.util.Vector;
import java.net.URISyntaxException;
import java.net.URI;

import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.JsResult;
import android.webkit.WebView;
import android.webkit.ConsoleMessage;
import android.util.Log;
import android.util.AttributeSet;
import android.content.Context;

import mobileup.network.Reactor;
import mobileup.json.*;
import mobileup.web.WebSocketClientProtocol;

class ViewWebSocket extends WebSocketClientProtocol{
    WebView mView;

    public static Vector<ViewWebSocket> connections = new Vector<ViewWebSocket>();
    public static void closeConnections() {
	for(ViewWebSocket sock: connections) {
	    sock.close();
	}
	connections = new Vector<ViewWebSocket>();
    }

    public ViewWebSocket(WebView v, URI url) throws URISyntaxException {
	super(url);
	mView = v;
	connections.add(this);
    }
	
    protected static class JSEvent {
	static String buildJSON(String type, String target, String data) {
	    //Log.i("JSEvent", "{\"type\":\"" + type + "\",\"target\":\"" + target + "\",\"data\":'"+ data +"'}");
	    //return "{\"type\":\"" + type + "\",\"target\":\"" + target + "\",\"data\":'"+ data +"'}";
	    try {
		return new JSONStringer().object().key("type").value(type)
		    .key("target").value(target).key("data").value(data).endObject()
		    .toString();
	    } catch(JSONException e) {
		e.printStackTrace();
		return "{}";
	    }
	}
		
	static String buildJSON(String type, String target) {
	    //return "{\"type\":\"" + type + "\",\"target\":\"" + target + "\",\"data\":\"\"}";
	    try {
		return new JSONStringer().object().key("type").value(type)
		    .key("target").value(target).endObject()
		    .toString();
	    } catch(JSONException e) {
		e.printStackTrace();
		return "{}";
	    }

	}		
    }
	
    @Override
    public void onmessage(String data) {
	mView.loadUrl("javascript:WebSocket.triggerEvent(" + JSEvent.buildJSON("message", this.toString(), data) + ")");
    }

    @Override
    public void onopen() {
	Log.d("ViewWebSocket", "on open");
	mView.loadUrl("javascript:WebSocket.triggerEvent(" + JSEvent.buildJSON("open", this.toString()) + ")");
    }
	
    @Override	
    public void onerror() {
	Log.d("ViewWebSocket", "on error");
	mView.loadUrl("javascript:WebSocket.triggerEvent(" + JSEvent.buildJSON("error", this.toString()) + ")");
    }

    @Override
    public void onclose() {
	Log.d("ViewWebSocket", "on error");
	mView.loadUrl("javascript:WebSocket.triggerEvent(" + JSEvent.buildJSON("close", this.toString()) + ")");		
    }
	
    public ViewWebSocket connect() {
	Log.d("Main", "web socket connect " + toString());
	Reactor reactor = Reactor.getInstance();
	try {
	    connect(reactor);
	}catch(java.io.IOException e) {
	    e.printStackTrace();
	    onerror();
	}
	return this;
    }

    public String getIdentifier() {
	return this.toString();
    }

    public void finalize() {
	close();
    }
}

class ViewWebSocketFactory
{
    WebView mView;
    public ViewWebSocketFactory(WebView view)
    {
	mView = view;
    }
	
    public ViewWebSocket getNew(String url) throws URISyntaxException {
	Log.d("ViewWebSocketFactory", "getNew " + url);
	return new ViewWebSocket(mView, new URI(url)).connect();
    }  
}


public class CustomWebView extends WebView
{
    public CustomWebView(Context context){
	super(context);
    }
    public CustomWebView(Context context, AttributeSet attrSet) {
	super(context, attrSet);
    }
    public CustomWebView(Context context, AttributeSet attrSet, int defStyle) {
	super(context, attrSet, defStyle);
    }

    public void resetView() {
	// Checkbox
	getSettings().setJavaScriptEnabled(true);

	setWebViewClient(new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    if(!url.startsWith("javascript:") && !url.startsWith("#")) {
			//Log.d("Html5Main", "close all connections on " + url);
			ViewWebSocket.closeConnections();
		    }
		    view.loadUrl(url);
		    return true;
		}
		@Override
		public void onReceivedError(WebView view, int errorCode, String desc, String failingURL) {
		    Log.e("WebView", "code: " + errorCode + ", desc=" + desc);
		}		    
	    });
	setWebChromeClient(new WebChromeClient() {
		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
		    Log.d("WebChromeClient", "[Alert] " + message);
		    return true;
		}

		@Override
		public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
		    ViewWebSocket.closeConnections();
		    return true;
		}

		@Override
		public boolean onConsoleMessage(ConsoleMessage msg) {
		    Log.d("WebClientConsole", "[" + String.valueOf(msg.messageLevel()) + "] " + msg.lineNumber() + ": " + msg.message());
		    return true;
		}		    
	    });

	ViewWebSocketFactory wsFactory = new ViewWebSocketFactory(this);
	addJavascriptInterface(wsFactory, "WebSocketFactory");
	clearCache(true);
    }
}