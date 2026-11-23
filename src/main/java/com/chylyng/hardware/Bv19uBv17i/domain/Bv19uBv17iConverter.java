package com.chylyng.hardware.Bv19uBv17i.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;
import com.chylyng.util.HexUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Bv19uBv17iConverter {

	// 包含BV19U、BV17I
	public Bv19uBv17iRequest convert(DeviceDataBo deviceDataBo) {
		String rawData = deviceDataBo.getRawData();
		//一定要包含02開頭才是正確版本
		
		if (rawData!=null && rawData.length() >= 46) { //原本是62改為46
			String strPrefix = rawData.substring(0, 2);
			//System.out.println("strPrefix:"+strPrefix);
			
			if(strPrefix.equalsIgnoreCase("02")) {
					byte battery = getBattery(rawData);
					short heatRate = getHeartRate(rawData);
					int step = getStep(rawData);
					int calorie = getCalorie(step);

					String deviceModelName = deviceDataBo.getDeviceModelName();
					Bv19uBv17iRequest bv19uBv17iRequest = new Bv19uBv17iRequest();
					bv19uBv17iRequest.setDeviceModelName(deviceModelName);
					bv19uBv17iRequest.setDeviceMac(deviceDataBo.getDeviceMac());
					bv19uBv17iRequest.setBattery(battery);
					bv19uBv17iRequest.setCalorie(calorie);
					bv19uBv17iRequest.setGatewayMac(deviceDataBo.getGatewayMac());
					bv19uBv17iRequest.setHeatRate(heatRate);
					bv19uBv17iRequest.setStep(step);
					bv19uBv17iRequest.setRssi(deviceDataBo.getRssi());
					bv19uBv17iRequest.setTime(deviceDataBo.getTime());
					return bv19uBv17iRequest;
			}else {
					Bv19uBv17iRequest bv19uBv17iRequest = new Bv19uBv17iRequest();
					return bv19uBv17iRequest;
			}
			
		} else {
					log.error("deviceMac = " + deviceDataBo.getDeviceMac() + " rawdata長度或格式錯誤" + rawData.length());
					throw new RuntimeException();
		}
	}

	// Type (3byte)
	public String processType(String rawData) {
		String strType = "";
		strType = rawData.substring(0, 6);
		return strType;
	}

	// Header (4byte)
	public String processHeader(String rawData) {
		String strHeader = "";
		strHeader = rawData.substring(6, 14);
		return strHeader;
	}

	// Mac (6byte)
	public String processMac(String rawData) {
		String strMac = "";
		strMac = rawData.substring(14, 26);
		return strMac;
	}

	// Steps (2byte)
	public int getStep(String rawData) {
		String stepString = rawData.substring(28, 32);
		int step = Integer.parseInt(stepString, 16);
		return step;
	}

	// Battery (1byte)
	public byte getBattery(String rawData) {
		String batteryString = rawData.substring(32, 34);
		byte battery = Byte.parseByte(batteryString, 16);
		return battery;
	}

	// 在心臟收縮時所測得的血壓叫做「收縮壓」（systolic blood pressure ,
	// SBP），在心臟舒張時所測得的血壓叫做「舒張壓」（diastolic blood pressure , DBP）。

	// SBP (1byte)
	public String processSBP(String rawData) {
		String strSBP = rawData.substring(34, 36);
		String strSBPConverted = "" + Integer.parseInt(strSBP, 16);
		return strSBPConverted;
	}

	// DBP (1byte)
	public String processDBP(String rawData) {
		String strDBP = rawData.substring(36, 38);
		String strDBPConverted = "" + Integer.parseInt(strDBP, 16);
		return strDBPConverted;
	}

	//用公式計算
	private int getCalorie(int step) {
		int calorie = 0;
		try {
			float f_calorie = step * 0.6f * 60 * 1.036f / 1000;
			calorie = Math.round(f_calorie);
			//System.out.println("Calorie:"+ calorie);
		}catch(Exception e) {
			System.out.println("Parsing Calorie Error!");
		}
		
		return calorie;
	}

	// Rssi (1byte)
	public String processRssi(String rawData) {
		String brssi = rawData.substring(42, 44);
		String strRssi = "" + Integer.parseInt(brssi, 16);
		return strRssi;
	}

	// SOS (1byte)
	public String processSOS(String rawData) {
		String strSOS = rawData.substring(44, 46);
		String strSOSConverted = "" + Integer.parseInt(strSOS, 16);
		return strSOSConverted;
	}


	// =============================================

	public short getHeartRate(final String rawData) {
		final String heartRateString = rawData.substring(26, 28);
		final short heartRate = Short.parseShort(heartRateString, 16);
		return heartRate;
	}
}
