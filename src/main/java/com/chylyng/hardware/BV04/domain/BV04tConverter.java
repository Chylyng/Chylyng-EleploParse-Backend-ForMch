package com.chylyng.hardware.BV04.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BV04tConverter {
	public BV04Request convert(DeviceDataBo deviceDataBo) {

		log.info("進行BV04Request拆解");
		String rawData = deviceDataBo.getRawData();
		String deviceMac = deviceDataBo.getDeviceMac();
		if (rawData.length() >= 10) {
			String deviceModelName = deviceDataBo.getDeviceModelName();
			LocalDateTime time = deviceDataBo.getTime();

			Byte battery = getBattery(rawData); // 電量
			System.out.println("battery: " + battery);
			boolean sos = getSos(rawData);
			Byte rssi=deviceDataBo.getRssi();
			BV04Request bV04Request = new BV04Request();
			bV04Request.setGatewayMac(deviceDataBo.getGatewayMac());
			bV04Request.setDeviceMac(deviceMac);
			bV04Request.setDeviceModelName(deviceModelName);
			bV04Request.setRssi(rssi);
			bV04Request.setTime(time);
			bV04Request.setBattery(battery);
			bV04Request.setSos(sos);
			log.info("rawData:"+rawData+";deviceMac:"+deviceMac+",battery:"+battery+",sos:"+sos+",rssi:"+rssi);
			return bV04Request;
			
		} else {
			log.error("mac:" + deviceMac + "，rowdata格式錯誤" + rawData);
			throw new RuntimeException();
		}
	}

	// 電量
	public static Byte getBattery(String rawData) {
		// 提取電量的原始數據
		String batteryRaw = rawData.substring(32, 34);
		System.out.println("batteryRaw:" + batteryRaw);
		int batteryInt = Integer.parseInt(batteryRaw, 16);
		if (batteryInt >= 30) {
			return 100;
		} else if (batteryInt >= 29) {
			return 95;
		} else if (batteryInt >= 28.5) {
			return 90;
		} else if (batteryInt >= 28) {
			return 85;
		} else if (batteryInt >= 27.5) {
			return 80;
		} else if (batteryInt >= 27) {
			return 75;
		} else if (batteryInt >= 26.5) {
			return 70;
		} else if (batteryInt >= 26) {
			return 65;
		} else if (batteryInt >= 25.5) {
			return 60;
		} else if (batteryInt >= 25) {
			return 55;
		} else if (batteryInt >= 24.5) {
			return 50;
		} else if (batteryInt >= 24) {
			return 45;
		} else if (batteryInt >= 23.5) {
			return 40;
		} else if (batteryInt >= 23) {
			return 35;
		} else if (batteryInt >= 22.5) {
			return 30;
		} else if (batteryInt >= 22) {
			return 25;
		} else if (batteryInt >= 21.5) {
			return 20;
		} else if (batteryInt >= 21) {
			return 15;
		} else if (batteryInt >= 20.5) {
			return 10;
		} else if (batteryInt >= 20) {
			return 5;
		} else {
			return null; // 返回null表示无效的电量水平
		}

	}
	
	// SOS
		private boolean getSos(String rawData) {
			String hexFallOffString = rawData.substring(30, 32);
			Integer sosInt = Integer.parseInt(hexFallOffString, 16);
			System.out.println("getBv04Sos:"+sosInt);
			boolean Sos = sosInt.equals(1);//1為發送sos
			System.out.println("getBv04Sos:"+Sos);
			return Sos;
		}
}
