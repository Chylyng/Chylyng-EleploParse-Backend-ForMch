package com.chylyng.gatewayData.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.chylyng.deviceData.DeviceDataBo;

@Service
public class GatewayDataConverterCg05 {

	public HashMap<String, DeviceDataBo> parseToDeviceDataMap(List<GatewayDataDtoCg05> cg05DtoList) {
		HashMap<String, DeviceDataBo> deviceDataMap = new HashMap<String, DeviceDataBo>();
		if (cg05DtoList.size() >= 1) {
			cg05DtoList.forEach(gatewayDataCg05Dto -> setDeviceDataMap(deviceDataMap, gatewayDataCg05Dto));
		}
		return deviceDataMap;
	}

	private void setDeviceDataMap(Map<String, DeviceDataBo> deviceDataMap, GatewayDataDtoCg05 gatewayDataCg05Dto) {
		DeviceDataBo deviceDataBo = new DeviceDataBo();
		deviceDataBo.setTime(gatewayDataCg05Dto.getSystemTime()); // 資料傳過來所自帶的timestamp
		deviceDataBo.setRssi(gatewayDataCg05Dto.getRssi());
		deviceDataBo.setRawData(gatewayDataCg05Dto.getRawData());
		deviceDataBo.setGatewayMac(gatewayDataCg05Dto.getGatewayMac());
		deviceDataBo.setDeviceMac(gatewayDataCg05Dto.getDeviceMac());
		deviceDataBo.setCompanyId(gatewayDataCg05Dto.getCompanyId()); // 這是新增加了，為了讓Eleplo Receiver Cloud能識別是哪一個公司
		deviceDataMap.put(gatewayDataCg05Dto.getDeviceMac(), deviceDataBo);
	}
}
