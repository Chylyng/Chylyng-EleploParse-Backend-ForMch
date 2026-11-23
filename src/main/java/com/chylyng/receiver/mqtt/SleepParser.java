package com.chylyng.receiver.mqtt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SleepParser {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MqttDeviceDataService mqttDeviceDataService;

	public void parse(String deviceMac, String msg) throws Exception {

		// 原始 JSON → Sleep DTO
		SleepDataDto dto = objectMapper.readValue(msg, SleepDataDto.class);

		System.out.println("sleep raw = " + dto.getAckSleepData());
		System.out.println("sleep time = " + dto.getSleeptime());
		System.out.println("sleep type = " + dto.getType());

		// 建立 GP21Request（只塞睡眠資料）
		GP21Request req = new GP21Request();
		req.setDeviceMac(deviceMac);
		req.setDeviceModelName("GP21");
		if (dto.getSleeptime() != null && dto.getType() != null) {

			String[] parts = dto.getSleeptime().split(",");
			Integer type = Integer.valueOf(dto.getType());

			// type = 1 → 三項
			if (type == 1 && parts.length >= 3) {
				req.setTotal(Integer.valueOf(parts[0])); // minutes
				req.setDeep(Integer.valueOf(parts[1]));
				req.setLight(Integer.valueOf(parts[2]));
			}

			// type = 2 → 四項（眼動睡不需要就略過）
			if (type == 2 && parts.length >= 3) {
				req.setTotal(Integer.valueOf(parts[0]));
				req.setDeep(Integer.valueOf(parts[1]));
				req.setLight(Integer.valueOf(parts[2]));
			}
		}
		mqttDeviceDataService.send(req);
		log.info("gP21Request", req);

	}
	
	public void setObjectMapper(ObjectMapper objectMapper) {
	    this.objectMapper = objectMapper;
	}

	public void setMqttDeviceDataService(MqttDeviceDataService mqttDeviceDataService) {
	    this.mqttDeviceDataService = mqttDeviceDataService;
	}

}
