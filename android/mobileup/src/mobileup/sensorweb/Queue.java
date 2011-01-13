/** %LICENSE% **/
package mobileup.sensorweb;
import java.util.*;
import mobileup.util.Log;

public class Queue
{
    protected List<WireProcessor> list = new Vector<WireProcessor>();
    public void sub(WireProcessor processor) {
	Log.d("Queue", "add " + processor);
	list.add(processor);
    }

    public void unsub(WireProcessor processor) {
	Log.d("Queue", "remove " + processor);
	list.remove(processor);
    }

    public void put(String[] args) {
	for(WireProcessor processor: list) {
	    processor.sendMessage("ntf", args);
	}
    }
    public void put(String qname, String[] args) {
	for(WireProcessor processor: list) {
	    processor.sendQueue(qname, args);
	}
    }

}