package com.chylyng.receiver.mqtt;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MqttDeviceDataService {
	@Autowired
    private RestTemplate restTemplate;

    @Value("${eleploCore.url}")
    private String coreUrl;

    public void send(GP21Request gP21Request) {

        try {

            restTemplate.postForEntity(
                    URI.create(coreUrl + "/api/v2/gatewayParser/getDeviceVo"),
                    gP21Request,
                    String.class
            );

        } catch (Exception e) {

        	 System.err.println("API送出失敗: " + e.getMessage());
        	    e.printStackTrace();

        }
    }
}


