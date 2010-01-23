/**
 * 
 */
package org.deb.connection.spp_slave_service;

import java.io.IOException;

import org.deb.connection.bluetooth_thread.ConnectedThread;
import org.deb.connection.bluetooth_thread.Constants;

import android.os.Message;
import backport.android.bluetooth.BluetoothSocket;

final class MyConnectedThrad extends ConnectedThread {

	/**
	 * 
	 */
	private final SPPSlaveService bimejiSPPService;

	MyConnectedThrad(SPPSlaveService bimejiSPPService, BluetoothSocket socket) {
		super(socket);
		this.bimejiSPPService = bimejiSPPService;
	}

	public void connectionLost(IOException error) {
		Message msg = this.bimejiSPPService.mHandler.obtainMessage(Constants.MESSAGE_CONNECTION_LOST, error
				.toString());
		msg.sendToTarget();
	}

	@Override
	public void onReceive(byte[] buffer, int bytes) {
		Message msg = this.bimejiSPPService.mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1);
		msg.sendToTarget();

		// for debug
		String reprData = Integer.toHexString(bytes);
		Message dmsg = this.bimejiSPPService.mHandler.obtainMessage(Constants.MESSAGE_SYS_OUT, reprData);
		dmsg.sendToTarget();
	}

}