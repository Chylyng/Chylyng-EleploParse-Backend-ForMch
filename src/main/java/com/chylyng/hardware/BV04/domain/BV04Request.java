package com.chylyng.hardware.BV04.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class BV04Request extends DeviceDataRequest {
	private Byte battery;	//電量
	private Boolean sos; //SOS *
}
