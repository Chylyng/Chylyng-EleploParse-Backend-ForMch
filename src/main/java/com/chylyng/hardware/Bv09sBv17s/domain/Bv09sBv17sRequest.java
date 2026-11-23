package com.chylyng.hardware.Bv09sBv17s.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Bv09sBv17sRequest extends DeviceDataRequest {
	private Short heartRate;
	private Integer step;
	private Byte battery;
	private Float bodyTemperature;
	private Integer calorie;
}
