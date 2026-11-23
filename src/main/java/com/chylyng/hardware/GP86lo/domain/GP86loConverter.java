package com.chylyng.hardware.GP86lo.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GP86loConverter {
	public GP86loRequest convert(DeviceDataBo deviceDataBo) {

		// log.info("進行GP18loRequest拆解");
		String rawData = deviceDataBo.getRawData();
		String deviceMac = deviceDataBo.getDeviceMac();
		if (rawData.length() >= 14) {
			String deviceModelName = deviceDataBo.getDeviceModelName();
			LocalDateTime time = deviceDataBo.getTime();
			GP86loRequest gP86loRequest = new GP86loRequest();
			gP86loRequest.setGatewayMac(deviceDataBo.getGatewayMac());
			gP86loRequest.setRssi(deviceDataBo.getRssi());
			gP86loRequest.setDeviceMac(deviceMac);
			gP86loRequest.setDeviceModelName(deviceModelName);
			gP86loRequest.setTime(time);
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

//			for (int i = 0; i < 2; i++) {
				String bdValue = bdValues.get(0);
				String DbData = substrings.get(0);
				System.out.println("Gp18Lo ServiceDbData:" + DbData + ",bdValues.get(i)" + bdValue);

				switch (bdValue) {

				case "D6":// 藍芽
					String beaconID1 = getFirstMinor(DbData);
					gP86loRequest.setBeaconID1(beaconID1);
					break;

				case "02":// 警報數據(SOS 等)
					System.out.println("TcpService Message ID: 02");
					String sOSDates = DbData.substring(4, 6);
					System.out.println("Extracted SOS substring: '" + sOSDates + "'");
					switch (sOSDates) {
					case "02":
						gP86loRequest.setSos(true);
						break;
					}
					log.info("進行GP18loRequest拆解" + gP86loRequest);
					return gP86loRequest;

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
					gP86loRequest.setLatitude(latitude);// 經度
					gP86loRequest.setLongitude(longitude);// 緯度
					gP86loRequest.setSos(false);
					break;
					
				 case "F9":// 電量
					 System.out.println("GP86lo Message ID: F9");
					 String hexBatteryType = rawData.substring(6, 8);
					System.out.println("hexBatteryType:"+hexBatteryType);
					 byte batteryByte = (byte) Integer.parseInt(hexBatteryType, 16);
					 gP86loRequest.setBattery(batteryByte);
					 gP86loRequest.setSos(false);
				}
			  
//			}
			return gP86loRequest;
		} else {
			log.error("mac:" + deviceMac + "，rowdata長度錯誤" + rawData.length());
			throw new RuntimeException();
		}
	}

	//
	private static String getFirstMinor(String rawData) {
		int startIndex = 18;
		String majorHex = rawData.substring(startIndex + 2, startIndex + 4)
				+ rawData.substring(startIndex, startIndex + 2);
		String minorHex = rawData.substring(startIndex + 6, startIndex + 8)
				+ rawData.substring(startIndex + 4, startIndex + 6);
		String rssiHex = rawData.substring(startIndex + 8, startIndex + 10);
		int major = Integer.parseInt(majorHex, 16);
		String majorString = String.valueOf(major);
		int minor = Integer.parseInt(minorHex, 16);
		String minorString = String.valueOf(minor);

		int rssi = Integer.parseInt(rssiHex, 16);

		if (rssi > 127) {
			rssi -= 256; // Convert to signed value
		}
		System.out.println("Major: " + majorString + ", Minor: " + minorString + ", RSSI: " + rssi);
		return minorString;

	}

	// 步數計算（Low byte first）
	private Integer getStep(String rawData) {
		String hexStepString = rawData.substring(26, 28) + rawData.substring(24, 26) + rawData.substring(22, 24)
				+ rawData.substring(20, 22);
		Integer step = Integer.parseInt(hexStepString, 16);
		return step;
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
