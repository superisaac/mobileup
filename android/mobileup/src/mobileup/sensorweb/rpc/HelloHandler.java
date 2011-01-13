/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.sensorweb.rpc;
import mobileup.sensorweb.Handler;
import mobileup.json.*;

public class HelloHandler extends Handler
{
    public Object called(String methodName, JSONArray args) {
	return "Hello";
    }
}
