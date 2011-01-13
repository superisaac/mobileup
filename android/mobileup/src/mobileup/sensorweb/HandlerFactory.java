/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.sensorweb;

import mobileup.sensorweb.rpc.*;

public class HandlerFactory
{
    public static HandlerFactory instance = new HandlerFactory();

    public Handler getHandler(String objectName) {
	if(objectName.equals("system")) {
	    return new SystemHandler();
	}
	return null;
    }
}