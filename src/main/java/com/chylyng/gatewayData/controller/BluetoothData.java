package com.chylyng.gatewayData.controller;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BluetoothData {
	private String bluetoothMac;
	private String bluetoothName;
	private Byte rssi;
	private String rawData;
}
