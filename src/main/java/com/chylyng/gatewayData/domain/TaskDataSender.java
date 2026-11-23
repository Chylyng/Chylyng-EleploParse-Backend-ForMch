package com.chylyng.gatewayData.domain;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.chylyng.deviceData.TaskDeviceDataRequest;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TaskDataSender {
	@Value("${eleploCore.url}")
	private String eleploCoreUrl;
	@Autowired
	private RestTemplate restTemplate;

	public void send(TaskDeviceDataRequest taskDeviceDataVo) {
		try {
			restTemplate.postForEntity(URI.create(eleploCoreUrl + "/api/v1/taskforgateway/gettask"), taskDeviceDataVo,
					String.class);
		} catch (Exception e) {
			log.warn("taskDeviceDataVo = " + taskDeviceDataVo);
		}

	}

}
