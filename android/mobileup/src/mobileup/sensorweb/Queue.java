/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
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