/** %LICENSE% **/
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