package com.chylyng.hardware.Bv07Bv08.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class Bv07Bv08Request extends DeviceDataRequest {
	private Byte battery;	//電量
	private Integer humidity;
	private Float temperature;

}
