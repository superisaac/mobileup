/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.android;
import mobileup.util.ILogger;
import android.util.Log;

public class Logger implements ILogger {
    public void e(String directive, String content)
    {
	Log.e(directive, content);
    }

    public void w(String directive, String content)
    {
	Log.w(directive, content);
    }

    public void d(String directive, String content)
    {
	Log.d(directive, content);
    }
}
