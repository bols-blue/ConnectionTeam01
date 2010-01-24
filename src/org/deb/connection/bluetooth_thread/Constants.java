package org.deb.connection.bluetooth_thread;

public interface Constants {
	static final int MESSAGE_FAIL = 12;
	static final int MESSAGE_STOP = 7;
	static final int REQUEST_CONNECT_DEVICE = 15;
	static final int MESSAGE_CONNECTION_FAIL = 5;
	static final int MESSAGE_CONNECTION_LOST = -1;
	static final int MESSAGE_WRITE = 2;
	static final int MESSAGE_READ = 1;
	static final int MESSAGE_CONNECTED = 0;
	static final int MESSAGE_CONTROL_CONNECTED = 10;
	static final int MESSAGE_CONTROL_READ = 11;
	static final int MESSAGE_SYS_OUT = 3;
	static final int MESSAGE_TOAST = 9;
	static final int MESSAGE_START = 6;
	static final String SERVICE = "DevCon";
	static final int RESULT_NO_START =100;
}
