package com.chylyng.gatewayData.domain;

import java.util.List;

import com.chylyng.gatewayData.controller.BluetoothData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GatewayDataDtoCg05Json { //這是傳入的結構
	private String bluetoothMac;
	private String firmwareVersion;
	private int companyId;  //這是新增加了，為了讓Eleplo Receiver Cloud能識別是哪一個公司
	private List<BluetoothData> bluetoothDataList;
}
