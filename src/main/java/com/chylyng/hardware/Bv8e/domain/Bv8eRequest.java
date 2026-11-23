package com.chylyng.hardware.Bv8e.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Bv8eRequest extends DeviceDataRequest {
	private Byte battery;	//電量
}
