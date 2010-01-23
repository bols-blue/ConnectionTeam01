package org.deb.connection.bluetooth_thread;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothServerSocket;
import backport.android.bluetooth.BluetoothSocket;

public abstract class AcceptThread extends Thread {

	final String mServiceName;
	final UUID mUuid;
	BluetoothServerSocket mServerSocket;
	final ReentrantReadWriteLock mLock;
	boolean mClosed;

	protected AcceptThread(String name, UUID uuid) {
		mServiceName = name;
		mUuid = uuid;
		mLock = new ReentrantReadWriteLock();
	}

	@Override
	public void run() {
		boolean succeed = ensureServerSocket();

		if (!succeed) {
			return;
		}

		listenStarted(mUuid);
		waitForConnectionEstablished();
	}

	public void cancel() {
		if (mServerSocket == null) {
			return;
		}

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
			try {
				mServerSocket.close();
			} catch (IOException e) {
			}
		} finally {
			mLock.writeLock().unlock();
		}
	}

	boolean ensureServerSocket() {
		try {
			BluetoothAdapter locDev = BluetoothAdapter.getDefaultAdapter();
			BluetoothServerSocket tmp = null;
			tmp = locDev
					.listenUsingRfcommWithServiceRecord(mServiceName, mUuid);
			mServerSocket = tmp;
			return true;
		} catch (IOException e) {
			connectionFailure(e);
		}

		return false;
	}

	void waitForConnectionEstablished() {
		BluetoothSocket socket = null;

		for (;;) {
			try {
				socket = mServerSocket.accept();
			} catch (IOException e) {
				mLock.readLock().lock();

				try {
					if (mClosed) {
						break;
					}
				} finally {
					mLock.readLock().unlock();
				}

				cancel();
				connectionFailure(e);
				break;
			}

			if (socket != null) {
				manageConnectedSocket(socket);
				cancel();
				break;
			}
		}
	}

	protected abstract void listenStarted(UUID uuid);

	protected abstract void connectionFailure(IOException cause);

	protected abstract void manageConnectedSocket(BluetoothSocket socket);
}