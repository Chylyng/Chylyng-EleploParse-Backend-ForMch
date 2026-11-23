package com.chylyng.hardware.Bv02i.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

@Component
public class Bv02iConverter {
	public Bv02iRequest convert(DeviceDataBo deviceDataBo) {
		Bv02iRequest bv02iRequest = new Bv02iRequest();
		bv02iRequest.setDeviceModelName(deviceDataBo.getDeviceModelName());
		bv02iRequest.setDeviceMac(deviceDataBo.getDeviceMac());
		bv02iRequest.setGatewayMac(deviceDataBo.getGatewayMac());
		bv02iRequest.setRssi(deviceDataBo.getRssi());
		bv02iRequest.setTime(deviceDataBo.getTime());
		return bv02iRequest;
	}

}
