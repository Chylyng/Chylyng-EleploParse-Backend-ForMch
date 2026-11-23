package com.chylyng.hardware.Bv11c.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Bv11cRequest extends DeviceDataRequest {
	private Boolean sos; //SOS **
	private Byte battery;	//電量

}
