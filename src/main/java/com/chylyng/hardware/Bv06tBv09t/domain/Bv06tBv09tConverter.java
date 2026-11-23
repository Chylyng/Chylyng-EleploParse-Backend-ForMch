package com.chylyng.hardware.Bv06tBv09t.domain;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Bv06tBv09tConverter {

	public Bv06tBv09tRequest convert(DeviceDataBo deviceDataBo) {
		String rawData = deviceDataBo.getRawData();
		String deviceMac = deviceDataBo.getDeviceMac();
		if (rawData.length() >= 46) {//原本是62改為46
			String deviceModelName = deviceDataBo.getDeviceModelName();
			LocalDateTime time = deviceDataBo.getTime();
			byte battery = getBattery(rawData);
			short heartRate = getHeartRate(rawData);
			int step = getStep(rawData);
			int calorie = getCalorie(step);
			
			float bodyTemperature = getBodyTemperature(rawData);
			Bv06tBv09tRequest bv06tBv09tRequest = new Bv06tBv09tRequest();
			bv06tBv09tRequest.setGatewayMac(deviceDataBo.getGatewayMac());
			bv06tBv09tRequest.setBattery(battery);
			bv06tBv09tRequest.setBodyTemperature(bodyTemperature);
			bv06tBv09tRequest.setCalorie(calorie);
			bv06tBv09tRequest.setDeviceMac(deviceMac);
			bv06tBv09tRequest.setDeviceModelName(deviceModelName);
			bv06tBv09tRequest.setHeartRate(heartRate);
			bv06tBv09tRequest.setRssi(deviceDataBo.getRssi());
			bv06tBv09tRequest.setTime(time);
			bv06tBv09tRequest.setStep(step);
			return bv06tBv09tRequest;
		} else {
			log.error("mac:" + deviceMac + "，rowdata長度錯誤" + rawData.length());
			throw new RuntimeException();
		}
	}



	private int getStep(String rawData) {
		String stepString = rawData.substring(28, 32);
		int step = Integer.parseInt(stepString, 16);
		return step;
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

	private short getHeartRate(String rawData) {
		String heartRateString = rawData.substring(26, 28);
		short heartRate = Short.parseShort(heartRateString, 16);
		return heartRate;
	}

	private byte getBattery(String rawData) {
		String batteryString = rawData.substring(32, 34);
		byte battery = Byte.parseByte(batteryString, 16);
		return battery;
	}

	private float getBodyTemperature(String rawData) {
		String bodyTemperatureString = rawData.substring(39, 42);
		DecimalFormat df = new DecimalFormat("#.##");
		double bodyTemperature = Integer.parseInt(bodyTemperatureString, 16) * 0.01f;
		float bodyTemperatureWithFormat = Float.parseFloat(df.format(bodyTemperature));
		return bodyTemperatureWithFormat;
	}

}
