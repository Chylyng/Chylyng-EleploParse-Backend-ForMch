package com.chylyng.hardware.Bv09sBv17s.domain;

import java.text.DecimalFormat;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

//包含BV09S、BV17S(體溫+心率+血氧款)
@Component
@Slf4j
public class Bv09sBv17sConverter {
	public Bv09sBv17sRequest convert(DeviceDataBo deviceDataBo) {
		String rawData = deviceDataBo.getRawData();
		if (rawData.length() >= 46) {//原本是62改為46
			
			//log.info("mac:" + deviceDataBo.getDeviceMac() + "，rowdata" + rawData);
			short heartRate = getHeartRate(rawData);
			byte battery = getBattery(rawData);
			Float bodyTemperature = getBodyTemperature(rawData);
			int step = getStep(rawData);
			int calorie = getCalorie(step);
			
			String deviceModelName = deviceDataBo.getDeviceModelName();
			Bv09sBv17sRequest bv09sBv17sRequest = new Bv09sBv17sRequest();
			bv09sBv17sRequest.setBattery(battery);
			bv09sBv17sRequest.setBodyTemperature(bodyTemperature);
			bv09sBv17sRequest.setHeartRate(heartRate);
			bv09sBv17sRequest.setCalorie(calorie);
			bv09sBv17sRequest.setStep(step);
			bv09sBv17sRequest.setTime(deviceDataBo.getTime());
			bv09sBv17sRequest.setDeviceMac(deviceDataBo.getDeviceMac());
			bv09sBv17sRequest.setDeviceModelName(deviceModelName);
			bv09sBv17sRequest.setGatewayMac(deviceDataBo.getGatewayMac());
			bv09sBv17sRequest.setRssi(deviceDataBo.getRssi());
			return bv09sBv17sRequest;
		} else {
			log.error("mac:" + deviceDataBo.getDeviceMac() + "，rowdata長度錯誤" + rawData.length());
			throw new RuntimeException();

		}

	}

	// Type (3byte)
//	private String getType(String rawData) {
//		String strType = "";
//		strType = rawData.substring(0, 6);
//		return strType;
//	}
//
//	// Header (4byte)
//	private String getHeader(String rawData) {
//		String strHeader = "";
//		strHeader = rawData.substring(6, 14);
//		return strHeader;
//	}
//
	// =========================================

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

	// HeartRate (1byte)
	private short getHeartRate(String rawData) {
		String heartRateString = rawData.substring(26, 28);
		return Short.parseShort(heartRateString, 16);
	}

	// Steps (2byte)
	private int getStep(String rawData) {
		String stepString = rawData.substring(28, 32);
		return Integer.parseInt(stepString, 16);
	}

	// Battery (1byte)
	private byte getBattery(String rawData) {
		String batteryString = rawData.substring(32, 34);
		return Byte.parseByte(batteryString, 16);
	}

	// 在心臟收縮時所測得的血壓叫做「收縮壓」（systolic blood pressure ,
	// SBP），在心臟舒張時所測得的血壓叫做「舒張壓」（diastolic blood pressure , DBP）。
	// SBP (1byte)
//	private String getSystolic(String rawData) {
//		String strSBP = rawData.substring(34, 36);
//		String strSBPConverted = "" + Integer.parseInt(strSBP, 16);
//		return strSBPConverted;
//	}
//
//	// DBP (1byte)
//	private String getDiastolic(String rawData) {
//		String strDBP = rawData.substring(36, 38);
//		String strDBPConverted = "" + Integer.parseInt(strDBP, 16);
//		return strDBPConverted;
//	}

	// 體溫 (2byte)
//	private byte getBodyTemperature(String rawData) {
//		DecimalFormat df_second_decimal = new DecimalFormat("#.##");
//		String strTemperature = rawData.substring(38, 42);
//		double doubleTemperature = 0d;
//		String strTempConverted = "0";
//		doubleTemperature = Integer.parseInt(strTemperature, 16) * 0.01d;
//		strTempConverted = df_second_decimal.format(doubleTemperature);
//		if (strTempConverted.equals("195.67")) {
//			return 0;
//		}
//		return Byte.valueOf(strTempConverted);
//	}
	
	
	private float getBodyTemperature(String rawData) {
		String bodyTemperatureString = rawData.substring(38, 42);
		DecimalFormat df = new DecimalFormat("#.##");
		double bodyTemperature = Integer.parseInt(bodyTemperatureString, 16) * 0.01f;
		float bodyTemperatureWithFormat = Float.parseFloat(df.format(bodyTemperature));
		return bodyTemperatureWithFormat;
	}
	
	
	

//	// SOS (1byte)
//	private String getSOS_breath(String rawData) {
//		String strSOS = rawData.substring(44, 46);
//		String strSOSConverted = "" + Integer.parseInt(strSOS, 16);
//		return strSOSConverted;
//	}
//
//	// 血氧O2(1byte)
//	private String getO2(String rawData) {
//		String strO2 = rawData.substring(46, 48);
//		String strO2Converted = "" + Integer.parseInt(strO2, 16);
//		return strO2Converted;
//	}
//
//	// 情緒壓力(1byte)
//	private String getMotionPressure(String rawData) {
//		String strPressure = rawData.substring(48, 50);
//		String strPressureConverted = "" + Integer.parseInt(strPressure, 16);
//		return strPressureConverted;
//	}
//
//	// RRI(2byte)
//	private String getRRI(String rawData) {
//		String strRRI = rawData.substring(50, 54);
//		String strRRIConverted = "" + Integer.parseInt(strRRI, 16);
//		return strRRIConverted;
//	}
//
//	// Name (4byte)
//	private String getName(String rawData) {
//		String strNameHex = rawData.substring(54, 62);
//		String strNameConverted = hexToAscii(strNameHex);
//		return strNameConverted;
//	}
	/*
	 * calorie = (int)total_steps*per.weight*0.0006564 卡路里消耗 = 步數x體重x0.0006564
	 * 體重單位為公斤 卡路里單位為千卡 手環默認體重是75公斤。 小數點後不保留，採取去尾法。 比如卡路里為33.545千卡，保留為33千卡。
	 */

}
