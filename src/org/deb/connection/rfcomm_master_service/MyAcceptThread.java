/**
 * 
 */
package org.deb.connection.rfcomm_master_service;

import java.io.IOException;
import java.util.UUID;

import org.deb.connection.bluetooth_thread.AcceptThread;
import org.deb.connection.bluetooth_thread.Constants;

import android.os.Message;
import backport.android.bluetooth.BluetoothSocket;

final class MyAcceptThread extends AcceptThread {

	/**
	 * 
	 */
	private final MasterService masterService;

	MyAcceptThread(MasterService masterService, String name, UUID uuid) {
		super(name, uuid);
		this.masterService = masterService;
	}

	@Override
	protected void connectionFailure(IOException cause) {
		Message msg = this.masterService.mHandler.obtainMessage(Constants.MESSAGE_FAIL);
		msg.sendToTarget();
	}

	@Override
	protected void manageConnectedSocket(BluetoothSocket socket) {
		this.masterService.mLocalDevice.cancelDiscovery();
		this.masterService.mRemoteDevice = socket.getRemoteDevice();
		Message msg = this.masterService.mHandler.obtainMessage(Constants.MESSAGE_CONNECTED);
		msg.sendToTarget();
		this.masterService.mConnectedThread = new MyConnectedThread(this.masterService, socket);
		this.masterService.mConnectedThread.start();
	}

	@Override
	protected void listenStarted(UUID uuid) {
		Message msg = this.masterService.mHandler.obtainMessage(Constants.MESSAGE_START);
		msg.sendToTarget();
	}
}