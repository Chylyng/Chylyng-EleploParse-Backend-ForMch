package com.chylyng.hardware.Bv8e.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;
import com.chylyng.hardware.Bv30xBv30s.domain.Bv30xBV30sConverter;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class Bv8eConverter {
	public Bv8eRequest convert(DeviceDataBo deviceDataBo) {
		log.info("進行bv8eRequest拆解");
		String rawData = deviceDataBo.getRawData();
		
		Bv8eRequest bv8eRequest = new Bv8eRequest();
		bv8eRequest.setDeviceModelName(deviceDataBo.getDeviceModelName());
		bv8eRequest.setDeviceMac(deviceDataBo.getDeviceMac());
		bv8eRequest.setGatewayMac(deviceDataBo.getGatewayMac());
		bv8eRequest.setRssi(deviceDataBo.getRssi());
		bv8eRequest.setTime(deviceDataBo.getTime());
		if(rawData.substring(22, 24).equals("a1")) {//回傳格式Frame Type 為A1 才有電量
			byte battery = getBattery(rawData);
			bv8eRequest.setBattery(battery);
		}else {//其他格式回傳-1 
			byte battery =-1;
			bv8eRequest.setBattery(battery);
		}
		log.info("bv8eRequest:"+bv8eRequest);
		return bv8eRequest;
	}
	
	private static byte getBattery(String rawData) {
        String batteryHexString = rawData.substring(26, 28);
        int batteryLevel = Integer.parseInt(batteryHexString, 16);
        byte batteryLevelAsByte = (byte) batteryLevel;
        
        return batteryLevelAsByte;
    }
	

}
