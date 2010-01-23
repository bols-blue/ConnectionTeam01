/**
 * 
 */
package org.deb.connection.rfcomm_master_service;

import java.io.IOException;

import org.deb.connection.bluetooth_thread.ConnectedThread;
import org.deb.connection.bluetooth_thread.Constants;

import android.os.Message;
import backport.android.bluetooth.BluetoothSocket;

final class MyConnectedThread extends ConnectedThread {

	/**
	 * 
	 */
	private final MasterService masterService;

	MyConnectedThread(MasterService masterService, BluetoothSocket socket) {
		super(socket);
		this.masterService = masterService;
	}

	@Override
	public
	void connectionLost(IOException error) {
		Message msg = this.masterService.mHandler.obtainMessage(Constants.MESSAGE_CONNECTION_LOST);
		msg.sendToTarget();
		this.masterService.mLock.readLock().lock();

		try {
			if (this.masterService.mStop) {
				return;
			}
		} finally {
			this.masterService.mLock.readLock().unlock();
		}

		this.masterService.startServer();
	}

	@Override
	public	void onReceive(byte[] buffer, int bytes) {
		Message msg = this.masterService.mHandler
				.obtainMessage(Constants.MESSAGE_READ, 0, bytes, buffer);
		msg.sendToTarget();
	}
}