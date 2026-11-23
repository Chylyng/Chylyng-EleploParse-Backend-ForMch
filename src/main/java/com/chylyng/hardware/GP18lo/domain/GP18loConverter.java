package com.chylyng.hardware.GP18lo.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GP18loConverter {
	public GP18loRequest convert(DeviceDataBo deviceDataBo) {

		// log.info("進行GP18loRequest拆解");
		String rawData = deviceDataBo.getRawData();
		String deviceMac = deviceDataBo.getDeviceMac();
		if (rawData.length() >= 14) {
			String deviceModelName = deviceDataBo.getDeviceModelName();
			LocalDateTime time = deviceDataBo.getTime();
//			String prefix = "BD";
//			int prefixIndex = rawData.indexOf(prefix) + prefix.length();
//			String value = rawData.substring(prefixIndex, prefixIndex + 2);
			GP18loRequest gP18loRequest = new GP18loRequest();
			gP18loRequest.setGatewayMac(deviceDataBo.getGatewayMac());
			gP18loRequest.setRssi(deviceDataBo.getRssi());
			gP18loRequest.setDeviceMac(deviceMac);
			gP18loRequest.setDeviceModelName(deviceModelName);
			
			Pattern pattern = Pattern.compile("BD(..)");
	        Matcher matcher = pattern.matcher(rawData);
	        ArrayList<String> bdValues = new ArrayList<>();
	        ArrayList<String> substrings = new ArrayList<>();
	        
	        int lastEnd = 0;
	        while (matcher.find()) {
	            bdValues.add(matcher.group(1));
	            int start = matcher.start();
	            if (start > lastEnd) {
	                substrings.add(rawData.substring(lastEnd, start));
	            }
	            lastEnd = start;
	        }
	        // 添加最後一個子字符串
	        if (lastEnd < rawData.length()) {
	            substrings.add(rawData.substring(lastEnd));
	        }
	        
	        for (int i = 0; i < bdValues.size(); i++) {
	        	String bdValue = bdValues.get(i);
	        	String DbData =substrings.get(i);
	        	System.out.println("Gp18Lo ServiceDbData:"+ DbData+",bdValues.get(i)"+bdValue);
	        	
	        	switch (bdValue) {
			    case "32": // 健康數據
				System.out.println("Gp18Lo Service Message ID: 32");
				byte battery = getBattery(DbData);
				float bodyTemperature = getBodyTemperature(DbData);
				short heartRate = getHeartRate(DbData);
				Integer step = getStep(DbData);
				Integer bloodOxygen = getBloodOxygen(DbData);
				Integer calorie = getCalorie(step);
				Integer systolic = getSystolic(DbData); // 收縮壓(SBP)
				Integer diastolic = getDiastolic(DbData); // 舒張壓(DBP)

				gP18loRequest.setBattery(battery);
				gP18loRequest.setBodyTemperature(bodyTemperature);
				gP18loRequest.setHeartRate(heartRate);
				gP18loRequest.setTime(time);
				gP18loRequest.setStep(step);
				gP18loRequest.setBloodOxygen(bloodOxygen);
				gP18loRequest.setCalorie(calorie);
				gP18loRequest.setSystolic(systolic); // 收縮壓(SBP)
				gP18loRequest.setDiastolic(diastolic); // 舒張壓(DBP)
				gP18loRequest.setSos(false);
				log.info("進行GP18loRequest拆解" + gP18loRequest);
				    break;
			    case "D6":// 藍芽
					String beaconID1= getFirstMinor(DbData);
					gP18loRequest.setBeaconID1(beaconID1);
					break;
				
			    case "B5":// 警報數據(SOS 等)
			        System.out.println("TcpService Message ID: B5");
			        String sOSDates = DbData.substring(4, 6);
			        System.out.println("Extracted SOS substring: '" + sOSDates + "'");
			        switch (sOSDates) {
			        case "01":
		            gP18loRequest.setSos(true);
		           
		            break;
		            }
			        log.info("進行GP18loRequest拆解" + gP18loRequest);
			        return gP18loRequest;
				
			    case "03":// GPS
				// Extract the substring for the longitude and latitude in little-endian format
				String longitudeHexLittleEndian = DbData.substring(4, 20); // Note the indices for longitude
				String latitudeHexLittleEndian = DbData.substring(20, 36); // Note the indices for latitude

				// Convert little-endian hex to double
				double longitude = convertLittleEndianHexToDouble(longitudeHexLittleEndian);
				double latitude = convertLittleEndianHexToDouble(latitudeHexLittleEndian);
				
				// Print the longitude and latitude values
				System.out.println("Longitude: " + longitude);
				System.out.println("Latitude: " + latitude);
				gP18loRequest.setLatitude(latitude);//經度
				gP18loRequest.setLongitude(longitude);//緯度
				gP18loRequest.setSos(false);
				break;
	        }
	       
		
			}
	        return gP18loRequest;
		} else {
			log.error("mac:" + deviceMac + "，rowdata長度錯誤" + rawData.length());
			throw new RuntimeException();
		}
	}
      //
      private static  String getFirstMinor(String rawData) {
	  int startIndex = 18;
	    String majorHex = rawData.substring(startIndex+2, startIndex + 4)+rawData.substring(startIndex, startIndex + 2);
      String minorHex = rawData.substring(startIndex + 6, startIndex + 8)+rawData.substring(startIndex + 4, startIndex + 6);
      String rssiHex = rawData.substring(startIndex + 8, startIndex + 10);
      int major = Integer.parseInt(majorHex, 16);
      String majorString = String.valueOf(major);
      int minor = Integer.parseInt(minorHex, 16);
      String minorString= String.valueOf(minor);
      
      int rssi = Integer.parseInt(rssiHex, 16);
      
      if (rssi > 127) {
          rssi -= 256; // Convert to signed value
      }
      System.out.println("Major: " + majorString +
              ", Minor: " + minorString +
              ", RSSI: " + rssi);
      return minorString;
	  
}

	private static Byte getBattery(String rawData) {
		String hexBatteryType = rawData.substring(29, 30);
		Byte battery = 0;

		switch (hexBatteryType) {
		case "0":
			battery = 10;
			break;
		case "1":
			battery = 30;
			break;
		case "2":
			battery = 60;
			break;
		case "3":
			battery = 80;
			break;
		case "4":
			battery = 100;
			break;
		}

		return battery;

	};

	// 步數計算（Low byte first）
	private Integer getStep(String rawData) {
		String hexStepString = rawData.substring(26, 28) + rawData.substring(24, 26) + rawData.substring(22, 24)
				+ rawData.substring(20, 22);
		Integer step = Integer.parseInt(hexStepString, 16);
		return step;
	}

	// 心率
	private short getHeartRate(String rawData) {
		String hexHeartRateString = rawData.substring(8, 10);
		Short heartRate = Short.parseShort(hexHeartRateString, 16);
		return heartRate;
	}

	// 收縮壓
	private Integer getSystolic(String rawData) {
		String hexSystolicString = rawData.substring(4, 6);// 收縮壓
		Integer systolic = Integer.parseInt(hexSystolicString, 16);
		return systolic;
	}

	// 舒張壓
	private Integer getDiastolic(String rawData) {
		String hexDiastolicString = rawData.substring(6, 8);// 舒張壓
		Integer diastolic = Integer.parseInt(hexDiastolicString, 16);
		return diastolic;
	}

	// 體溫
	private float getBodyTemperature(String rawData) {
		String BodyTemperatureString = rawData.substring(18, 20) + rawData.substring(16, 18);
		int temperatureValue = Integer.parseInt(BodyTemperatureString, 16);
		float bodyTemperature = temperatureValue * 0.1f;
		return bodyTemperature;

	}

	// 血氧
	private Integer getBloodOxygen(String rawData) {
		String hexBloodOxygenString = rawData.substring(10, 12);
		Integer bloodOxygen = Integer.parseInt(hexBloodOxygenString, 16);
		return bloodOxygen;
	}

	// 用公式計算卡路里
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

	private static double convertLittleEndianHexToDouble(String hexLittleEndian) {
		// Convert little-endian hex to big-endian
		String hexBigEndian = littleEndianToBigEndian(hexLittleEndian);

		// Convert big-endian hex string to long
		long longBits = Long.parseUnsignedLong(hexBigEndian, 16);

		// Convert the long bits to double
		return Double.longBitsToDouble(longBits);
	}

	private static String littleEndianToBigEndian(String hex) {
		StringBuilder result = new StringBuilder();
		for (int i = hex.length() - 2; i >= 0; i -= 2) {
			result.append(hex.substring(i, i + 2));
		}
		return result.toString();
	}

}
