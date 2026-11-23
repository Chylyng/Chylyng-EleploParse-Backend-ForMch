package com.chylyng.hardware.GP86lo.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class GP86loRequest extends DeviceDataRequest{
	private Byte battery;	//電量
	private Integer step;	//步數計算
	private Boolean sos; //SOS **
	private Integer calorie; //消耗卡路里
	private double latitude;//緯度
	private double longitude;//經度
	private String beaconID1;
	private float beaconID1RSSI;

}
