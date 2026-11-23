package com.chylyng.hardware.Bv7c.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

@Component
public class Bv7cConverter {
	// 包含BV19U、BV17I
	public Bv7cRequest convert(DeviceDataBo deviceDataBo) {
		Bv7cRequest bv7cRequest = new Bv7cRequest();
		bv7cRequest.setDeviceModelName(deviceDataBo.getDeviceModelName());
		bv7cRequest.setDeviceMac(deviceDataBo.getDeviceMac());
		bv7cRequest.setGatewayMac(deviceDataBo.getGatewayMac());
		bv7cRequest.setRssi(deviceDataBo.getRssi());
		bv7cRequest.setTime(deviceDataBo.getTime());
		return bv7cRequest;
	}
}
