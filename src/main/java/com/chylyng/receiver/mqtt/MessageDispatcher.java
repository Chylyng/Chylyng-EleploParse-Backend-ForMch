package com.chylyng.receiver.mqtt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MessageDispatcher {
	 @Autowired
	    private ObjectMapper objectMapper;

	    @Autowired
	    private HealthParser healthParser;

	    @Autowired
	    private WifiParser wifiParser;

	    @Autowired
	    private SleepParser sleepParser;

	    public void dispatch(String deviceMac,String msg) {

	        try {

	            JsonNode root = objectMapper.readTree(msg);

	            if (root.has("outdoorLocationData")) {

	                healthParser.parse(deviceMac,msg);

	            } else if (root.has("apInfo")) {

	                wifiParser.parse(deviceMac,msg);

	            } else if (root.has("ackSleepData")) {

	                sleepParser.parse(deviceMac,msg);

	            }

	        } catch (Exception e) {

	            System.err.println("解析失敗: " + msg);

	        }

	    }	

}
