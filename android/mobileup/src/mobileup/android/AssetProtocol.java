/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/

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

