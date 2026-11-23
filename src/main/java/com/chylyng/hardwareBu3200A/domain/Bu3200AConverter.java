package com.chylyng.hardwareBu3200A.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;
import com.chylyng.hardware.BV03ht.domain.BV03htRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Bu3200AConverter {
	
	public Bu3200ARequest convert(DeviceDataBo deviceDataBo) {

		log.info("進行Bu3200A拆解");
		String deviceMac = deviceDataBo.getDeviceMac();
		String deviceModelName = deviceDataBo.getDeviceModelName();
		LocalDateTime time = deviceDataBo.getTime();
			
			
			Float temperature = deviceDataBo.getTemperature();
			Integer axzonHumidity = deviceDataBo.getHumidity();
			Integer humidity =getHumidityForAxzonHum(axzonHumidity);
			System.out.println("humidity:"+ humidity);
			Bu3200ARequest  bu3200AReques = new  Bu3200ARequest();
			bu3200AReques.setGatewayMac(deviceDataBo.getGatewayMac());
			bu3200AReques.setDeviceMac(deviceMac);
			bu3200AReques.setDeviceModelName(deviceModelName);
			bu3200AReques.setTime(time);
			bu3200AReques.setTemperature(temperature);
			bu3200AReques.setHumidity(humidity);
			return bu3200AReques;
	}
	
	//解析Axzon回傳數值,並定義濕度
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
	
	
	

}
