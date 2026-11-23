package com.chylyng.hardware.SSW.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SswConverter {

	public SswRequset convert(DeviceDataBo deviceDataBo) {

		log.info("進行三星SswRequset拆解");
		String rawData = deviceDataBo.getRawData();
		String deviceMac = deviceDataBo.getDeviceMac();
		if (rawData.length() >= 54) { // 原本是62改為54
			String deviceModelName = deviceDataBo.getDeviceModelName();
			LocalDateTime time = deviceDataBo.getTime();

			byte battery = getBattery(rawData);
			float bodyTemperature = getBodyTemperature(rawData);
			short heartRate = getHeartRate(rawData);
			Integer step = getStep(rawData);
			boolean sos = getSos(rawData);
			Integer bloodOxygen = getBloodOxygen(rawData);
			Integer calorie = getCalorie(step);
			Integer systolic = getSystolic(rawData); // 收縮壓(SBP)
			Integer diastolic = getDiastolic(rawData); // 舒張壓(DBP)
			byte state =getState(rawData);//00為未配戴 01為已配戴
			
			Integer stressIndex = getStressIndex(rawData);
			Integer hrv = getHrv(rawData);
			Float eda = getEda(rawData);

			log.info("rawData:" + rawData + ";deviceMac:" + deviceMac + ",battery:" + battery + ",heartRate:"
					+ heartRate + ",step:" + step + ",bodyTemperature:" + bodyTemperature + ",sos:" + sos+",state:"+state);

			SswRequset sswRequset = new SswRequset();
			sswRequset.setGatewayMac(deviceDataBo.getGatewayMac());
			sswRequset.setRssi(deviceDataBo.getRssi());
			sswRequset.setDeviceMac(deviceMac);
			sswRequset.setDeviceModelName(deviceModelName);
			sswRequset.setBattery(battery);
			sswRequset.setBodyTemperature(bodyTemperature);
			sswRequset.setHeartRate(heartRate);
			sswRequset.setTime(time);
			sswRequset.setStep(step);
			sswRequset.setSos(sos);
			sswRequset.setBloodOxygen(bloodOxygen);
			sswRequset.setCalorie(calorie);
			sswRequset.setSystolic(systolic); // 收縮壓(SBP)
			sswRequset.setDiastolic(diastolic); // 舒張壓(DBP)
			sswRequset.setState(state);
			sswRequset.setStressIndex(stressIndex);
			sswRequset.setHrv(hrv);
			sswRequset.setEda(eda);

			return sswRequset;
		} else {
			log.error("mac:" + deviceMac + "，rowdata長度錯誤" + rawData.length());
			throw new RuntimeException();
		}
	}

	// 步數計算（Low byte first）
	private Integer getStep(String rawData) {
		String hexStepString = rawData.substring(42, 44) + rawData.substring(40, 42);
		Integer step = Integer.parseInt(hexStepString, 16);
		return step;
	}

	// 心率
	private short getHeartRate(String rawData) {
		String hexHeartRateString = rawData.substring(48,50);
		Short heartRate = Short.parseShort(hexHeartRateString, 16);
		return heartRate;
	}

	// 體溫
	private float getBodyTemperature(String rawData) {
		String hexBodyTemperatureString = rawData.substring(46, 48) + rawData.substring(44, 46);
		Float bodyTemperature = Float.valueOf(Integer.parseInt(hexBodyTemperatureString, 16)) / 100;
		return bodyTemperature;
	}

	// 電量
	private byte getBattery(String rawData) {
		String hexBatteryString = rawData.substring(52, 54);
		Byte battery = Byte.parseByte(hexBatteryString, 16);
		return battery;
	}

	// SOS
	private boolean getSos(String rawData) {
		String hexSosString = rawData.substring(50, 52);
		Byte sos = Byte.parseByte(hexSosString, 16);
		if (sos == 1) {
			// System.out.println("按下 SOS");
			return true;
		} else {
			// System.out.println("恢復 SOS");
		}
		return false;
	}

	// 血氧
	private Integer getBloodOxygen(String rawData) {
		String hexBloodOxygenString = rawData.substring(16, 18);
		Integer bloodOxygen = Integer.parseInt(hexBloodOxygenString, 16);
		return bloodOxygen;
	}

	// 用公式計算
	private Integer getCalorie(Integer step) {
		int calorie = 0;
		try {

			float f_calorie = step * 0.6f * 60 * 1.036f / 1000;
			calorie = Math.round(f_calorie);
			// System.out.println("Calorie:"+ calorie);
		} catch (Exception e) {
			System.out.println("Parsing Calorie Error!");
		}

		return calorie;
	}

	// 收縮壓(SBP)
	private Integer getSystolic(String rawData) {
		String hexSystolicString = rawData.substring(28, 30);
		Integer systolic = Integer.parseInt(hexSystolicString, 16);
		// System.out.println("systolic:"+ systolic);
		return systolic;
	}

	// 舒張壓(DBP)
	private Integer getDiastolic(String rawData) {
		String hexDiastolicString = rawData.substring(30, 32);
		Integer diastolic = Integer.parseInt(hexDiastolicString, 16);
		// System.out.println("diastolic:"+ diastolic);
		return diastolic;
	}
	
	// 配戴狀態
	private Byte getState(String rawData) {
	    String hexStateString = rawData.substring(32, 34);
	    
	    if (hexStateString.equals("00")) {
	        return 0; // 未配戴
	    } else if (hexStateString.equals("01")) {
	        return 1; // 已配戴
	    } else {
	        return -1; // 未知狀態
	    }
	}
	// 壓力指數
	private Integer getStressIndex(String rawData) {
		String hexStressString = rawData.substring(20, 22);
		return Integer.parseInt(hexStressString, 16);
	}

	// HRV (高字節在前 Big Endian)
	private Integer getHrv(String rawData) {
		String hexHrvString = rawData.substring(22, 26);
		return Integer.parseInt(hexHrvString, 16);
	}

	// EDA (放大 10 倍，1 Bytes)
	private Float getEda(String rawData) {
		String hexEdaString = rawData.substring(34, 36);
		Integer edaInt = Integer.parseInt(hexEdaString, 16);
		// 若超過 255 也是以 255 呈現 (25.5)
		if (edaInt >= 255) return 25.5f;
		return edaInt / 10.0f;
	}

	// 合理性檢查
//	private boolean isValidData(byte battery, float bodyTemperature, short heartRate, Integer step) {
//		if (battery < 0 || battery > 100)
//			return false;
//		if (bodyTemperature < 30 || bodyTemperature > 45)
//			return false; // 人體體溫合理範圍
//		if (heartRate < 30 || heartRate > 220)
//			return false; // 常見心率範圍
//		if (step < 0 || step > 50000)
//			return false; // 步數不可能負數或過大
//		return true;
//	}

}
