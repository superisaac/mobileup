/** %LICENSE% **/
package mobileup.sensorweb.rpc;
import mobileup.sensorweb.Handler;
import mobileup.json.*;

public class HelloHandler extends Handler
{
    public Object called(String methodName, JSONArray args) {
	return "Hello";
    }
}
