package com.chylyng.hardware.Bv10t.domain;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Bv10tConverter {
	public Bv10tRequest convert(DeviceDataBo deviceDataBo) {
        String rawData = deviceDataBo.getRawData().toUpperCase();

        // 提取自定義數據 (從 "B8" 開始)
        rawData = rawData.substring(rawData.indexOf("B8"));
        System.out.println(rawData);
        String deviceMac = deviceDataBo.getDeviceMac();
        String deviceModelName = deviceDataBo.getDeviceModelName();
        LocalDateTime time = deviceDataBo.getTime();
     // 2. 根據協議逐步解析各部分數據
        // 位元組順序：
        // - 位元組 1 (心率值)
        String heartRateHex = rawData.substring(2, 4);
        short heartRate = (short) Integer.parseInt(heartRateHex, 16);
        System.out.println("心率值 (Heart Rate): " + heartRate);

     // 2. 狀態標誌位 (位元組 2)
        String statusHex = rawData.substring(4, 6); // 提取第 2 個位元組
        int statusByte = Integer.parseInt(statusHex, 16); // 轉換為十進制

        // 提取每個 Bit 的值
        boolean bit7 = ((statusByte >> 7) & 0x01) == 1; // Bit7 運動模式
        boolean bit6 = ((statusByte >> 6) & 0x01) == 1; // Bit6 靜止
        boolean bit5 = ((statusByte >> 5) & 0x01) == 1; // Bit5 配戴
        byte bit5AsByte = (byte) (bit5 ? 1 : 0);
        boolean bit4 = ((statusByte >> 4) & 0x01) == 1; // Bit4 SOS
        boolean bit3 = ((statusByte >> 3) & 0x01) == 1; // Bit3 充電狀態
        boolean bit2 = ((statusByte >> 2) & 0x01) == 1; // Bit2 保留
        boolean bit1 = ((statusByte >> 1) & 0x01) == 1; // Bit1 跌倒告警
        boolean bit0 = ((statusByte >> 0) & 0x01) == 1; // Bit0 保留

        // 列印每個 Bit 的值
        System.out.println("狀態標誌位解析:");
        System.out.println("Bit7: " + (bit7 ? "1" : "0") + " (運動模式: " + bit7 + ")");
        System.out.println("Bit6: " + (bit6 ? "1" : "0") + " (靜止: " + !bit6 + ")");
        System.out.println("Bit5: " + (bit5 ? "1" : "0") + " (配戴: " + bit5 + ")");
        System.out.println("Bit4: " + (bit4 ? "1" : "0") + " (SOS: " + bit4 + ")");
        System.out.println("Bit3: " + (bit3 ? "1" : "0") + " (充電狀態: " + bit3 + ")");
        System.out.println("Bit2: " + (bit2 ? "1" : "0") + " (保留: " + bit2 + ")");
        System.out.println("Bit1: " + (bit1 ? "1" : "0") + " (跌倒告警: " + bit1 + ")");
        System.out.println("Bit0: " + (bit0 ? "1" : "0") + " (保留: " + bit0 + ")");

        // - 位元組 3 (睡眠狀態)
        String sleepStatusHex = rawData.substring(6, 8);
        int sleepStatus = Integer.parseInt(sleepStatusHex, 16);
        String sleepState;
        switch (sleepStatus) {
            case 0x00:
                sleepState = "清醒";
                break;
            case 0x01:
                sleepState = "淺度睡眠";
                break;
            case 0x02:
                sleepState = "深度睡眠";
                break;
            case 0xFF:
                sleepState = "未檢測";
                break;
            default:
                sleepState = "未知狀態";
                break;
        }
        System.out.println("睡眠狀態: " + sleepState);

     // - 位元組 8-9 (RRI，低位元組在前)
     // 提取低位元組 (位元組 8)
     String rriLowHex = rawData.substring(18, 20); // 位元組 8
     System.out.println("RRI 低位元組 (Hex): " + rriLowHex);

     // 提取高位元組 (位元組 9)
     String rriHighHex = rawData.substring(16, 18); // 位元組 9
     System.out.println("RRI 高位元組 (Hex): " + rriHighHex);

     // 將低位元組和高位元組拼接（低位在前）
     String rriHex = rriLowHex + rriHighHex;
     System.out.println("RRI 拼接後 (Hex, 低位在前): " + rriHex);

     // 轉換為十進制
     int rri = Integer.parseInt(rriHex, 16);
     System.out.println("RRI (心率變異性): " + rri);
     
  // 取 12-18 位元組的可變數據部分
        Integer step = null; // 步數  
        Integer calorie = null; //卡路里
        float  bodyTemperature = 0;//體溫
        Integer bloodOxygen = null;//血氧
        Integer systolic = null;//收縮壓
        Integer diastolic =null;//舒張壓
	    String variableData = rawData.substring(24,38); // 假設 12-18 字節從原始數據第 22 位開始
	    System.out.println("取 12-18 位元組的可變數據部分:"+variableData);
	    // 第一個字節為自定義標記
	    String customTagHex = variableData.substring(0, 2);
	    int customTag = Integer.parseInt(customTagHex, 16);
	    System.out.println("第一個字節為自定義標記:"+customTag);
	    switch (customTag) {
     case 0x00: // 計步與卡路里
         String stepLow = variableData.substring(2, 4);
         String stepMid = variableData.substring(4, 6);
         String stepHigh = variableData.substring(6, 8);
         step = Integer.parseInt(stepHigh + stepMid + stepLow, 16);
        
         String calorieLow = variableData.substring(8, 10);
         String calorieMid = variableData.substring(10, 12);
         String calorieHigh = variableData.substring(12, 14);
          calorie = Integer.parseInt(calorieHigh + calorieMid + calorieLow, 16);
      

         log.info("自定義標記 0x00 - 計步數據: Steps={}, Calorie={}", step, calorie);
         break;

     case 0x02: // 體溫數據
    	    // Extract ambient temperature
    	    String ambientTempLow = variableData.substring(2, 4);
    	    String ambientTempHigh = variableData.substring(4, 6);
    	    float x = Integer.parseInt(ambientTempHigh + ambientTempLow, 16) * 0.005f;
    	    System.out.println("環境溫度 (x): " +  x);
    	    
    	    // Extract body temperature
    	    String bodyTempLow = variableData.substring(6, 8);
    	    String bodyTempHigh = variableData.substring(8, 10);
    	    float y = Integer.parseInt(bodyTempHigh + bodyTempLow, 16) * 0.005f;
    	    System.out.println("體表溫度 (y): " + y);
    	    // Apply the formula for human body temperature (z) 原本代公式,現在改為直接回傳
//    	    bodyTemperature = (float) (0.0337 * Math.pow(y, 2) - 0.545 * y + 1.7088 * x - 0.0519 * x * y + 17.626);
    	    String bodyTemperatureLow = variableData.substring(10, 12);
    	    String bodyTemperatureHigh = variableData.substring(12, 14);
    	    bodyTemperature = Integer.parseInt(bodyTemperatureHigh + bodyTemperatureLow, 16)* 0.005f;
    	    
    	    System.out.println("Calculated Body Temperature (z): " +  bodyTemperature);
    	    break;

     case 0x04: // 血壓與血氧
         String bloodOxygenHex = variableData.substring(2, 4);
         bloodOxygen = Integer.parseInt(bloodOxygenHex, 16);
         

         String systolicHex = variableData.substring(4, 6);
         systolic = Integer.parseInt(systolicHex, 16);
         

         String diastolicHex = variableData.substring(6, 8);
         diastolic = Integer.parseInt(diastolicHex, 16);
        
         String restingHeartRateHex = variableData.substring(8, 10);
         int restingHeartRate = Integer.parseInt(restingHeartRateHex, 16);
        
         log.info("自定義標記 0x04 - 血壓與血氧: BloodOxygen={}, Systolic={}, Diastolic={}, RestingHR={}",
                  bloodOxygen, systolic, diastolic, restingHeartRate);
         break;

     case 0x0A: // 氣壓數據
         String pressureIntLow = variableData.substring(2, 4);
         String pressureIntMid = variableData.substring(4, 6);
         String pressureIntHigh = variableData.substring(6, 8);
         int pressureInt = Integer.parseInt(pressureIntHigh + pressureIntMid + pressureIntLow, 16);

         String pressureDecimal = variableData.substring(8, 10);
         float pressure = pressureInt + Integer.parseInt(pressureDecimal, 16) / 100.0f;
       
         log.info("自定義標記 0x0A - 氣壓數據: Pressure={}", pressure);
         break;

     default:
         log.warn("未知的自定義標記: 0x{}", String.format("%02X", customTag));
         break;
 }

        // - 位元組 19 (電池電量)
        String batteryHex = rawData.substring(38, 40);
        byte battery = (byte) Integer.parseInt(batteryHex, 16);
        System.out.println("電池電量: " + battery + "%");

          boolean still = !bit6; // 靜止
          byte state = bit5AsByte; // 配戴狀態
//        float bodyTemperature = getBodyTemperature(rawData); // 體溫
          boolean sos = bit4; // SOS
//        Integer bloodOxygen = getBloodOxygen(rawData); // 血氧
//        Integer calorie = getCalorie(step); // 卡路里
//        Integer systolic = getSystolic(rawData); // 收縮壓
//        Integer diastolic = getDiastolic(rawData); // 舒張壓
          boolean fallOff = bit1; // 跌倒

        if (state == 1) {
            Bv10tRequest request = new Bv10tRequest();
              request.setGatewayMac(deviceDataBo.getGatewayMac());
              request.setRssi(deviceDataBo.getRssi());
              request.setDeviceMac(deviceMac);
              request.setDeviceModelName(deviceModelName);
              request.setState(state);
              request.setBattery(battery);
              request.setBodyTemperature(bodyTemperature);
              request.setHeartRate(heartRate);
              request.setTime(time);
              request.setStep(step);
              request.setSos(sos);
              request.setBloodOxygen(bloodOxygen);
              request.setCalorie(calorie);
              request.setSystolic(systolic);
              request.setDiastolic(diastolic);
              request.setFallOff(fallOff);
              request.setStill(still);
              log.info("Bv10t Request: {}", request);
              return request;
              } else {
             Bv10tRequest request = new Bv10tRequest();
              request.setGatewayMac(deviceDataBo.getGatewayMac());
              request.setRssi(deviceDataBo.getRssi());
              request.setDeviceMac(deviceMac);
              request.setTime(time);
              request.setDeviceModelName(deviceModelName);
              request.setStep(step);
              request.setState(state);
              request.setBattery(battery);
              request.setSos(sos);
              request.setFallOff(fallOff);
              request.setStill(still);
            log.info("Bv10t Request (未配戴): {}", request);
            return request;
        }
    }
}
