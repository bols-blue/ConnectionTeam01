package org.deb.connection.spp_slave_service;

import org.deb.connection.bluetooth_thread.ConnectedThread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SPPBroadcastReceiver extends BroadcastReceiver {
	private ConnectedThread mConnectedThread;
	
	SPPBroadcastReceiver(ConnectedThread connectedThread){
		super();
		mConnectedThread = connectedThread;
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		 Log.d("TEST","Received intent "+intent);
		 int data = intent.getIntExtra("data", 20);
		mConnectedThread.writeInt(data);
	}
}
