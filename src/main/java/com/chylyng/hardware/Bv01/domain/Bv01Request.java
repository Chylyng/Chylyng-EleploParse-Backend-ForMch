package com.chylyng.hardware.Bv01.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class Bv01Request extends DeviceDataRequest {
	private Byte battery;
	private Boolean sos;
}

