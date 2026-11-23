package com.chylyng.gatewayData.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.chylyng.deviceData.DeviceDataBo;

@Service
public class GatewayDataConverterKerlink473 {

	public HashMap<String, DeviceDataBo> parseToDeviceDataMap(List<GatewayDataDtoKerlink473> kerlink473DtoList) {
		HashMap<String, DeviceDataBo> deviceDataMap = new HashMap<String, DeviceDataBo>();
		if (kerlink473DtoList.size() >= 1) {

			kerlink473DtoList
					.forEach(gatewayDataKerlink473Dto -> setDeviceDataMap(deviceDataMap, gatewayDataKerlink473Dto));
		}
		return deviceDataMap;
	}

	private void setDeviceDataMap(Map<String, DeviceDataBo> deviceDataMap,
			GatewayDataDtoKerlink473 gatewayDataKerlink473Dto) {

		DeviceDataBo deviceDataBo = new DeviceDataBo();
		deviceDataBo.setTime(gatewayDataKerlink473Dto.getSystemTime()); // 資料傳過來所自帶的timestamp
		deviceDataBo.setRawData(gatewayDataKerlink473Dto.getRawData());
		deviceDataBo.setGatewayMac(gatewayDataKerlink473Dto.getGatewayMac());
		deviceDataBo.setDeviceMac(gatewayDataKerlink473Dto.getDeviceMac());
		deviceDataBo.setRssi(gatewayDataKerlink473Dto.getRssi());
		deviceDataMap.put(gatewayDataKerlink473Dto.getDeviceMac(), deviceDataBo);
	}
}
