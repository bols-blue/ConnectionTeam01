/**
 * 
 */
package org.deb.connection.spp_slave_service;

import static org.deb.connection.spp_slave_service.UUID_Constants.SERIAL_PORT_PROFILE;

import java.io.IOException;

import org.deb.connection.bluetooth_thread.ConnectThread;
import org.deb.connection.bluetooth_thread.Constants;

import android.os.Message;
import backport.android.bluetooth.BluetoothDevice;
import backport.android.bluetooth.BluetoothSocket;

final class MyConnectThread extends ConnectThread {

	/**
	 * 
	 */
	private final SPPSlaveService bimejiSPPService;

	MyConnectThread(SPPSlaveService bimejiSPPService, BluetoothDevice remoteDevice) {
		super(remoteDevice, SERIAL_PORT_PROFILE);
		this.bimejiSPPService = bimejiSPPService;
	}

	protected void connectionFailure(IOException cause) {
		Message msg = this.bimejiSPPService.mHandler.obtainMessage(Constants.MESSAGE_CONNECTION_FAIL, cause
				.toString());
		msg.sendToTarget();
		this.bimejiSPPService.reconnect();
	}

	@Override
	protected void manageConnectedSocket(BluetoothSocket socket) {
		BluetoothDevice remDev = socket.getRemoteDevice();
		String name = remDev.getName();
		String addr = remDev.getAddress();
		Message msg = this.bimejiSPPService.mHandler.obtainMessage(Constants.MESSAGE_CONTROL_CONNECTED,
				(name + "\n" + addr));
		msg.sendToTarget();
		this.bimejiSPPService.mConnectedThread = new MyConnectedThrad(this.bimejiSPPService, socket);
		this.bimejiSPPService.mConnectedThread.start();
	}
}