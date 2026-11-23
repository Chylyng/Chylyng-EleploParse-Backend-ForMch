package com.chylyng.hardware.GP18lo.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class GP18loRequest extends DeviceDataRequest{
	private Byte battery;	//電量
	private Short heartRate;	//心率
	private Integer step;	//步數計算
	private Float bodyTemperature;	//體溫
	private Boolean sos; //SOS **
	private Integer diastolic; //舒張壓(DBP)
	private Integer systolic; //收縮壓(SBP)
	private Integer bloodOxygen; //血氧
	private Integer calorie; //消耗卡路里
	private boolean wearStatus;//是否配戴
	private double latitude;//緯度
	private double longitude;//經度
	private String beaconID1;
	private float beaconID1RSSI;
}
