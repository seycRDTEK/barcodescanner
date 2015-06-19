package com.eyc.plugins;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/*
 * Common Barcode Scanner Plugin
 */
public class BarcodeScanner extends CordovaPlugin {
	public static final String TAG = "BarcodeScanner";

	public final static String SEND_FINAL_BARCODE = "com.eyc.plugins.BarcodeScanner.barcodeReceiver.SEND_FINAL_BARCODE";
	public final static String BARCODE = "BARCODE";
	public final static String PLUGINSTATUS = "PLUGINSTATUS";
	public final static String KEEPCALLBACK = "KEEPCALLBACK";

	public final static String ACTION_RECEIVE_BARCODE = "startBarcodeReceiving";
	public final static String ACTION_STOP_BARCODE = "stopBarcodeReceiving";

	private boolean isReceiving = false;
	private CallbackContext callback_receive;
	private ScanReceiver scanReceiver;

	public BarcodeScanner() {
		super();
	}

	@Override
	public boolean execute(String action, JSONArray arg1,
			final CallbackContext callbackContext) throws JSONException {
		Log.d(TAG, "execute : "+action);
		if (action.equals(ACTION_RECEIVE_BARCODE)) {

			// if already receiving (this case can happen if the startReception
			// is called
			// several times
			if (this.isReceiving) {
				// close the already opened callback ...
				PluginResult pluginResult = new PluginResult(
						PluginResult.Status.NO_RESULT);
				pluginResult.setKeepCallback(false);
				this.callback_receive.sendPluginResult(pluginResult);
				// ... before registering a new one to the  receiver
			}

			this.isReceiving = true;
			if (this.scanReceiver == null) {
				this.scanReceiver = new ScanReceiver(callbackContext);
				IntentFilter fp = new IntentFilter(SEND_FINAL_BARCODE);
				//register the broadcast
				LocalBroadcastManager.getInstance(this.cordova.getActivity().getApplicationContext()).registerReceiver(this.scanReceiver, fp);			
			}

			this.scanReceiver.startReceiving(callbackContext);


			PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
			pluginResult.setKeepCallback(true);
			callbackContext.sendPluginResult(pluginResult);
			this.callback_receive = callbackContext;
			return true;


		} else if (action.equals(ACTION_STOP_BARCODE)) {
			if (this.scanReceiver != null) {
				this.scanReceiver.stopReceiving();
			}
			this.isReceiving = false;

			// 1. Stop the receiving context
			PluginResult pluginResult = new PluginResult(
					PluginResult.Status.NO_RESULT);
			pluginResult.setKeepCallback(false);
			this.callback_receive.sendPluginResult(pluginResult);
			// 2. Send result for the current context
			pluginResult = new PluginResult(PluginResult.Status.OK);
			callbackContext.sendPluginResult(pluginResult);


			// 3. Stop receiving barcode
			// All barcode scanners can listen to this plugin and stop
			Intent intentToStop = new Intent(ACTION_STOP_BARCODE);
			LocalBroadcastManager.getInstance(this.cordova.getActivity().getApplicationContext()).sendBroadcast(intentToStop);
			return true;
		}

		return false;
	}


	private class ScanReceiver extends BroadcastReceiver {
		private CallbackContext callback_receive;
		private boolean isReceiving = false;

		public ScanReceiver(CallbackContext callback){
			super();
			this.callback_receive = callback;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String sMsg = intent.getStringExtra(BARCODE);
			Status status =  (Status) intent.getSerializableExtra(PLUGINSTATUS);
			boolean bCallBack =  intent.getBooleanExtra(KEEPCALLBACK, false);

			PluginResult result = new PluginResult(status, sMsg);
			result.setKeepCallback(bCallBack);

			if(callback_receive != null)
				callback_receive.sendPluginResult(result);

			Log.d(TAG, "messageScaned : "+sMsg);
		}


		public void startReceiving(CallbackContext ctx) {
			this.callback_receive = ctx;
			this.isReceiving = true;
		}

		public void stopReceiving() {
			this.callback_receive = null;
			this.isReceiving = false;
		}
	};



	public static Intent getBroadCastIntent(Status s, String msg, boolean bKeepCallback){

		Intent broadCastIntent = new Intent(SEND_FINAL_BARCODE);
		broadCastIntent.putExtra(PLUGINSTATUS , s);
		broadCastIntent.putExtra(BARCODE , msg);
		broadCastIntent.putExtra(KEEPCALLBACK , bKeepCallback);

		return broadCastIntent;

	}




}
