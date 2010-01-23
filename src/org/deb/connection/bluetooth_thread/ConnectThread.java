package org.deb.connection.bluetooth_thread;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import backport.android.bluetooth.BluetoothDevice;
import backport.android.bluetooth.BluetoothSocket;

public abstract class ConnectThread extends Thread {

	final BluetoothDevice mRemoteDevice;
	final UUID mUuid;
	final ReentrantReadWriteLock mLock;
	boolean mClosed;
	BluetoothSocket mSocket;

	protected ConnectThread(BluetoothDevice remoteDevice, UUID uuid) {
		mRemoteDevice = remoteDevice;
		mUuid = uuid;
		mLock = new ReentrantReadWriteLock();
	}

	@Override
	public void run() {
		boolean succeed = ensureSocket();

		if (!succeed) {
			return;
		}

		waitForConnectionEstablished();
	}

	public final void cancel() {
		mLock.readLock().lock();

		try {
			if (mClosed) {
				return;
			}
		} finally {
			mLock.readLock().unlock();
		}

		mLock.writeLock().lock();

		try {
			mClosed = true;
		} finally {
			mLock.writeLock().unlock();
		}

		try {
			mSocket.close();
		} catch (IOException e) {
		}
	}

	final boolean ensureSocket() {
		try {
			BluetoothSocket socket = mRemoteDevice
					.createRfcommSocketToServiceRecord(mUuid);
			mSocket = socket;
			return true;
		} catch (IOException e) {
			connectionFailure(e);
		}

		return false;
	}

	final void waitForConnectionEstablished() {
		try {
			mSocket.connect();
		} catch (IOException e) {
			mLock.readLock().lock();

			try {
				if (mClosed) {
					return;
				}
			} finally {
				mLock.readLock().unlock();
			}

			cancel();
			connectionFailure(e);
			return;
		}

		mLock.writeLock().lock();

		try {
			mClosed = true;
			manageConnectedSocket(mSocket);
		} finally {
			mLock.writeLock().unlock();
		}
	}

	protected abstract void manageConnectedSocket(BluetoothSocket socket);

	protected abstract void connectionFailure(IOException cause);
}
