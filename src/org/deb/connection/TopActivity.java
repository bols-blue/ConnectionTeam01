package org.deb.connection;

import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TopActivity extends Activity implements OnClickListener {
	final String TAG = "DebCon";

	Button bluetoothButton;
	Button handwritingButton;
	BluetoothAdapter mLocalDevice;
	static final private int REQUEST_ENABLE_BT = 100;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		bluetoothButton = (Button) findViewById(R.id.bluetooth_button);
		handwritingButton = (Button) findViewById(R.id.handwriting_button);
		bluetoothButton.setOnClickListener(this);
		handwritingButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bluetooth_button:
			init();
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
		int a;
		a=10;
		if (this.ensureSupported()) {
			;
		}
		if (this.ensureSupported()) {
			;
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
}
