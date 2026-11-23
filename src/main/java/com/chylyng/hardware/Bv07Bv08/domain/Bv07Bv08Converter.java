package com.chylyng.hardware.Bv07Bv08.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Bv07Bv08Converter {

	public Bv07Bv08Request convert(DeviceDataBo deviceDataBo) {

		log.info("Bv07Bv08Request");
		String rawData = deviceDataBo.getRawData();
		String deviceMac = deviceDataBo.getDeviceMac();
		if (rawData.length() >= 10) {
			String deviceModelName = deviceDataBo.getDeviceModelName();
			LocalDateTime time = deviceDataBo.getTime();

			Integer humidity = getHumidity(rawData);
			Float temperature = getTemperature(rawData);
			Byte battery = getBattery(rawData); // 電量

			System.out.println("humidity:" + humidity);
			System.out.println("temperature:" + temperature);
			Bv07Bv08Request bv07Bv08Request = new Bv07Bv08Request();
			bv07Bv08Request.setGatewayMac(deviceDataBo.getGatewayMac());
			bv07Bv08Request.setDeviceMac(deviceMac);
			bv07Bv08Request.setDeviceModelName(deviceModelName);
			bv07Bv08Request.setRssi(deviceDataBo.getRssi());
			bv07Bv08Request.setTime(time);
			bv07Bv08Request.setHumidity(humidity);
			bv07Bv08Request.setTemperature(temperature);
			bv07Bv08Request.setBattery(battery);
			System.out.println("Bv07Bv08Request:" + bv07Bv08Request);
			return bv07Bv08Request;
		} else {
			log.error("mac:" + deviceMac + "，rowdata格式錯誤" + rawData);
			throw new RuntimeException();
		}
	}

	// 電量
	public static Byte getBattery(String rawData) {
		// 提取電量的原始數據
		String batteryRaw = rawData.substring(32, 34);
		System.out.println("batteryRaw: " + batteryRaw);
		int batteryInt = Integer.parseInt(batteryRaw, 16);
//		if (batteryInt >= 30) {
//			return 100;
//		} else if (batteryInt >= 29) {
//			return 95;
//		} else if (batteryInt >= 28.5) {
//			return 90;
//		} else if (batteryInt >= 28) {
//			return 85;
//		} else if (batteryInt >= 27.5) {
//			return 80;
//		} else if (batteryInt >= 27) {
//			return 75;
//		} else if (batteryInt >= 26.5) {
//			return 70;
//		} else if (batteryInt >= 26) {
//			return 65;
//		} else if (batteryInt >= 25.5) {
//			return 60;
//		} else if (batteryInt >= 25) {
//			return 55;
//		} else if (batteryInt >= 24.5) {
//			return 50;
//		} else if (batteryInt >= 24) {
//			return 45;
//		} else if (batteryInt >= 23.5) {
//			return 40;
//		} else if (batteryInt >= 23) {
//			return 35;
//		} else if (batteryInt >= 22.5) {
//			return 30;
//		} else if (batteryInt >= 22) {
//			return 25;
//		} else if (batteryInt >= 21.5) {
//			return 20;
//		} else if (batteryInt >= 21) {
//			return 15;
//		} else if (batteryInt >= 20.5) {
//			return 10;
//		} else if (batteryInt >= 20) {
//			return 5;
//		} else {
//			return null; // 返回null表示无效的电量水平
//		}
		if (batteryInt >= 42) {
		    return 100;
		} else if (batteryInt >= 41) {
		    return 95;
		} else if (batteryInt >= 40.5) {
		    return 90;
		} else if (batteryInt >= 40) {
		    return 85;
		} else if (batteryInt >= 39.5) {
		    return 80;
		} else if (batteryInt >= 39) {
		    return 75;
		} else if (batteryInt >= 38.5) {
		    return 70;
		} else if (batteryInt >= 38) {
		    return 65;
		} else if (batteryInt >= 37.5) {
		    return 60;
		} else if (batteryInt >= 37) {
		    return 55;
		} else if (batteryInt >= 36.5) {
		    return 50;
		} else if (batteryInt >= 36) {
		    return 45;
		} else if (batteryInt >= 35.5) {
		    return 40;
		} else if (batteryInt >= 35) {
		    return 35;
		} else if (batteryInt >= 34.5) {
		    return 30;
		} else if (batteryInt >= 34) {
		    return 25;
		} else if (batteryInt >= 33.5) {
		    return 20;
		} else if (batteryInt >= 33) {
		    return 15;
		} else if (batteryInt >= 32.5) {
		    return 10;
		} else if (batteryInt >= 32) {
		    return 5;
		} else {
		    return null;
		}

	}

	// 溫度
	private float getTemperature(String rawData) {
		// 提取溫度和濕度的原始數據
		String tempRaw = rawData.substring(50, 54);
		// 將原始資料轉換為整數
		int tempInt = Integer.parseInt(tempRaw, 16);
		// 計算溫度
		float temperature = -45 + (175 * (float) tempInt / 65535.0f);

		if (Float.compare(temperature, -45.0f) == 0) {
			return 0.0f;
		}
		// 直接回傳溫度
		return temperature;
	}

	// 濕度
	private Integer getHumidity(String rawData) {
		String humRaw = rawData.substring(54, 58);
		// 將原始資料轉換為整數
		int humInt = Integer.parseInt(humRaw, 16);
		// 計算濕度
		float humidity = -6.0f + (125.0f * humInt / 65535.0f);
		// 轉成 int（四捨五入）
		int humidityInt = Math.round(humidity);

		return humidityInt;
	}

}
