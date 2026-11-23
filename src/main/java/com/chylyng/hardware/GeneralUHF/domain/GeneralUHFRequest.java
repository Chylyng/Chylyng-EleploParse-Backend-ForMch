package com.chylyng.hardware.GeneralUHF.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString(callSuper = true)
public class GeneralUHFRequest extends DeviceDataRequest{
	
	private String areaName;
}
