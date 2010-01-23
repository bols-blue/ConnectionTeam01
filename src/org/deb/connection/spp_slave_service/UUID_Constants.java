package org.deb.connection.spp_slave_service;

import java.util.UUID;

abstract class UUID_Constants {

	// Protocol
	static final UUID L2CAP_PROTOCOL = uuid("00000100-0000-1000-8000-00805f9b34fb");

	// Profile
	static final UUID HUMAN_INTERFACE_DEVICE_PROFILE = uuid("00001124-0000-1000-8000-00805f9b34fb");

	static final UUID PLUG_AND_PLAY_PROFILE = uuid("00001200-0000-1000-8000-00805f9b34fb");

	static final UUID SERIAL_PORT_PROFILE = uuid("00001101-0000-1000-8000-00805f9b34fb");

	// L2CAP Channel
	static final int HID_CONTROL_CHANNEL = 0x0011;

	static final int HID_INTERRUPT_CHANNEL = 0x0013;

	// Transaction Type
	static final int TRANSACTION_HANDSHAKE = 0x0;

	static final int TRANSACTION_HID_CONTROL = 0x1;

	static final int TRANSACTION_GET_REPORT = 0x4;

	static final int TRANSACTION_SET_REPORT = 0x5;

	static final int TRANSACTION_GET_PROTOCOL = 0x6;

	static final int TRANSACTION_SET_PROTOCOL = 0x7;

	static final int TRANSACTION_GET_IDLE = 0x8;

	static final int TRANSACTION_SET_IDLE = 0x9;

	static final int TRANSACTION_DATA = 0xa;

	static final int TRANSACTION_DATC = 0xb;

	// Index
	static final int HEADER_INDEX = 0;

	static final int REPORT_ID_INDEX = 1;
	
	static final int MODIFIER_INDEX = 2;
	
	static final int KEYCODE_START_INDEX = 4;

	// Buffer size
	static final int KEYBOARD_REPORT_BUFFER_SIZE = 9;
	
	static final int HEADER_SIZE = 1;
	
	private static final UUID uuid(String reprUuid) {
		UUID uuid = UUID.fromString(reprUuid);
		return uuid;
	}
}
