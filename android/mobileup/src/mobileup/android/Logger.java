/** %LICENSE% **/
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
