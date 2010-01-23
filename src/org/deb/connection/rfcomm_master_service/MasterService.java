package org.deb.connection.rfcomm_master_service;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.deb.connection.bluetooth_thread.AcceptThread;
import org.deb.connection.bluetooth_thread.ConnectedThread;
import org.deb.connection.bluetooth_thread.Constants;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;
import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;

public final class MasterService extends Service {

	final class MyHandler extends Handler implements Constants{

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_READ:
				byte[] raw = (byte[]) msg.obj;
				int bytes = msg.arg2;
				ByteArrayInputStream buf = new ByteArrayInputStream(raw, 0,
						bytes);
				System.out.println("receive:" + Arrays.toString(raw));
				DataInputStream in = new DataInputStream(buf);
				KeyEvent event = null;
				try {
					int action = in.readInt();
					int keyCode = in.readInt();
					int repeat = in.readInt();
					int metaState = in.readInt();
					int device = in.readInt();
					int scanCode = in.readInt();
					int flags = in.readInt();
					long downTime = in.readLong();
					long eventTime = in.readLong();
					event = new KeyEvent(downTime, eventTime, action, keyCode,
							repeat, metaState, device, scanCode, flags);
					//System.out.println("event:" + event.toString());
				} catch (IOException e) {
				} finally {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
		        Intent intent = new Intent();

		        intent.setAction(Intent.ACTION_ALL_APPS);
		        intent.setType("text/bt_test");
		        intent.putExtra("data", 20);
		        sendBroadcast(intent);
				break;
			case MESSAGE_START:
				showToast(SERVICE + ":server started");
				break;
			case MESSAGE_STOP:
				showToast(SERVICE + ":server stopped");
				break;
			case MESSAGE_CONNECTED:
				showToast(SERVICE + ":" + mRemoteDevice.getName()
						+ " is connected");
				break;
			case MESSAGE_CONNECTION_LOST:
				showToast(SERVICE + ":" + mRemoteDevice.getName()
						+ "is disconneted");
				mConnectedThread.cancel();
				break;
			case MESSAGE_FAIL:
				showToast(SERVICE + ":unable to start server");
				break;
			}
		}
	}

	static final String SERVICE = "Simeji";
	static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805f9b34fb");


	BluetoothAdapter mLocalDevice;
	BluetoothDevice mRemoteDevice;
	AcceptThread mAcceptThread;
	ConnectedThread mConnectedThread;
	final Handler mHandler;
	final ReentrantReadWriteLock mLock;
	boolean mStop;

	public MasterService() {
		mLock = new ReentrantReadWriteLock();
		mHandler = new MyHandler();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mLocalDevice = BluetoothAdapter.getDefaultAdapter();
		mStop = true;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		startServer();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopServer();
		Message msg = mHandler.obtainMessage(Constants.MESSAGE_STOP);
		msg.sendToTarget();
	}

	void showToast(String text) {
		Toast t = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		t.show();
	}

	void startServer() {
		mLock.readLock().lock();

		try {
			if (!mStop) {
				return;
			}
		} finally {
			mLock.readLock().unlock();
		}

		mLock.writeLock().lock();

		try {
			mStop = false;
			mAcceptThread = new MyAcceptThread(this, SERVICE, MY_UUID);
			mAcceptThread.start();
		} finally {
			mLock.writeLock().unlock();
		}
	}

	void stopServer() {
		mLock.readLock().lock();

		try {
			if (mStop) {
				return;
			}
		} finally {
			mLock.readLock().unlock();
		}

		mLock.writeLock().lock();

		try {
			mStop = true;

			if (mAcceptThread != null) {
				mAcceptThread.cancel();
			}

			if (mConnectedThread != null) {
				mConnectedThread.cancel();
			}
		} finally {
			mLock.writeLock().unlock();
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}