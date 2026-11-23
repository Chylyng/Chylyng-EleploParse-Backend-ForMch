package com.chylyng.hardware.BV05.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BV05Converter {
	public BV05Request convert(DeviceDataBo deviceDataBo) {

		log.info("進行BV05尿布二代Request拆解");
		String rawData = deviceDataBo.getRawData();
		String deviceMac = deviceDataBo.getDeviceMac();
		if (rawData.length() >= 10) { 
			String deviceModelName = deviceDataBo.getDeviceModelName();
			LocalDateTime time = deviceDataBo.getTime();
			
			Integer humidity = getHumidity(rawData);
			Float temperature = getTemperature(rawData);
		    Byte battery = getBattery(rawData);	//電量
			
			System.out.println("humidity:"+ humidity);
			System.out.println("temperature:"+ temperature);
			BV05Request bV05Request = new BV05Request();
			bV05Request.setGatewayMac(deviceDataBo.getGatewayMac());
			bV05Request.setDeviceMac(deviceMac);
			bV05Request.setDeviceModelName(deviceModelName);
			bV05Request.setRssi(deviceDataBo.getRssi());
			bV05Request.setTime(time);
			bV05Request.setHumidity(humidity);
			bV05Request.setTemperature(temperature);
			bV05Request.setBattery(battery);
			System.out.println("進行BV05尿布二代Request拆解:"+ bV05Request);
			return bV05Request;
		} else {
			log.error("mac:" + deviceMac + "，rowdata格式錯誤" + rawData);
			throw new RuntimeException();
		}
	}
	//電量
	public static Byte getBattery(String rawData) {
		// 提取電量的原始數據
        String batteryRaw = rawData.substring(32, 34);
        System.out.println("batteryRaw: " + batteryRaw);
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
    

	//溫度
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
       
	return temperature;
}
	//濕度
	private Integer getHumidity(String rawData) {
		    String humRaw = rawData.substring(54, 58);
	        // 將原始資料轉換為整數
	        int humInt = Integer.parseInt(humRaw, 16);
	        // 計算濕度
	        int humidity = 100 * (int) humInt / 65535;
		return humidity;
		}

}
