package com.chylyng.hardware.Bv01.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Bv01Converter { //華成資通

	public Bv01Request convert(DeviceDataBo deviceDataBo) {
		
		
		
		String deviceModelName = deviceDataBo.getDeviceModelName();
		LocalDateTime time = deviceDataBo.getTime();
		String deviceMac = deviceDataBo.getDeviceMac();
		String rawData = deviceDataBo.getRawData();
		
		if (rawData.contains(",")) {
			
			String delimiter_forEachInfo = ","; 
			String[] result_forEachInfo = rawData.split(delimiter_forEachInfo); 
			
			byte battery = getBattery(result_forEachInfo);
			boolean sos= getSos(result_forEachInfo);

			
			Bv01Request bv01Request = new Bv01Request();
			bv01Request.setGatewayMac(deviceDataBo.getGatewayMac());
			bv01Request.setDeviceMac(deviceMac);
			bv01Request.setDeviceModelName(deviceModelName);
			bv01Request.setRssi(deviceDataBo.getRssi());
			bv01Request.setTime(time);
			
			bv01Request.setBattery(battery);
			bv01Request.setSos(sos);
			return bv01Request;
		} else {
			log.error("mac:" + deviceMac + "，rowdata格式錯誤" + rawData.length());
			throw new RuntimeException();
		}
	}
	
	


	//電量
	private byte getBattery(String[] result_forEachInfo) {

		String StrCounter=result_forEachInfo[2];
		String StrTagBattery=result_forEachInfo[5];
		int intCounter = Integer.parseInt(StrCounter);
		
		
		//-----加工處理Battery----
		int intBattery = Integer.parseInt(StrTagBattery);
		System.out.println("Battery(Org Value) = " + intBattery);
		if (intBattery < intCounter) {
			intBattery = intBattery + 256;
		}	
		float volt = (float)((intBattery - intCounter)/10.0);
		String strAfterConvert = Float.toString(volt);
		System.out.println("Battery(After convert) = " + strAfterConvert +"V");
		//-----加工處理Battery----
		
		return 100;
	}
	

	
	/**
	 * SOS<br>
	 * 經測試轉換結果與手環數值相同。<br>
	 * Long press the physical key for 3 seconds<br>
	 * SOS = 1 and lasts for 60 seconds, usually 0
	 * 
	 * @param rawData
	 * @return
	 */
	private boolean getSos(String[] result_forEachInfo) {
		boolean boolSOS =false;
		
		String StrCounter=result_forEachInfo[2];
		String StrTagButtonStatus=result_forEachInfo[6];
		int intCounter = Integer.parseInt(StrCounter);
		
		
		//-----加工處理按鍵----
		int intButton = Integer.parseInt(StrTagButtonStatus);
		System.out.println("Button(Org Value)="+intButton);
		int inButtonAfterConvert = intButton - intCounter;
		System.out.println("Button(After convert) = "+inButtonAfterConvert);
		if(inButtonAfterConvert==1){
			boolSOS=true;
			System.out.println("button = ON");
		}else{
			boolSOS=false;
			System.out.println("button = OFF");
		}
		//-----加工處理按鍵----
		
		return boolSOS;
	}



}
