package com.chylyng.hardware.BV01M.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class Bv01mConverter {
	public Bv01mRequest convert(DeviceDataBo deviceDataBo) {
		log.info("進行Bv01mRequest拆解");
		String rawData = deviceDataBo.getRawData();
		
		Bv01mRequest bv01mRequest = new Bv01mRequest();
		bv01mRequest.setDeviceModelName(deviceDataBo.getDeviceModelName());
		bv01mRequest.setDeviceMac(deviceDataBo.getDeviceMac());
		bv01mRequest.setGatewayMac(deviceDataBo.getGatewayMac());
		bv01mRequest.setRssi(deviceDataBo.getRssi());
		bv01mRequest.setTime(deviceDataBo.getTime());
		if(rawData.substring(22, 24).equals("a1")) {//回傳格式Frame Type 為A1 才有電量
			byte battery = getBattery(rawData);
			bv01mRequest .setBattery(battery);
			Boolean demolished =parseDemolished(rawData);
            System.out.println("demolished: " + demolished);
            bv01mRequest.setDemolished(demolished);
			
		}else {//其他格式回傳-1 
			byte battery =-1;
			bv01mRequest .setBattery(battery);
			bv01mRequest.setDemolished(false);
		}
		log.info("bv01mRequest :"+bv01mRequest );
	
		return bv01mRequest;
	}
	
	private static byte getBattery(String rawData) {
        String batteryHexString = rawData.substring(26, 28);
        int batteryLevel = Integer.parseInt(batteryHexString, 16);
        byte batteryLevelAsByte = (byte) batteryLevel;
        
        return batteryLevelAsByte;
    }
	
	private static Boolean parseDemolished(String rawData) {
    	String demolishedHexString = rawData.substring(28, 30);
    	int demolishedInt = Integer.parseInt(demolishedHexString, 16);
    	System.out.println("demolishedHexString: " + demolishedHexString);
    	
    	Boolean demolished =false;
    	if(demolishedInt==1) {
    		demolished =true;
    	}
		return demolished;
	}


}
