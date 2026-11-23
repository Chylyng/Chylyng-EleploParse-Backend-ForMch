package com.chylyng.hardware.Bv19uBv17i.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Bv19uBv17iRequest  extends DeviceDataRequest {
	private Byte battery;
	private Short heatRate;
	private Integer step;
	private Integer calorie;

}
