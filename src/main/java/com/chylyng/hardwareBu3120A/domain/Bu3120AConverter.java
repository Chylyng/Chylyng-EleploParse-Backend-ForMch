package com.chylyng.hardwareBu3120A.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Bu3120AConverter {
	public Bu3120ARequest convert(DeviceDataBo deviceDataBo) {

		log.info("進行Bu3120A拆解");
		String rawData = deviceDataBo.getRawData();
		String deviceMac = deviceDataBo.getDeviceMac();
		String deviceModelName = deviceDataBo.getDeviceModelName();
		LocalDateTime time = deviceDataBo.getTime();
        
		String temp1 = extractTemp(rawData);
        System.out.println("temp1 (16進位): " + temp1);
        // 步驟 1: 將 tid 從 16 進位轉換為 10 進位的數值
        int temp1Decimal = Integer.parseInt(temp1, 16);
        // 步驟 2: 減去 800
        int resultAfterSubtraction = temp1Decimal - 800;
        // 步驟 3: 最後除以 10
        Float temperature = (float) (resultAfterSubtraction / 10.0);
        System.out.println("溫度: " + temperature);
        
		Integer axzonHumidity = getHumidity(rawData);
		Integer humidity = getHumidityForAxzonHum(axzonHumidity);
		System.out.println("humidity:" + humidity);
		Bu3120ARequest bu3120AReques = new Bu3120ARequest();
		bu3120AReques.setGatewayMac(deviceDataBo.getGatewayMac());
		bu3120AReques.setDeviceMac(deviceMac);
		bu3120AReques.setDeviceModelName(deviceModelName);
		bu3120AReques.setTime(time);
		bu3120AReques.setTemperature(temperature);
		bu3120AReques.setHumidity(humidity);
		return bu3120AReques;
	}

	// 解析Axzon回傳數值,並定義濕度
	public int getHumidityForAxzonHum(int axzonHumidity) {
		if (axzonHumidity >= 0 && axzonHumidity < 100) {
			return 30;
		} else if (axzonHumidity >= 100 && axzonHumidity < 200) {
			return 50;
		} else if (axzonHumidity >= 200 && axzonHumidity < 300) {
			return 70;
		} else if (axzonHumidity >= 300 && axzonHumidity < 400) {
			return 80;
		} else if (axzonHumidity >= 400 && axzonHumidity < 500) {
			return 90;
		} else {
			return 99;
		}
	}
	
	private static Integer getHumidity(String rawData) {
    	// 2.提取數據（第8和12個字符）
        String HumidityHex = rawData.substring(8, 12);
        // 將 16 進位的濕度值轉換為 10 進位的整數
        int humidityDecimal = Integer.parseInt(HumidityHex, 16);

		return humidityDecimal;
	}

	private static String extractTemp(String rawData) {
        // 1. 確認是否為 403h 標籤型號
        
        // 2.提取數據（第4和8個字符）
        String sensorCodeHex = rawData.substring(4, 8);
        
        // 3. 將十六進位轉換為二進制
        String sensorCodeBinary = hexToBinary(sensorCodeHex);
        
        // 4. 取前11位
        String temp1Binary = sensorCodeBinary.substring(0, 11);
        
        // 5. 將11位二進制轉換為十六進位
        return binaryToHex(temp1Binary);
    }

    private static String hexToBinary(String hex) {
        int i = Integer.parseInt(hex, 16);
        String binary = Integer.toBinaryString(i);
        return String.format("%8s", binary).replace(' ', '0');
    }

    private static String binaryToHex(String binary) {
        int decimal = Integer.parseInt(binary, 2);
        return Integer.toHexString(decimal).toUpperCase();
    }

}
