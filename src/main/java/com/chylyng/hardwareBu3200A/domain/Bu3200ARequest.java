package com.chylyng.hardwareBu3200A.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class Bu3200ARequest extends DeviceDataRequest {
	private Float temperature;
	private Integer humidity;

}
