package superisaac.mobileup.demo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.app.Activity;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceScreen;
import android.content.SharedPreferences;
import android.content.ComponentName;

import android.content.ServiceConnection;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import mobileup.web.HttpProtocol;


import mobileup.sensorweb.HandlerFactory;
import mobileup.network.IFactory;

import mobileup.android.MobileUpContext;
import mobileup.android.Util;
import mobileup.android.CustomWebView;
import mobileup.json.*;

public class Demo extends Activity
{
    View mainView;
    MobileUpContext mMobileUp;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	setContentView(R.layout.main);
	Log.d("Main", "Activity on Create");

	mMobileUp = new MobileUpContext(this);

	if(mainView == null) {
	    mainView = findViewById(R.layout.main);
	    CheckBox cb = (CheckBox)findViewById(R.id.service_control);
	    final Context context = this;
	    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
		    if(isChecked) {
			cb.setText("Server at http://" + Util.getIPAddr(context) + ":5899/");
		    } else {
			cb.setText("Service stopped.");
		    }
		}
		});
	    Log.d("Main", "Reset View");
	    CustomWebView browser = (CustomWebView)findViewById(R.id.content_view);
	    browser.resetView();
	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
	CustomWebView browser = (CustomWebView)findViewById(R.id.content_view);
	browser.saveState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedState) {
	CustomWebView browser = (CustomWebView)findViewById(R.id.content_view);
	browser.restoreState(savedState);
    }


    @Override
    public void onDestroy() {
	Log.d("Main", "Activity on Destroy");
	//unbindService(this);
	mMobileUp.detach();
	super.onDestroy();
    }
    
    public void onServiceControl(View v) {
	CheckBox cb = (CheckBox)v;
	if(cb.isChecked()) {
	    startService();	    
	} else {
	    stopService();
	}
    }
    public void stopService() {
	if(mMobileUp != null) {
	    mMobileUp.stop();
	}
    }

    public void startService() {
	if(mMobileUp != null) { // && mMobileUp.checkRunning() != 0) {
	    Log.d("Main", "start running");
	    mMobileUp.start(5899);
	    CustomWebView browser = (CustomWebView)findViewById(R.id.content_view);
	    browser.loadUrl("http://localhost:5899");
	}
    }
};

