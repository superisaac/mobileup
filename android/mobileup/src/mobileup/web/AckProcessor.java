/** %LICENSE% **/

package mobileup.web;
import java.io.*;
import java.net.*;
import java.util.*;
import mobileup.util.Log;

public class AckProcessor extends Processor
{
    public void process(HttpProtocol request) {
	Response response = request.response;
	String ack = "Ack";
	response.begin(ack.length());
	response.send(ack);
	response.finish();
    }
}