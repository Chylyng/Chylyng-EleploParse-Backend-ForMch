package com.chylyng.receiver.mqtt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HealthParser {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MqttDeviceDataService mqttDeviceDataService;

    public void parse(String deviceMac, String msg) throws Exception {

        HealthDataDto dto = objectMapper.readValue(msg, HealthDataDto.class);
        HealthDataDto.AdditionMessage add = dto.getOutdoorLocationData().getAdditionMessage();

        GP21Request vo = new GP21Request();

        // ★★★★★ 把 deviceMac
        vo.setDeviceMac(deviceMac);
        vo.setDeviceModelName("GP21");
        if(add.getIsWear().equals("yes")) {
        	vo.setState((byte) 1);
        }else {
        	vo.setState((byte) 0);
        }
        vo.setBattery(toByte(add.getBattery()));
        vo.setStep(toInt(add.getStep()));
        vo.setHeart(toShort(add.getHeart()));
        vo.setBloodOxygen(add.getSpo2());
        vo.setBodyTemperature(toFloat(add.getCelsius()));
        vo.setCalorie(toShort(add.getCalorie()));

        vo.setSystolic(add.getSystolic());
        vo.setDiastolic(add.getDiastolic());

        // 自己的系統會在 DeviceDataRequest 裡補 time / gatewayMac
        mqttDeviceDataService.send(vo);
    }

    private Byte toByte(String v) {
        try {
            return v == null ? null : Byte.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer toInt(String v) {
        try {
            return v == null ? null : Integer.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    private Short toShort(Integer v) {
        try {
            return v == null ? null : v.shortValue();
        } catch (Exception e) {
            return null;
        }
    }

    private Float toFloat(String v) {
        try {
            return v == null ? null : Float.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }
}