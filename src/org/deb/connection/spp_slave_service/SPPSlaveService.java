package org.deb.connection.spp_slave_service;



import org.deb.connection.DeviceListActivity;
import org.deb.connection.bluetooth_thread.ConnectThread;
import org.deb.connection.bluetooth_thread.ConnectedThread;
import org.deb.connection.bluetooth_thread.Constants;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;

public final class SPPSlaveService extends Service {

	final class MyHandler extends Handler implements Constants {

		@Override
		public void handleMessage(Message msg) {
			String reprObj;
			switch (msg.what) {
			case MESSAGE_CONTROL_READ:
				break;
			case MESSAGE_CONTROL_CONNECTED:
				reprObj = (String) msg.obj;
				showToast("connected control:\n" + reprObj);
				break;
			case MESSAGE_READ:
				int intVal = msg.arg1;
				break;
			case MESSAGE_CONNECTED:
				reprObj = (String) msg.obj;
				showToast("connected:\n" + reprObj);
				break;
			case MESSAGE_CONNECTION_LOST:
				reprObj = (String) msg.obj;
				showToast("connection lost:\n" + reprObj);
				break;
			case MESSAGE_CONNECTION_FAIL:
				reprObj = (String) msg.obj;
				showToast("faild to connect:\n" + reprObj);
				break;
			case MESSAGE_TOAST:
				reprObj = (String) msg.obj;
				showToast(reprObj);
				break;
			case MESSAGE_SYS_OUT:
				reprObj = (String) msg.obj;
				System.out.println(reprObj);
				break;
			}
		}
	}

	BluetoothAdapter mLocalDevice;
	BluetoothDevice mRemoteDevice;
	ConnectThread mConnectThread;
	ConnectedThread mConnectedThread;
	Handler mHandler = new MyHandler();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	void startClient() {
		BluetoothDevice remDev = obtainRemoteDevice();

		if (remDev == null) {
			Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST,
					"NO DEVICE SELECTED");
			msg.sendToTarget();
			return;
		}

		cancelThread();
		mConnectThread = new MyConnectThread(this, remDev);
		mConnectThread.start();

	}

	@Override
	public void onCreate() {
		super.onCreate();
		mLocalDevice = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelThread();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		String address = intent.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		mRemoteDevice = mLocalDevice.getRemoteDevice(address);
		startClient();
		onBinding();
	}
	
	public void onBinding(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.intent.action.ALL_APPS");
		try {
			intentFilter.addDataType("text/bt_test");
		} catch (MalformedMimeTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		registerReceiver(new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			 Log.d("TEST","Received intent "+intent);
			 int data = intent.getIntExtra("data", 20);
			mConnectedThread.writeInt(data);
		}

		}, intentFilter); 
	}
	
	void cancelThread() {
		if (mConnectThread != null) {
			mConnectThread.cancel();
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
		}
	}

	BluetoothDevice obtainRemoteDevice() {
		return mRemoteDevice;
	}

	void reconnect() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		cancelThread();
		mConnectThread = new MyConnectThread(this, mRemoteDevice);
		mConnectThread.start();
	}

	void showToast(String text) {
		Toast t = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		t.show();
	}
	
}