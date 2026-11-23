package com.chylyng.hardware.Bv30xBv30s.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Bv30xBV30sConverter {

	public Bv30xRequest convert(DeviceDataBo deviceDataBo) {
		
		log.info("進行Bv30xRequest拆解");
		String rawData = deviceDataBo.getRawData();
		String deviceMac = deviceDataBo.getDeviceMac();
		if (rawData.length() >= 54) { //原本是62改為54
			String deviceModelName = deviceDataBo.getDeviceModelName();
			LocalDateTime time = deviceDataBo.getTime();
			
			
			
			byte battery = getBattery(rawData);
			float bodyTemperature = getBodyTemperature(rawData);
			short heartRate = getHeartRate(rawData);
			Integer step = getStep(rawData);
			boolean sos= getSos(rawData);
			Integer bloodOxygen = getBloodOxygen(rawData);
			Integer calorie = getCalorie(step);
			Integer systolic = getSystolic(rawData); //收縮壓(SBP)
			Integer diastolic = getDiastolic(rawData); //舒張壓(DBP)
			
			// 合理性過濾條件
            if (!isValidData(battery, bodyTemperature, heartRate, step)) {
                log.warn("資料異常，忽略: rawData:" + rawData + "; deviceMac:" + deviceMac +
                        ", battery:" + battery + ", heartRate:" + heartRate +
                        ", step:" + step + ", bodyTemperature:" + bodyTemperature +",sos:"+sos);
                return null; // 或者丟 Exception / 寫入 errorLog / 存DB標記
            }
			
			log.info("rawData:"+rawData+";deviceMac:"+deviceMac+",battery:"+battery+",heartRate:"+heartRate+",step:"+step+",bodyTemperature:"+bodyTemperature+",sos:"+sos);
			
			Bv30xRequest bv30xRequest = new Bv30xRequest();
			bv30xRequest.setGatewayMac(deviceDataBo.getGatewayMac());
			bv30xRequest.setRssi(deviceDataBo.getRssi());
			bv30xRequest.setDeviceMac(deviceMac);
			bv30xRequest.setDeviceModelName(deviceModelName);
			
			bv30xRequest.setBattery(battery);
			bv30xRequest.setBodyTemperature(bodyTemperature);
			bv30xRequest.setHeartRate(heartRate);
			bv30xRequest.setTime(time);
			bv30xRequest.setStep(step);
			bv30xRequest.setSos(sos);
			bv30xRequest.setBloodOxygen(bloodOxygen);
			bv30xRequest.setCalorie(calorie);
			bv30xRequest.setSystolic(systolic); //收縮壓(SBP)
			bv30xRequest.setDiastolic(diastolic); //舒張壓(DBP)
			
			return bv30xRequest;
		} else {
			log.error("mac:" + deviceMac + "，rowdata長度錯誤" + rawData.length());
			throw new RuntimeException();
		}
	}
	
	
	
	//步數計算（Low byte first）
	private Integer getStep(String rawData) {
		String hexStepString = rawData.substring(40, 42) + rawData.substring(38, 40);
		Integer step = Integer.parseInt(hexStepString, 16);
		return step;
	}

	//心率
	private short getHeartRate(String rawData) {
		String hexHeartRateString = rawData.substring(46, 48);
		Short heartRate = Short.parseShort(hexHeartRateString, 16);
		return heartRate;
	}
	
	//體溫
	private float getBodyTemperature(String rawData) {
		String hexBodyTemperatureString = rawData.substring(44, 46) + rawData.substring(42, 44);
		Float bodyTemperature = Float.valueOf(Integer.parseInt(hexBodyTemperatureString, 16)) / 100;
		return bodyTemperature;
	}

	//電量
	private byte getBattery(String rawData) {
		String hexBatteryString = rawData.substring(50, 52);
		Byte battery = Byte.parseByte(hexBatteryString, 16);
		return battery;
	}
	
	//SOS
	private boolean getSos(String rawData) {
		String hexSosString = rawData.substring(48, 50);
		Byte sos = Byte.parseByte(hexSosString, 16);
		if (sos == 1) {
			//System.out.println("按下 SOS");
			return true;
		}else {
			//System.out.println("恢復 SOS");
		}
		return false;
	}
	
	//血氧
	private Integer getBloodOxygen(String rawData) {
		String hexBloodOxygenString = rawData.substring(14,16) ;
		Integer bloodOxygen = Integer.parseInt(hexBloodOxygenString, 16);
		return bloodOxygen;
	}
	
	//用公式計算
	private Integer getCalorie(Integer step) {
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
	
	//收縮壓(SBP)
	private Integer getSystolic(String rawData) {
		String hexSystolicString = rawData.substring(26,28) ;
		Integer systolic = Integer.parseInt(hexSystolicString, 16);
		//System.out.println("systolic:"+ systolic);
		return systolic;			
	}
	
	
	//舒張壓(DBP)
	private Integer getDiastolic(String rawData) {
		String hexDiastolicString = rawData.substring(28,30) ;
		Integer diastolic = Integer.parseInt(hexDiastolicString, 16);
		//System.out.println("diastolic:"+ diastolic);
		return diastolic;
	}
	
	//合理性檢查
    private boolean isValidData(byte battery, float bodyTemperature, short heartRate, Integer step) {
        if (battery < 0 || battery > 100) return false;
        if (bodyTemperature < 30 || bodyTemperature > 45) return false; // 人體體溫合理範圍
        if (heartRate < 30 || heartRate > 220) return false; // 常見心率範圍
        if (step < 0 || step > 50000) return false; // 步數不可能負數或過大
        return true;
    }
	


}
