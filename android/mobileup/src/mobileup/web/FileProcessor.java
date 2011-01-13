/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.web;
import java.io.*;
import java.net.*;
import java.util.*;
import mobileup.util.Log;
import mobileup.util.MimeManager;

public class FileProcessor extends Processor
{
    public String docRoot;
    private String prefix;
    public FileProcessor(String docRoot, String prefix) {
	this.prefix = prefix;
	Log.d("FileProcessor", prefix);
	try {
	    this.docRoot = new File(docRoot).getCanonicalPath();
	} catch(IOException e) {
	    this.docRoot = docRoot;
	}
    }
    public FileProcessor(String docRoot) {
	this(docRoot, "");
    }

    /*public void processData(HttpProtocol request, byte[] data, int offset, int numRead) {

      }*/

    public void process(HttpProtocol request) {
        try{
            String path = request.getURL().getPath();
            if(path.equals("/")) {
                path += "index.html";
            }
	    path = path.substring(prefix.length());

	    File inputf;
	    try {
		Log.d("FileProcessor", "file://" + docRoot + path);
		inputf = new File(new URI("file://" + docRoot + path));
	    } catch(URISyntaxException e) {
		throw new Http500(e.toString());
	    }
	    
            Response response = request.response;
            if(inputf.isFile()) {
		response.mimeType = MimeManager.getMimeType(inputf.getName());
		Log.d("FileProcessor", "mime type " + response.mimeType);
		Log.d("FileProcessor", "file length " + inputf.length());
                response.begin(inputf.length());
                BufferedInputStream buffer = new BufferedInputStream(
						 new FileInputStream(inputf));
		int sumal = 0;
                while(true) {
                    byte[] b = new byte[4096];
                    int al = buffer.read(b, 0, b.length);
                    if(al < 0) {
                        break;
                    }
		    sumal += al;
                    response.send(b, 0, al);
                }
		Log.d("FileProcessor", "total read bytes " + sumal);
                response.finish();
                buffer.close();
            } else {
                throw new Http404();
            }
        } catch(IOException e) {
	    throw new Http500(e.toString());
        } 
    }
}
