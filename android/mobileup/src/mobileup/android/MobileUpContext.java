/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.android;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

import mobileup.network.Reactor;
import mobileup.network.Protocol;
import mobileup.network.IFactory;
import mobileup.sensorweb.WireProcessor;
import mobileup.sensorweb.Queue;
import mobileup.sensorweb.QueueManager;

import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import mobileup.sensorweb.HandlerFactory;
import mobileup.sensorweb.rpc.SystemHandler;
import mobileup.sensorweb.Handler;

class SensorQueue extends Queue implements SensorEventListener
{
    SensorManager sensorManager;
    int sensorType;

    public SensorQueue(SensorManager sensorManager, int sensorType)
    {
	this.sensorManager = sensorManager;
	this.sensorType = sensorType;
    }

    @Override
    public void sub(WireProcessor processor) {
        super.sub(processor);
        if(list.size() == 1) {
	    startSensor();
	}
    }

    @Override
    public void unsub(WireProcessor processor) {
	super.unsub(processor);
	if(list.size() == 0) {
	    stopSensor();
	}
	Log.d("SensQueue", "unsub " + list.size());
    }

    private void startSensor() {
	Log.d("QriQueue", "start sensor sen.ori");
	Sensor sensor = sensorManager.getDefaultSensor(sensorType);
	sensorManager.registerListener(this, sensor,
				       SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    public void stopSensor() {
	Log.d("SensQueue", "stop sensor sen.ori");
	if(sensorManager != null) {
	    sensorManager.unregisterListener(this);
	}
    }
    
    public void finalize() {
	Log.d("SensorQueue", "finalize");
	stopSensor();
    }

    public void onSensorChanged(SensorEvent event){
	float[] values = event.values;
        String[] args = new String[values.length];
        for(int i=0; i< values.length; i++) {
            args[i] = String.valueOf(values[i]);
        }
	put("sen.ori", args);	
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
	//Log.d("SensQueue", "accuracy changed to " + accuracy);
    }
}

public class MobileUpContext extends HandlerFactory
{
    private Context context;
    SensorManager sensorManager;

    public MobileUpContext(Context context) {
	this.context = context;
	sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	SystemHandler.docRoot = "/sdcard";
    }

    public void detach() {
	this.context = null;
    }

    public int start(int port) {
	AssetFactory factory = new AssetFactory(this.context);
	return start(factory, port);
    }

    public int start(IFactory factory, int port) {
	mobileup.util.Log.registerLogger(new Logger());
	
	Reactor reactor = Reactor.getInstance();
	try {
	    reactor.listen(null, port, factory);
	    reactor.start();
	}catch(IOException e) {
	    e.printStackTrace();
	    Log.e("MobileUpContext", e.toString());
	    return 0;
	}
	prepareQueues();
	return 1;
    }

    public int stop () {
	Reactor reactor = Reactor.getInstance();
	reactor.kill();
	Log.d("MobileUpContext", "on stop");
	return 1;
    }

    private void addSensorQueue(String name, SensorQueue q) {
	QueueManager qm = QueueManager.getInstance();
	Queue oldq = qm.get(name, false);
	if(oldq != null && oldq instanceof SensorQueue) {
	    ((SensorQueue)oldq).stopSensor();
	}
	qm.addQueue(name, q);
    }

    
    private void prepareQueues() {
	addSensorQueue("sen.ori",
		       new SensorQueue(sensorManager, Sensor.TYPE_ORIENTATION));
    }    

    @Override
    public Handler getHandler(String objectName) {
	if(objectName.equals("phone")) {
	    return new PhoneHandler(context);
	}
	return super.getHandler(objectName);
    }
}
