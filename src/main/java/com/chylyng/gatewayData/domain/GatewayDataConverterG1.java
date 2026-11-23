package com.chylyng.gatewayData.domain;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.chylyng.deviceData.DeviceDataBo;

@Service
public class GatewayDataConverterG1 {

	public 	HashMap<String, DeviceDataBo>  parseToDeviceDataMap(List<GatewayDataDtoG1> g1DtoList) {
		HashMap<String, DeviceDataBo> deviceDataMap = new HashMap<String, DeviceDataBo>();
		if (g1DtoList.size() > 1) {
			String gatewayMac = g1DtoList.get(0).getMac();  //第一筆就是Gateway的Mac
			g1DtoList.forEach(gatewayDataG1Dto -> setDeviceDataMap(deviceDataMap, gatewayMac, gatewayDataG1Dto));
		}
		return deviceDataMap;
	}

	private void setDeviceDataMap(Map<String, DeviceDataBo> deviceDataMap, String gatewayMac,
			GatewayDataDtoG1 gatewayDataG1Dto) {
		if (!gatewayDataG1Dto.getType().equals("Gateway")) {  //Type不是Gateway就是其他的藍牙裝置
			
			String deviceMac = gatewayDataG1Dto.getMac();
			DeviceDataBo deviceDataBo = new DeviceDataBo();
			deviceDataBo.setTime(LocalDateTime.now());  //傳過來的時間會有時間差，以目前的時間為主
			deviceDataBo.setRssi(gatewayDataG1Dto.getRssi());
			deviceDataBo.setRawData(gatewayDataG1Dto.getRawData());
			deviceDataBo.setGatewayMac(gatewayMac);
			deviceDataBo.setDeviceMac(deviceMac);
			deviceDataMap.put(deviceMac, deviceDataBo);
		}
	}
}
