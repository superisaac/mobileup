/** %LICENSE% **/
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
import mobileup.web.AckProcessor;
import mobileup.web.HttpProtocol;
import mobileup.web.Processor;
import mobileup.web.FileProcessor;
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

class SensorProtocol extends HttpProtocol
{
    public Processor route() {
	if(object.equals("/websockets")){
	    return new WireProcessor();
	} else if(object.startsWith("/sd/")){
	    return new FileProcessor("/sdcard", "/sd");	    
	}
	//SensorFactory sf = (SensorFactory)factory;
        //return new AssetProcessor(sf.context.getAssets());
	return new FileProcessor("/sdcard/www");
    }
}

class SensorFactory implements IFactory {
    public Context context;
    public Protocol buildProtocol() {
	return new SensorProtocol();
    }
}

public class SensorContext extends HandlerFactory
{
    private Context context;
    SensorManager sensorManager;

    public SensorContext(Context context) {
	this.context = context;
	sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	SystemHandler.docRoot = "/sdcard";
    }

    public void detach() {
	this.context = null;
    }

    public int start() {
	mobileup.util.Log.registerLogger(new Logger());
	
	Reactor reactor = Reactor.getInstance();
	try {
	    SensorFactory factory = new SensorFactory();
	    factory.context = context;
	    reactor.listen(null, 5899, factory);
	    reactor.start();
	}catch(IOException e) {
	    e.printStackTrace();
	    Log.e("SensorContext", e.toString());
	    return 0;
	}
	prepareQueues();
	return 1;
    }

    public int stop () {
	Reactor reactor = Reactor.getInstance();
	reactor.kill();
	Log.d("SensorContext", "on stop");
	return 1;
    }

    public int checkRunning() {
	Reactor reactor = Reactor.getInstance();
	return reactor.isRunning()? 1: 0;
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