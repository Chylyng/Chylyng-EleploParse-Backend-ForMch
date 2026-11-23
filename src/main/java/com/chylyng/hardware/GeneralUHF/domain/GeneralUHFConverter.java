package com.chylyng.hardware.GeneralUHF.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

@Component
public class GeneralUHFConverter {

	public GeneralUHFRequest convert(DeviceDataBo deviceDataBo) {
		GeneralUHFRequest generalUHFRequest = new GeneralUHFRequest();
		generalUHFRequest.setDeviceModelName(deviceDataBo.getDeviceModelName());
		generalUHFRequest.setDeviceMac(deviceDataBo.getDeviceMac());
		generalUHFRequest.setGatewayMac(deviceDataBo.getGatewayMac());
		generalUHFRequest.setTime(deviceDataBo.getTime());
		generalUHFRequest.setAreaName(deviceDataBo.getAreaName());
		return generalUHFRequest;
	}

}
