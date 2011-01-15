package mobileup.android;

import mobileup.web.HttpProtocol;
import mobileup.sensorweb.WireProcessor;
import mobileup.web.FileProcessor;
import mobileup.web.Processor;

public class AssetProtocol extends HttpProtocol
{
    public Processor route() {
	if(object.equals("/websockets")){
	    return new WireProcessor();
	} else if(object.startsWith("/sd/")){
	    return new FileProcessor("/sdcard", "/sd");	    
	}
	AssetFactory sf = (AssetFactory)factory;
        return new AssetProcessor(sf.context.getAssets());
    }
}

