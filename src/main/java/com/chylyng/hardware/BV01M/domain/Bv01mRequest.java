package com.chylyng.hardware.BV01M.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Bv01mRequest extends DeviceDataRequest{
	private Byte battery;	//電量
	private Boolean demolished ;//拆除

}
