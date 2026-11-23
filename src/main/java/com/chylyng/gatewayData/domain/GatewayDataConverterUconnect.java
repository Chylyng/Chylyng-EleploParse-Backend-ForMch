package com.chylyng.gatewayData.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.chylyng.deviceData.DeviceDataBo;

@Service
public class GatewayDataConverterUconnect {

	public 	HashMap<String, DeviceDataBo>  parseToDeviceDataMap(List<GatewayDataDtoUconnect> uconnectDtoList) {
		HashMap<String, DeviceDataBo> deviceDataMap = new HashMap<String, DeviceDataBo>();
		if (uconnectDtoList.size() >= 1) {

			uconnectDtoList.forEach(gatewayDataUconnectDto -> setDeviceDataMap(deviceDataMap, gatewayDataUconnectDto));
		}
		return deviceDataMap;
	}

	private void setDeviceDataMap(Map<String, DeviceDataBo> deviceDataMap, GatewayDataDtoUconnect gatewayDataUconnectDto) {

		DeviceDataBo deviceDataBo = new DeviceDataBo();
		deviceDataBo.setTime(gatewayDataUconnectDto.getSystemTime());  //資料傳過來所自帶的timestamp
		deviceDataBo.setRssi(gatewayDataUconnectDto.getRssi());
		deviceDataBo.setRawData(gatewayDataUconnectDto.getRawData());
		deviceDataBo.setGatewayMac(gatewayDataUconnectDto.getGatewayMac());
		deviceDataBo.setDeviceMac(gatewayDataUconnectDto.getDeviceMac());
		deviceDataMap.put(gatewayDataUconnectDto.getDeviceMac(), deviceDataBo);
	}
}
