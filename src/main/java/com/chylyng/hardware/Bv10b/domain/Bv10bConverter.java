package com.chylyng.hardware.Bv10b.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Bv10bConverter {

	public Bv10bRequest convert(DeviceDataBo deviceDataBo) {

		// 02010603030d1816ffb80000000000000000000002c012ff6241601936fb0bffb60831b7e49d804224ee1209423130435f45343944383034323234454500

		// log.info("進行Bv30xRequest拆解");
		String rawData = deviceDataBo.getRawData();
		System.out.println(
				"Bv10b===>rawData:" + rawData );
	
		// 測試一般狀況
//		String rawDataB8 = "02010613FFB852220000020055425600026227EB624B2324161FB60831B7E49D804224EE";
		// 測試跌倒
//		String rawDataB8 = "02010613FFB85222000006020055425600026227EB624B2324161FB60831B7E49D804224EE";
		// 測試sos
//		String rawDataB8 = "02010613FFB85222000003020055425600026227EB624B2324161FB60831B7E49D804224EE";

		// 從B8開始擷取rawData
		rawData = rawData.toUpperCase();
		rawData = rawData.substring(rawData.indexOf("B8"));
		

		String deviceMac = deviceDataBo.getDeviceMac();
//		if (rawData.length() >= 54) { //原本是62改為54
		String deviceModelName = deviceDataBo.getDeviceModelName();
		LocalDateTime time = deviceDataBo.getTime();
        
		boolean still = getStill(rawData);//靜止
		byte state = getState(rawData);
		byte battery = getBattery(rawData);
		float bodyTemperature = getBodyTemperature(rawData);
		short heartRate = getHeartRate(rawData);
		Integer step = getStep(rawData);
		boolean sos = getSos(rawData);
		Integer bloodOxygen = getBloodOxygen(rawData);
		Integer calorie = getCalorie(step);
		Integer systolic = getSystolic(rawData); // 收縮壓(SBP)
		Integer diastolic = getDiastolic(rawData); // 舒張壓(DBP)
		boolean fallOff = getFallOff(rawData);// 跌倒
		System.out.println("配戴狀況state:" + state+"靜止:"+still);

		// log.info("battery:"+battery+",heartRate:"+heartRate+",step:"+step+",bodyTemperature:"+bodyTemperature+",sos:"+sos+",FallOff:"+FallOff);
		if (state == 1) {
			Bv10bRequest bv10bRequest = new Bv10bRequest();
			bv10bRequest.setGatewayMac(deviceDataBo.getGatewayMac());
			bv10bRequest.setRssi(deviceDataBo.getRssi());
			bv10bRequest.setDeviceMac(deviceMac);
			bv10bRequest.setDeviceModelName(deviceModelName);
			bv10bRequest.setState(state);
			bv10bRequest.setBattery(battery);
			bv10bRequest.setBodyTemperature(bodyTemperature);
			bv10bRequest.setHeartRate(heartRate);
			bv10bRequest.setTime(time);
			bv10bRequest.setStep(step);
			bv10bRequest.setSos(sos);
			bv10bRequest.setBloodOxygen(bloodOxygen);
			bv10bRequest.setCalorie(calorie);
			bv10bRequest.setSystolic(systolic); // 收縮壓(SBP)
			bv10bRequest.setDiastolic(diastolic); // 舒張壓(DBP)
			bv10bRequest.setFallOff(fallOff);
			bv10bRequest.setStill(still);
			System.out.println(
					"Bv10b===>battery:" + battery + ",heartRate:" + heartRate + ",step:" + step + ",bodyTemperature:"
							+ bodyTemperature + ",sos:" + sos + ",FallOff:" + fallOff + ",配戴狀況state:" + state+"靜止狀態:"+still);
			return bv10bRequest;
		} else {
			Bv10bRequest bv10bRequest = new Bv10bRequest();
			bv10bRequest.setGatewayMac(deviceDataBo.getGatewayMac());
			bv10bRequest.setRssi(deviceDataBo.getRssi());
			bv10bRequest.setDeviceMac(deviceMac);
			bv10bRequest.setTime(time);
			bv10bRequest.setDeviceModelName(deviceModelName);
			bv10bRequest.setStep(step);
			bv10bRequest.setState(state);
			bv10bRequest.setBattery(battery);
			bv10bRequest.setSos(sos);
			bv10bRequest.setFallOff(fallOff);
			bv10bRequest.setStill(still);
			System.out.println("Bv10b===>未配戴回傳:"+bv10bRequest);
			return bv10bRequest;
		}
		
//		} else {
//			log.error("mac:" + deviceMac + "，rowdata長度錯誤" + rawData.length());
//			throw new RuntimeException();
//		}
	}

	// 心率
	private short getHeartRate(String rawData) {
		String hexHeartRateString = rawData.substring(2, 4);
		Short heartRate = Short.parseShort(hexHeartRateString, 16);
		return heartRate;
	}

	// 步數計算（Low byte first）
	private Integer getStep(String rawData) {
		String hexStepString = rawData.substring(8, 10) + rawData.substring(6, 8) + rawData.substring(4, 6);
		Integer step = Integer.parseInt(hexStepString, 16);
		return step;
	}

	// 舒張壓(DBP)
	private Integer getDiastolic(String rawData) {
		String hexDiastolicString = rawData.substring(14, 16);
		Integer diastolic = Integer.parseInt(hexDiastolicString, 16);
		// System.out.println("diastolic:"+ diastolic);
		return diastolic;
	}

	// 收縮壓(SBP)
	private Integer getSystolic(String rawData) {
		String hexSystolicString = rawData.substring(16, 18);
		Integer systolic = Integer.parseInt(hexSystolicString, 16);
		// System.out.println("systolic:"+ systolic);
		return systolic;
	}

	// 血氧
	private Integer getBloodOxygen(String rawData) {
		String hexBloodOxygenString = rawData.substring(18, 20);
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

	// 電量
	private byte getBattery(String rawData) {
		String hexBatteryString = rawData.substring(34, 36);
		Byte battery = Byte.parseByte(hexBatteryString, 16);
		return battery;
	}

	// 體溫
	private float getBodyTemperature(String rawData) {
		String integerBodyTemperatureString = rawData.substring(36, 38);
		String decimalBodyTemperatureString = rawData.substring(38, 40);
		Float integerTemperature = Float.valueOf(Integer.parseInt(integerBodyTemperatureString, 16));
		Float decimalTemperature = Float.valueOf(Integer.parseInt(decimalBodyTemperatureString, 16)) / 100;
		Float bodyTemperature = integerTemperature + decimalTemperature;
		return bodyTemperature;
	}

	// SOS
	private boolean getSos(String rawData) {
		String hexFallOffString = rawData.substring(10, 12);
		int i = Integer.parseInt(hexFallOffString, 16);
		String binS = Integer.toBinaryString(i);
		if (binS.length() != 8) {
			for (int j = binS.length(); j < 8; j++) {
				binS = "0" + binS;
				// System.out.println("add binS " + binS);
			}
		}
		StringBuilder newBinS = new StringBuilder(binS);
		newBinS.reverse();
		String checkSos = newBinS.toString().substring(0, 1);
		boolean Sos = checkSos.equals("1");

		return Sos;
	}

	// 跌倒 hex to bin
	private boolean getFallOff(String rawData) {
		String hexFallOffString = rawData.substring(10, 12);
		int i = Integer.parseInt(hexFallOffString, 16);
		String binS = Integer.toBinaryString(i);
		if (binS.length() != 8) {
			for (int j = binS.length(); j < 8; j++) {
				binS = "0" + binS;
				// System.out.println("add binS " + binS);
			}
		}
		StringBuilder newBinS = new StringBuilder(binS);
		newBinS.reverse();
		String checkFallOff = newBinS.toString().substring(2, 3);
		boolean ifFall = checkFallOff.equals("1");

		return ifFall;
	}

	// 配戴狀況 state
	private byte getState(String rawData) {
		String hexFallOffString = rawData.substring(10, 12);
		int i = Integer.parseInt(hexFallOffString, 16);
		String binS = Integer.toBinaryString(i);
		if (binS.length() != 8) {
			for (int j = binS.length(); j < 8; j++) {
				binS = "0" + binS;
				// System.out.println("add binS " + binS);
			}
		}
		StringBuilder newBinS = new StringBuilder(binS);
		newBinS.reverse();
		String checkState = newBinS.toString().substring(1, 2);
		Byte state = Byte.parseByte(checkState, 16);
		return state;
	}
	
	// 配戴狀況 still
		private boolean getStill(String rawData) {
			String hexFallOffString = rawData.substring(10, 12);
			int i = Integer.parseInt(hexFallOffString, 16);
			String binS = Integer.toBinaryString(i);
			if (binS.length() != 8) {
				for (int j = binS.length(); j < 8; j++) {
					binS = "0" + binS;
					// System.out.println("add binS " + binS);
				}
			}
			StringBuilder newBinS = new StringBuilder(binS);
			newBinS.reverse();
			String checkStill = newBinS.toString().substring(6, 7);
			Byte Still = Byte.parseByte(checkStill, 16);
			boolean isStill = Still.equals("1");
			return isStill;
		}
	

}
