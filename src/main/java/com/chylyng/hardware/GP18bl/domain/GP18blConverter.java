package com.chylyng.hardware.GP18bl.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GP18blConverter {
	public GP18blRequest convert(DeviceDataBo deviceDataBo) {
		log.info("進行GP18blRequest拆解");
		String rawData = deviceDataBo.getRawData();
		
		GP18blRequest gP18blRequest = new GP18blRequest();
		gP18blRequest.setDeviceModelName(deviceDataBo.getDeviceModelName());
		gP18blRequest.setDeviceMac(deviceDataBo.getDeviceMac());
		gP18blRequest.setGatewayMac(deviceDataBo.getGatewayMac());
		gP18blRequest.setRssi(deviceDataBo.getRssi());
		gP18blRequest.setTime(deviceDataBo.getTime());
		
		log.info("gP18blRequest :"+gP18blRequest );
	
		return gP18blRequest;
	}
}
