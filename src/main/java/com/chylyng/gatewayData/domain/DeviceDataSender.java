package com.chylyng.gatewayData.domain;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DeviceDataSender {
	@Value("${eleploCore.url}")
	private String eleploCoreUrl;
	@Autowired
	private RestTemplate restTemplate;

	public void send(DeviceDataRequest deviceDataVo) {
		try {
			restTemplate.postForEntity(URI.create(eleploCoreUrl + "/api/v2/gatewayParser/getDeviceVo"), deviceDataVo,
					String.class);
		} catch (Exception e) {
			log.warn("deviceDataVo = " + deviceDataVo);
		}

	}
}
