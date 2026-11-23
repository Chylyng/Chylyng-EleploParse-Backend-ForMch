package com.chylyng.hardware.Bv06tBv09t.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class Bv06tBv09tRequest extends DeviceDataRequest {
	private Byte battery;
	private Short heartRate;
	private Integer step;
	private Float bodyTemperature;
	private Integer calorie;
}

