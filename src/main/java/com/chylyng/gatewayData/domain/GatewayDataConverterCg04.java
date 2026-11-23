package com.chylyng.gatewayData.domain;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

@Component
public class GatewayDataConverterCg04 {

	public HashMap<String, DeviceDataBo> parseToDeviceDataMap(String allDeviceDataString) {
		String[] allDeviceDataStringArray = allDeviceDataString.replace("[", "").replace("]", "").split("&");
		HashMap<String, DeviceDataBo> deviceDataMap = getDeviceDataMap(allDeviceDataStringArray);
		return deviceDataMap;
	}

	private static HashMap<String, DeviceDataBo> getDeviceDataMap(String[] allDeviceDataStringArray) {
		LocalDateTime time = LocalDateTime.now().withNano(0);
		HashMap<String, DeviceDataBo> deviceDataMap = new HashMap<String, DeviceDataBo>();
		if (allDeviceDataStringArray.length > 0) {
			for (String deviceDataString : allDeviceDataStringArray) {
				if (!deviceDataString.isBlank()) {
					DeviceDataBo deviceDataBo = getDeviceDataBo(deviceDataString, time);
					deviceDataMap.put(deviceDataBo.getDeviceMac(), deviceDataBo);
				}
			}
		}
		if (deviceDataMap.isEmpty()) {
			throw new RuntimeException("錯誤");
		} else {
			return deviceDataMap;
		}
	}

	// devicedata
	public static DeviceDataBo getDeviceDataBo(final String deviceDataString, LocalDateTime time) {
		DeviceDataBo deviceDataBo = new DeviceDataBo();
		String[] oneDeviceDataStringArray = deviceDataString.split(",");
		if (oneDeviceDataStringArray.length == 6) {
			deviceDataBo.setTime(time);
			deviceDataBo.setGatewayMac(parseGatewayMac(oneDeviceDataStringArray));
			deviceDataBo.setDeviceMac(parseDeviceMac(oneDeviceDataStringArray));
			deviceDataBo.setRssi(parseRssi(oneDeviceDataStringArray));
			deviceDataBo.setRawData(parseRawData(oneDeviceDataStringArray));
			return deviceDataBo;
		} else {
			throw new RuntimeException("oneDeviceDataStringArray.length != 6資料長度錯誤");
		}

	}

	private static String parseGatewayMac(String[] oneDeviceDataStringArray) {
		String gatewayMac = oneDeviceDataStringArray[0];
		// 有時候會因為換行的關係導致gatewayMac不正確，故加此判斷
		if (gatewayMac.length() == 14) {
			gatewayMac = gatewayMac.substring(2, 14);
		}
		return gatewayMac;
	}

	private static String parseDeviceMac(String[] oneDeviceDataStringArray) {
		String deviceMac = oneDeviceDataStringArray[4];
		return deviceMac;
	}

	private static Byte parseRssi(String[] oneDeviceDataStringArray) {
		String strRssi = oneDeviceDataStringArray[1];
		Byte rssi = (byte) -Byte.parseByte(strRssi, 16);
		return rssi;
	}

	private static String parseRawData(String[] oneDeviceDataStringArray) {
		String rawData = oneDeviceDataStringArray[5];
		return rawData;
	}
}
