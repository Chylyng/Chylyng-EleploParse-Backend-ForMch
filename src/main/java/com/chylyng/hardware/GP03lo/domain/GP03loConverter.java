package com.chylyng.hardware.GP03lo.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GP03loConverter {
	public GP03loRequest convert(DeviceDataBo deviceDataBo) {

		// log.info("進行GP03loRequest拆解");
		String rawData = deviceDataBo.getRawData();
		String deviceMac = deviceDataBo.getDeviceMac();
		if (rawData.length() >= 128) {
			String deviceModelName = deviceDataBo.getDeviceModelName();
			LocalDateTime time = deviceDataBo.getTime();

			byte battery = getBattery(rawData);
			float bodyTemperature = getBodyTemperature(rawData);
			short heartRate = getHeartRate(rawData);
			Integer step = getStep(rawData);
			boolean sos = getSos(rawData);
			Integer bloodOxygen = getBloodOxygen(rawData);
			Integer calorie = getCalorie(rawData);
			Integer systolic = getSystolic(rawData); // 收縮壓(SBP)
			Integer diastolic = getDiastolic(rawData); // 舒張壓(DBP)
			String beaconID1 = getBeaconID1(rawData);
			float beaconID1RSSI = getBeaconID1RSSI(rawData);
			String beaconID2 = getBeaconID2(rawData);
			float beaconID2RSSI = getBeaconID2RSSI(rawData);
			String beaconID3 = getBeaconID3(rawData);
			float beaconID3RSSI = getBeaconID3RSSI(rawData);
			boolean wearStatus = getWearStatus(rawData);		
			String latitude =getLatitude(rawData);//緯度
			String longitude =getLongitude(rawData);//經度

			GP03loRequest gP03loRequest = new GP03loRequest();
			gP03loRequest.setGatewayMac(deviceDataBo.getGatewayMac());
			gP03loRequest.setRssi(deviceDataBo.getRssi());
			gP03loRequest.setDeviceMac(deviceMac);
			gP03loRequest.setDeviceModelName(deviceModelName);

			gP03loRequest.setBattery(battery);
			gP03loRequest.setBodyTemperature(bodyTemperature);
			gP03loRequest.setHeartRate(heartRate);
			gP03loRequest.setTime(time);
			gP03loRequest.setStep(step);
			gP03loRequest.setSos(sos);
			gP03loRequest.setBloodOxygen(bloodOxygen);
			gP03loRequest.setCalorie(calorie);
			gP03loRequest.setSystolic(systolic); // 收縮壓(SBP)
			gP03loRequest.setDiastolic(diastolic); // 舒張壓(DBP)
			gP03loRequest.setBeaconID1(beaconID1);
			gP03loRequest.setBeaconID1RSSI(beaconID1RSSI);
			gP03loRequest.setBeaconID2(beaconID2);
			gP03loRequest.setBeaconID2RSSI(beaconID2RSSI);
			gP03loRequest.setBeaconID3(beaconID3);
			gP03loRequest.setBeaconID3RSSI(beaconID3RSSI);
			gP03loRequest.setWearStatus(wearStatus);//配戴狀態
			gP03loRequest.setLatitude(latitude);//經度
			gP03loRequest.setLongitude(longitude);//緯度
			log.info("進行GP03loRequest拆解"+gP03loRequest);
			return gP03loRequest;

		} else {
			log.error("mac:" + deviceMac + "，rowdata長度錯誤" + rawData.length());
			throw new RuntimeException();
		}

	}

	// 心率
	private static short getHeartRate(String rawData) {
		String hexHeartRateString = rawData.substring(32, 34);
		Short heartRate = Short.parseShort(hexHeartRateString, 16);
		return heartRate;
	}

	// 步數計算（Low byte first）
	private static Integer getStep(String rawData) {
		String hexStepString = rawData.substring(26, 28) + rawData.substring(24, 26);
		Integer step = Integer.parseInt(hexStepString, 16);
		return step;
	}

	// 舒張壓(DBP)
	private static Integer getDiastolic(String rawData) {
		String hexDiastolicString = rawData.substring(14, 16);
		Integer diastolic = Integer.parseInt(hexDiastolicString, 16);
		return diastolic;
	}

	// 收縮壓(SBP)
	private static Integer getSystolic(String rawData) {
		String hexSystolicString = rawData.substring(12, 14);
		Integer systolic = Integer.parseInt(hexSystolicString, 16);
		return systolic;
	}

	// 血氧
	private static Integer getBloodOxygen(String rawData) {
		String hexBloodOxygenString = rawData.substring(0, 2);
		Integer bloodOxygen = Integer.parseInt(hexBloodOxygenString, 16);
		return bloodOxygen;
	}

	// 電量
	private static byte getBattery(String rawData) {
		String hexBatteryString = rawData.substring(36, 38);
		Byte battery = Byte.parseByte(hexBatteryString, 16);
		return battery;
	}

	// 體溫
	private static float getBodyTemperature(String rawData) {
		String BodyTemperatureString = rawData.substring(30, 32)+rawData.substring(28, 30); 
		int integerTemperature = Integer.parseInt(BodyTemperatureString, 16);
	    float bodyTemperature = integerTemperature / 100.0f;
		if(rawData.substring(30, 32).equals("48") && rawData.substring(28, 30).equals("69")) {
			return  40.0f;
		}else if(rawData.substring(30, 32).equals("4C") && rawData.substring(28, 30).equals("6F") ) {
			return  36.0f;
		}else {
			return bodyTemperature;
		}
		
	}

	// SOS
	private static boolean getSos(String rawData) {
		String SosString = rawData.substring(34, 36);
		Integer bloodSos = Integer.parseInt(SosString, 16);

		boolean sos = bloodSos.equals(3);
		return sos;
	}

	// Calorie(Low bytes are first)
	private static Integer getCalorie(String rawData) {
		String calorieString = rawData.substring(18, 20) + rawData.substring(16, 18);
		Integer calorie = Integer.parseInt(calorieString, 16);

		return calorie;
	}
	
	//緯度
	private static String getLatitude(String rawData) {
		 double gpsLongitude = 0.0;
		    

		    String latitudeString = rawData.substring(112, 114) + rawData.substring(110, 112) +rawData.substring(108, 110)+rawData.substring(106, 108)+rawData.substring(104, 106)+rawData.substring(102, 104)+rawData.substring(100, 102)+rawData.substring(98, 100);
		    String latitudeSorN = rawData.substring(114,116).toLowerCase();;
		    if(latitudeSorN.equals("00")) {
		    	
		    }else {
		   
		    long longHex = parseUnsignedHex(latitudeString);
		    
		    gpsLongitude = Double.longBitsToDouble(longHex);  
		    
		    String gpsStr = Double.toString(gpsLongitude);
		    int decimalIndex = gpsStr.indexOf('.');
		    String gpsDegree = gpsStr.substring(0, decimalIndex-2);
		    String gpsPrime = gpsStr.substring(decimalIndex-2, decimalIndex+5);
		    String gpsSouthNorth;
		    if (latitudeSorN.equals("53")) {
		    	gpsSouthNorth = "S";
		    } else if (latitudeSorN.equals("4e")) {
		    	gpsSouthNorth = "N";
		    } else {
		    	gpsSouthNorth = "error";
		    }
		    
		    String latitude = gpsDegree + "°" + gpsPrime + "'" + gpsSouthNorth;
		    return latitude;
		    }
			return "0";
	}
	
    //經度
	private static String getLongitude(String rawData) {
	    double gpsLatitude = 0.0;
	    

	    String latitudeString = rawData.substring(94, 96) + rawData.substring(92, 94) +rawData.substring(90, 92)+rawData.substring(88, 90)+rawData.substring(86, 88)+rawData.substring(84, 86)+rawData.substring(82, 84)+rawData.substring(80, 82);
	    String latitudeEorW = rawData.substring(96, 98);
	    if(latitudeEorW.equals("00")) {
	    	
	    }else {
	    long longHex = parseUnsignedHex(latitudeString);
	    gpsLatitude = Double.longBitsToDouble(longHex);  
	    String gpsStr = Double.toString(gpsLatitude);
	    int decimalIndex = gpsStr.indexOf('.');
	    String gpsDegree = gpsStr.substring(0, decimalIndex-2);
	    String gpsPrime = gpsStr.substring(decimalIndex-2, decimalIndex+5);
	    String gpsWestEast;
	    if (latitudeEorW.equals("45")) {
	        gpsWestEast = "E";
	    } else if (latitudeEorW.equals("57")) {
	        gpsWestEast = "W";
	    } else {
	        gpsWestEast = "error";
	    }
	    
	    String latitude = gpsDegree + "°" + gpsPrime + "'" + gpsWestEast;
	    return latitude;
	    }
		return "0";
	}

	public static long parseUnsignedHex(String text) {
	    if (text.length() == 16) {
	    	
	        return  (parseUnsignedHex(text.substring(0, 1)) << 60)
	                | parseUnsignedHex(text.substring(1));
	    }
	    return Long.parseLong(text, 16);
	}

	
	
	//0 not wear，1 already wear
	private static boolean getWearStatus(String rawData) {
		String wearStatusString = rawData.substring(34,36) ;
		Integer booleanWearStatus = Integer.parseInt(wearStatusString, 16);	
		
	  boolean wearStatus = booleanWearStatus.equals("1");

	  return wearStatus;
	}

	//Is a positive integer with the minus sign removed
	private static float getBeaconID3RSSI(String rawData) {
		String beaconID3RssiString = rawData.substring(56, 58);
		Integer beaconID3Rss = Integer.parseInt(beaconID3RssiString, 16);
		Float beaconID3Rssi =-Float.valueOf(beaconID3Rss);
		return beaconID3Rssi;
		
	}
	private static String getBeaconID3(String rawData) {
		String beaconID3String = rawData.substring(52, 56);
		int beaconID3Int = Integer.parseInt(beaconID3String, 16);
	    String beaconID3 = String.valueOf(beaconID3Int);
		return beaconID3;
	}
	//Is a positive integer with the minus sign removed
	private static float getBeaconID2RSSI(String rawData) {
		String beaconID2RssiString = rawData.substring(50, 52);
		Integer beaconID2Rss = Integer.parseInt(beaconID2RssiString, 16);
		Float beaconID2Rssi =-Float.valueOf(beaconID2Rss);
		return beaconID2Rssi;

	}
	private static String getBeaconID2(String rawData) {
		String beaconID2String = rawData.substring(46, 50);
		int beaconID2Int = Integer.parseInt(beaconID2String, 16);
	    String beaconID2 = String.valueOf(beaconID2Int);
		return beaconID2;
	
	}
	//Is a positive integer with the minus sign removed
	private static float getBeaconID1RSSI(String rawData) {
		String beaconID1RssiString = rawData.substring(44, 46);
		Integer beaconID1Rss = Integer.parseInt(beaconID1RssiString, 16);
		Float beaconID1Rssi =-Float.valueOf(beaconID1Rss);
		
		return beaconID1Rssi;
	}
	private static String getBeaconID1(String rawData) {
		String beaconID1String = rawData.substring(40, 44);
		int beaconID1Int = Integer.parseInt(beaconID1String, 16);
	    String beaconID1 = String.valueOf(beaconID1Int);
		return beaconID1;
	}

	
	
	

}
