/** %LICENSEx% **/
package mobileup.android;
import java.io.*;
import java.net.*;
import java.util.*;
import mobileup.util.Log;
import mobileup.util.MimeManager;
import mobileup.web.Http500;
import mobileup.web.Http404;
import mobileup.web.Processor;
import mobileup.web.Response;
import mobileup.web.HttpProtocol;
import android.content.res.AssetManager;
import android.content.res.AssetFileDescriptor;

public class AssetProcessor extends Processor
{
    public String docRoot = "www";
    private AssetManager asset;
    public AssetProcessor(AssetManager asset) {
	this.asset = asset;
    }

    public void process(HttpProtocol request) {
        try{
            String path = request.getURL().getPath();
            if(path.equals("/")) {
                path += "index.html";
            }
	    path = docRoot + path;

	    InputStream in;
	    try {
		in = asset.open(path);
	    }catch(java.io.FileNotFoundException e) {
		e.printStackTrace();
		throw new Http404();
	    }
	    
            Response response = request.response;
	    response.mimeType = MimeManager.getMimeType(path);
	    response.begin(in.available());
	    BufferedInputStream buffer = new BufferedInputStream(in);
	    while(true) {
		byte[] b = new byte[1024];
		int al = buffer.read(b, 0, b.length);
		if(al < 0) {
		    break;
		} 
		response.send(b, 0, al);
	    }
	    response.finish();
	    buffer.close();
        } catch(IOException e) {
	    e.printStackTrace();
	    throw new Http500(e.toString());
        } 
    }
}
