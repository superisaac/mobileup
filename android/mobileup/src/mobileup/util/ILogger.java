/** %LICENSE% **/

package mobileup.util;

public interface ILogger {
    public void e(String directive, String content);
    public void w(String directive, String content);
    public void d(String directive, String content);
}
