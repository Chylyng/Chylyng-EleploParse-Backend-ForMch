package com.chylyng.hardware.Bv09.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Bv09Converter {
	public Bv09Request convert(DeviceDataBo deviceDataBo) {

		String rawData = deviceDataBo.getRawData();
		String deviceMac = deviceDataBo.getDeviceMac();
		log.info("進行BV09 Request拆解"+rawData);
		// 至少需要 60 bytes 以上
		if (rawData.length() < 60) {
			log.error("BV09 rawData 長度不足: length=" + rawData.length());
			return null;
		}

		int page = getByte(rawData, 8); // Page = 0x01 / 0x02 / 0x03
		int status = getByte(rawData, 9); // 00 正常、01 風扇故障

		Bv09Request dto = new Bv09Request();
		dto.setGatewayMac(deviceDataBo.getGatewayMac());
		dto.setDeviceMac(deviceMac);
		dto.setDeviceModelName(deviceDataBo.getDeviceModelName());
		dto.setRssi(deviceDataBo.getRssi());
		dto.setTime(deviceDataBo.getTime());
		dto.setStatus(status);
		dto.setPage(page);

		// 依 Page 解析
		switch (page) {
		case 0x01:
			dto.setPm1(getUint16Div10(rawData, 25));
			dto.setPm25(getUint16Div10(rawData, 27));
			dto.setPm4(getUint16Div10(rawData, 29));
			break;

		case 0x02:
			dto.setPm10(getUint16Div10(rawData, 25));
			dto.setTemperature(getTemperature(rawData, 27)); // SHT30
			dto.setHumidity(getHumidity(rawData, 29)); // SHT30
			break;

		case 0x03:
			dto.setVoc(getUint16Div10(rawData, 25));
			dto.setCo2(getUint16(rawData, 27));
			dto.setHcho(getUint16Div10(rawData, 29));
			break;

		default:
			log.warn("未知 Page: " + page);
		}
		log.info("BV09 send to eleplo:" + dto );
		return dto;
	}

	// ------------------------ Byte Parsing Tools ------------------------

	// 取得 1 byte
	private int getByte(String raw, int byteIndex) {
		int start = byteIndex * 2;
		return Integer.parseInt(raw.substring(start, start + 2), 16);
	}

	// 取得 uint16（2 bytes, big endian）
	private int getUint16(String raw, int byteIndex) {
		int start = byteIndex * 2;
		String hex = raw.substring(start, start + 4);
		return Integer.parseInt(hex, 16);
	}

	// 取得 uint16 並除以 10
	private float getUint16Div10(String raw, int byteIndex) {
		return getUint16(raw, byteIndex) / 10.0f;
	}

	// 實際溫度 (°C) /200
	private float getTemperature(String raw, int byteIndex) {
		int value = getUint16(raw, byteIndex);
		return (float) (value / 200);
	}

	// 實際濕度 (%) /100
	private Integer getHumidity(String raw, int byteIndex) {
		int value = getUint16(raw, byteIndex);
		return (Integer) (value / 100);
	}

}
