package com.polution.bluetooth;

import java.util.concurrent.ThreadPoolExecutor;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public abstract class CustomIntentService extends Service {
	    private volatile Looper mServiceLooper;
	    private volatile ServiceHandler mServiceHandler;
	    private String mName;
	    private boolean mRedelivery;

	    private static final int STOP_SERVICE = -1;
	    private final class ServiceHandler extends Handler {
	        
	    	private int lastID;
	    	
	    	public ServiceHandler(Looper looper) {
	            super(looper);
	        }

	        @Override
	        public void handleMessage(Message msg) {
	           
	        	if(msg.arg1 == STOP_SERVICE && msg.arg2 == STOP_SERVICE)
	        		stopSelf(lastID);
	        	else{
	        		lastID = msg.arg1;
	        		onHandleIntent((Intent)msg.obj);	
	        	}
	        }
	        
	    }

	    public CustomIntentService(String name) {
	        super();
	        mName = name;
	    }

	    
	    public void stopServiceCustom(){
	    	 Message msg = mServiceHandler.obtainMessage();
		     msg.arg1 = STOP_SERVICE;
		     msg.arg2 = STOP_SERVICE;
		     mServiceHandler.sendMessage(msg);
	    }
	    /**
	     * Control redelivery of intents.  If called with true,
	     * {@link #onStartCommand(Intent, int, int)} will return
	     * {@link Service#START_REDELIVER_INTENT} instead of
	     * {@link Service#START_NOT_STICKY}, so that if this service's process
	     * is called while it is executing the Intent in
	     * {@link #onHandleIntent(Intent)}, then when later restarted the same Intent
	     * will be re-delivered to it, to retry its execution.
	     */
	    public void setIntentRedelivery(boolean enabled) {
	        mRedelivery = enabled;
	    }
	    
	    @Override
	    public void onCreate() {
	        super.onCreate();
	        
	        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
	        thread.start();

	        mServiceLooper = thread.getLooper();
	        mServiceHandler = new ServiceHandler(mServiceLooper);
	    }

	    @Override
	    public void onStart(Intent intent, int startId) {
	        Message msg = mServiceHandler.obtainMessage();
	        msg.arg1 = startId;
	        msg.obj = intent;
	        mServiceHandler.sendMessage(msg);
	    }

	    @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
	        onStart(intent, startId);
	        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
	    }
	    
	    @Override
	    public void onDestroy() {
	        mServiceLooper.quit();
	    }

	    @Override
	    public IBinder onBind(Intent intent) {
	        return null;
	    }

	    /**
	     * Invoked on the Handler thread with the {@link Intent} that is passed to {@link #onStart}.
	     * Note that this will be invoked from a different thread than the one that handles the
	     * {@link #onStart} call.
	     */
	    protected abstract void onHandleIntent(Intent intent);
	}
