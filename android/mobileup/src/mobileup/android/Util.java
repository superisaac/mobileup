/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/

package mobileup.android;
import android.content.Context;
import android.net.wifi.*;

public class Util
{
    public static String getIPAddr(Context context) {
        WifiManager w = (WifiManager)(context.getSystemService(Context.WIFI_SERVICE));
        WifiInfo info = w.getConnectionInfo();
        int addr = info.getIpAddress();
	if(addr == 0) {
	    return "127.0.0.1";
	}
        int a = (addr & 0xff);
        int b = (addr >> 8) & 0xff;
        int c = (addr >> 16) & 0xff;
        int d = (addr >> 24) & 0xff;
        String  wifiAddr = String.format("%d.%d.%d.%d", a, b, c, d);
	return wifiAddr;
    }   
}