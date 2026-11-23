package com.chylyng.hardware.Bv02m.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Bv02mConverter {
//	public static void main(String[] args) {
//	    Bv02mConverter converter = new Bv02mConverter();
//	    
//	    // 模擬傳入的參數
//	    DeviceDataBo bo = new DeviceDataBo();
//	    bo.setDeviceModelName("TestModel");
//	    
//	    // 執行
//	    Bv02mRequest request = converter.convert(bo);
//	    
//	    // 印出結果
//	    System.out.println("執行完成！結果為：" + request);
//	}
	public Bv02mRequest convert(DeviceDataBo deviceDataBo) {
		log.info("進行Bv02mRequest拆解");
		String rawData = deviceDataBo.getRawData();
//		String rawData = "02010614FF3906CA03030007FFF601042164220135011E45";
		Bv02mRequest bv02mRequest = new Bv02mRequest();
		bv02mRequest.setDeviceModelName(deviceDataBo.getDeviceModelName());
		bv02mRequest.setDeviceMac(deviceDataBo.getDeviceMac());
		bv02mRequest.setGatewayMac(deviceDataBo.getGatewayMac());
		bv02mRequest.setRssi(deviceDataBo.getRssi());
		bv02mRequest.setTime(deviceDataBo.getTime());
		if (rawData.substring(32, 34).equals("21")) {// 回傳格式Frame Type 為A1 才有電量
			byte battery = getBattery(rawData);
			bv02mRequest.setBattery(battery);
			Boolean demolished = parseDemolished(rawData);
			System.out.println("demolished: " + demolished);
			bv02mRequest.setDemolished(demolished);

		} else {// 其他格式回傳-1
			byte battery = -1;
			bv02mRequest.setBattery(battery);
			bv02mRequest.setDemolished(false);
		}
		log.info("bv02mRequest :" + bv02mRequest);

		return bv02mRequest;
	}

	private static byte getBattery(String rawData) {
		String batteryHexString = rawData.substring(34, 36);
		int batteryLevel = Integer.parseInt(batteryHexString, 16);
		byte batteryLevelAsByte = (byte) batteryLevel;

		return batteryLevelAsByte;
	}

	private static Boolean parseDemolished(String rawData) {
		String demolishedHexString = rawData.substring(38, 40);
		int demolishedInt = Integer.parseInt(demolishedHexString, 16);
		System.out.println("demolishedHexString: " + demolishedHexString);

		Boolean demolished = false;
		if (demolishedInt == 1) {
			demolished = true;
		}
		return demolished;
	}

}
