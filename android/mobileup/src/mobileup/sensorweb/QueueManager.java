/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.sensorweb;

import java.util.Map;
import java.util.HashMap;

import mobileup.util.Log;
import mobileup.json.*;

public class QueueManager
{
    private static QueueManager instance ;
    public static QueueManager getInstance() {
	if(instance == null) {
	    instance = new QueueManager();
	}
	return instance;
    }

    private Map<String, Queue> queueContainer = new HashMap<String, Queue>();

    public void addQueue(String device, Queue q) {
	queueContainer.put(device, q);
    }

    public Queue get(String device) {
	return get(device, true);
    }

    public synchronized Queue get(String device, boolean create) {
	Queue q = queueContainer.get(device);
	if(q == null && create) {
	    q = new Queue();
	    queueContainer.put(device, q);
	}
	return q;
    }
}
