package com.chylyng.hardware.Bv11c.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;
import com.chylyng.hardware.Bv30xBv30s.domain.Bv30xBV30sConverter;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Bv11cConverter {
	public Bv11cRequest convert(DeviceDataBo deviceDataBo) {
		String rawData = deviceDataBo.getRawData();
		Bv11cRequest bv11cRequest = new Bv11cRequest();
		bv11cRequest.setDeviceModelName(deviceDataBo.getDeviceModelName());
		bv11cRequest.setDeviceMac(deviceDataBo.getDeviceMac());
		bv11cRequest.setGatewayMac(deviceDataBo.getGatewayMac());
		bv11cRequest.setRssi(deviceDataBo.getRssi());
		bv11cRequest.setTime(deviceDataBo.getTime());
		bv11cRequest.setSos(false);
		if(rawData.substring(22, 24).equals("a1")) {
			byte batteryLevel = parseBatteryLevel(rawData);
            System.out.println("Battery level: " + batteryLevel);
            bv11cRequest.setBattery(batteryLevel);
		}
		
		 if(rawData.substring(14, 16).equals("02")) {
	        	boolean sos = rawData.substring(53, 54).equals("e");
	            System.out.println("sos: " + sos);
	            bv11cRequest.setSos(sos);
			}
	    System.out.println("bv11cRequest: " + bv11cRequest);
	    log.info("進行Bv11c拆解"+ bv11cRequest);
		return bv11cRequest;
	}
	
	//電量
    private static byte parseBatteryLevel(String deviceRawData) {
        String batteryLevelHexString = deviceRawData.substring(26, 28);
        byte batteryLevel = Byte.parseByte(batteryLevelHexString, 16);
        return batteryLevel;
    }
	
	
	
}
