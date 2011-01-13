/** %LICENSE% **/
package mobileup.web;

import java.net.URI;
import java.net.InetAddress;

import java.io.IOException;
import java.util.*;

import mobileup.network.protocol.LineReceiver;
import mobileup.util.Log;
import mobileup.network.Reactor;

public class HttpClientProtocol extends LineReceiver
{

    public Hashtable<String, String> headers = new Hashtable<String, String>();
    public Hashtable<String, String> requestHeaders = new Hashtable<String, String>();


    public int code;
    public String codeDescription;
    private int responseState = 0;
    public String method = "GET";  
    protected URI url;

    public HttpClientProtocol(String method, URI url) {
	this.method = method;
	this.url = url;

	String host = url.getHost();
	int port = url.getPort();
	if(port != 80) {
	    host += ":" + port;
	}
	requestHeaders.put("Host", host);
    }

    public HttpClientProtocol(URI url) {
	this("GET", url);
    }
    
    public void connect(Reactor reactor) throws IOException{
	reactor.connect(InetAddress.getByName(url.getHost()),
			url.getPort(),
			this);
    }

    @Override
    public void onConnectionMade() {
	// TODO: handle POST method
	String path = url.getPath();
	if(path.equals("")) {
	    path = "/";
	}
	String query = url.getQuery();
	if(query != null) {
	    path = path + "?" + query;
	}
	String line = method + " " + path + " HTTP/1.1";

	writeln(line);
	for(Map.Entry<String, String> entry: requestHeaders.entrySet()) {
	    line = entry.getKey() + ": " + entry.getValue();
	    writeln(line);	    
	}
	writeln("");
    }

    @Override
    public void onConnectionLost() {
	//Log.d("HttpClientProtocol", "client protocol connection Lost");
    }

    @Override
    public void onLineReceived(String line) {
	line = line.trim();
	if(responseState == 0) { // Response line
	    String[] args = line.split(" ", 3);
	    code = Integer.parseInt(args[1]);
	    codeDescription = args[2];
	    responseState = 1;
	} else if(responseState == 1) { // Headers
	    String[] items = line.split(":", 2);
	    if(items.length < 2) {
		responseState = 2;
		setRawMode(true);
	    } else {
		headers.put(items[0].trim().toLowerCase(), items[1].trim());
	    }
	}
    }
}