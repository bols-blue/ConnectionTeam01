package org.deb.connection.bluetooth_thread;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import backport.android.bluetooth.BluetoothSocket;

public abstract class ConnectedThread extends Thread {

	static final int BUFFER_SIZE = 1024;
	final BluetoothSocket mSocket;
	final DataInputStream mInStream;
	final DataOutputStream mOutStream;
	final ReentrantReadWriteLock mLock;
	boolean mClosed;

	protected ConnectedThread(BluetoothSocket socket) {
		mSocket = socket;
		InputStream inStream = null;
		OutputStream outStream = null;

		try {
			inStream = socket.getInputStream();
			outStream = socket.getOutputStream();
		} catch (IOException e) {
		}

		mInStream = new DataInputStream(inStream);
		mOutStream = new DataOutputStream(outStream);
		mLock = new ReentrantReadWriteLock();
	}

	@Override
	public final void run() {
		byte[] buff = new byte[BUFFER_SIZE];
		int intVal;
		for (;;) {
			try {
				intVal = mInStream.read(buff);
				onReceive(buff, intVal);
			} catch (IOException e) {
				mLock.readLock().lock();

				try {
					if (mClosed) {
						break;
					}
				} finally {
					mLock.readLock().unlock();
				}

				connectionLost(e);
				cancel();
				break;
			}
		}
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

	public final void writeInt(int val) {
		try {
			mOutStream.writeInt(val);
		} catch (IOException e) {
			connectionLost(e);
			cancel();
		}
	}

	public abstract void connectionLost(IOException error);

	public abstract void onReceive(byte[] buffer, int bytes);

}
