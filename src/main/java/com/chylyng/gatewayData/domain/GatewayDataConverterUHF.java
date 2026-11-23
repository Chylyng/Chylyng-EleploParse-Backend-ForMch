package com.chylyng.gatewayData.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.chylyng.deviceData.DeviceDataBo;

@Service
public class GatewayDataConverterUHF {
	
	public 	HashMap<String, DeviceDataBo>  parseToDeviceDataMap(ArrayList<GatewayDataDtoUHF> UHFDtoList) {
		HashMap<String, DeviceDataBo> deviceDataMap = new HashMap<String, DeviceDataBo>();
		if (UHFDtoList.size() >= 1) {

			UHFDtoList.forEach(gatewayDataUHFDto -> setDeviceDataMap(deviceDataMap, gatewayDataUHFDto));
		}
		return deviceDataMap;
	}

	private void setDeviceDataMap(Map<String, DeviceDataBo> deviceDataMap, GatewayDataDtoUHF gatewayDataUHFDto) {

		DeviceDataBo deviceDataBo = new DeviceDataBo();
		deviceDataBo.setTime(gatewayDataUHFDto.getSystemTime());  //資料傳過來所自帶的timestamp
		deviceDataBo.setGatewayMac(gatewayDataUHFDto.getGatewayMac());
		deviceDataBo.setDeviceMac(gatewayDataUHFDto.getDeviceMac());
		deviceDataBo.setTemperature(gatewayDataUHFDto.getTemperature());
		deviceDataBo.setHumidity(gatewayDataUHFDto.getHumidity());
		deviceDataBo.setAreaName(gatewayDataUHFDto.getAreaName());
		deviceDataMap.put(gatewayDataUHFDto.getDeviceMac(), deviceDataBo);
	}
}
