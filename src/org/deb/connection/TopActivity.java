package org.deb.connection;

import java.util.Set;

import org.deb.connection.bluetooth_thread.Constants;
import org.deb.connection.commiunication_server.CommunicateService;
import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TopActivity extends Activity implements OnClickListener {
	final String TAG = "DebCon";
	final Handler mHandler= new MyHandler() ;
	
	final class MyHandler extends Handler implements Constants {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_START:
				showToast(SERVICE + ":server started");
				break;
			case MESSAGE_STOP:
				showToast(SERVICE + ":server stopped");
				break;
			}
		}
	}

	Button bluetoothButton;
	Button handwritingButton;
	BluetoothAdapter mLocalDevice;
	private BluetoothDevice mRemoteDevice;
	private boolean debug = true;
	private String mBTAddress;
	static final private int REQUEST_ENABLE_BT = 100;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		bluetoothButton = (Button) findViewById(R.id.bluetooth_button);
		handwritingButton = (Button) findViewById(R.id.handwriting_button);
		bluetoothButton.setOnClickListener(this);
		handwritingButton.setOnClickListener(this);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bluetooth_button:
			startServer();
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, Constants.REQUEST_CONNECT_DEVICE);
//			init();
			// TODO start bluetooth activity
			Log.v(TAG, "Start Bluetooth Activity");
			break;
		case R.id.handwriting_button:
			// TODO start handwriting activity
			Log.v(TAG, "Start Handwriting Activity");
			break;
		}
	}


	/**
	 * bluetoothデバイスの有無を調べてある場合はmLocalDeviceにセットする。
	 * 
	 * @return デバイスがある場合 true
	 */
	boolean ensureSupported() {
		BluetoothAdapter locDev = BluetoothAdapter.getDefaultAdapter();

		if (locDev == null) {
			return false;
		}

		mLocalDevice = locDev;
		return true;
	}

	/**
	 * BTデバイスが有効にする
	 * 
	 * @return 有効になっている場合true ユーザーに問い合わせをして有効ににする場合 false
	 */
	boolean ensureEnabled() {
		boolean enabled = mLocalDevice.isEnabled();

		if (enabled) {
			return true;
		}

		Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBt, REQUEST_ENABLE_BT);
		return false;
	}

	protected void init() {
		if (this.ensureSupported()) {
			;
		}
		Log.d(TAG, "ensureSupported");
		if (this.ensureEnabled()) {
			;
		}
		Log.d(TAG, "ensureEnabled");
        Set<BluetoothDevice> pairedDevices = mLocalDevice.getBondedDevices();
        Log.d(TAG, "getBondedDevices:" +pairedDevices.size() + ":");
        doDiscovery();
        mLocalDevice.getScanMode();
        Log.d(TAG, "getBondedDevices:" +pairedDevices.size() + ":");
        for(int i =0; i < pairedDevices.size();i++){
        	Log.d(TAG,pairedDevices.iterator().next().getName());
        }
	}
    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);

        // If we're already discovering, stop it
        if (mLocalDevice.isDiscovering()) {
        	mLocalDevice.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mLocalDevice.startDiscovery();
    }
    /**
     * アクティビティへの院展との戻り値処理
     */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constants.REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				if(debug ) Log.d(TAG, "get address ");
				mBTAddress = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				if(debug ) Log.d(TAG, "get address "+mBTAddress);
//				mRemoteDevice = mLocalDevice.getRemoteDevice(mBTAddress);
				if(debug ) Log.d(TAG, "startClient ");
				Intent serviceIntent = new Intent(this, CommunicateService.class);
				serviceIntent.setAction("StartClient");
				serviceIntent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, mBTAddress);
				startService(serviceIntent);
			}
			break;
		}
	}

	/**
	 * 
	 * @param resultCode
	 * @param data
	 */
	void onRequestEnableResult(int resultCode, Intent data) {
		
		boolean enabled = (resultCode == Activity.RESULT_OK);

		// ACTION_REQUEST_ENABLE returns incorrect resultCode on Platform2.0.
		int sdkInt = Build.VERSION.SDK_INT;

		if (sdkInt > Build.VERSION_CODES.DONUT) {
			enabled = (resultCode == Activity.RESULT_CANCELED);
		}

	}

	/**
	 * 
	 * @param address
	 * @return
	 */
	BluetoothDevice obtainRemoteDevice(String address) {
		BluetoothDevice remDev = null;

		try {
			remDev = mLocalDevice.getRemoteDevice(address);
		} catch (IllegalArgumentException invalidBtAddr) {
		}
		return remDev;
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
		stopClient();
		stopServer();
        // Make sure we're not doing discovery anymore
        if (mLocalDevice != null) {
        	mLocalDevice.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }
	
    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                	Log.d(TAG,device.getName() + "\n" + device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);

            }
        }
    };
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		Log.i(TAG, "onKeyDown");

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return super.onKeyDown(keyCode, event);
		}

        Intent intent = new Intent();

        intent.setAction(Intent.ACTION_ALL_APPS);
        intent.setType("text/bt_test");
        intent.putExtra("data", keyCode);
        sendBroadcast(intent);

        Log.i("onKeyDown",String.valueOf(keyCode));
		return super.onKeyDown(keyCode, event);
	}
	void stopClient() {
		Intent serviceIntent = new Intent(this, CommunicateService.class);
		stopService(serviceIntent);
	}
	void startServer() {
		Intent serviceIntent = new Intent(this, CommunicateService.class);
		serviceIntent.setAction("StartServer");
		startService(serviceIntent);
	}

	void stopServer() {
		Intent serviceIntent = new Intent(this, CommunicateService.class);
		stopService(serviceIntent);
	}
	void showToast(String text) {
		Toast t = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		t.show();
	}
}
