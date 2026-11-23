package com.chylyng.hardware.BV03ht.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class BV03htRequest extends DeviceDataRequest {
	private Byte battery;	//電量
	private Integer humidity;
	private Float temperature;
}
