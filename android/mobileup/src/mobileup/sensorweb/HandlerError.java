/** %LICENSE% **/
package mobileup.sensorweb;

public class HandlerError extends RuntimeException
{
    String message;
    public HandlerError(String msg) {
	message = msg;
    }
    public String getMessage() {
	return message;
    }
}
