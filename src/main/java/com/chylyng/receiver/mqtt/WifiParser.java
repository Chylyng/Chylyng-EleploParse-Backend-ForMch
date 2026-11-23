package com.chylyng.receiver.mqtt;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WifiParser {

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MqttDeviceDataService mqttDeviceDataService;

	@Autowired
	private RestTemplate restTemplate;

	private static final String LOCATION_API = "http://www.gpwear.com.tw:6001/location-service/api/v1/location";

	public void parse(String deviceMac, String msg) throws Exception {

		WifiLocationDto dto = objectMapper.readValue(msg, WifiLocationDto.class);
		log.info("進行GP21Request拆解");
		System.out.println("AP Raw = " + dto.getApInfo());
		// 先預設 apNum 為 0
	    int apNum = 0;
	    try {
	        if (dto.getApNum() != null) {
	            apNum = Integer.parseInt(dto.getApNum());
	        }
	    } catch (NumberFormatException e) {
	        log.warn("apNum 不是數字: {}", dto.getApNum());
	        apNum = 0;
	    }

	    // ★★★ 只有 apNum >= 2 才解析
	    if (apNum < 2) {
	        log.warn("AP 數量不足，不進行定位計算。deviceMac={}, apNum={}", deviceMac, apNum);
	        return ;
	    }

		
		// 轉成ArrayList 列表
		ArrayList<WifiInfo> wifiList = parseWifi(dto.getApInfo());

		LocationDto locationDto = new LocationDto();
		locationDto.setProjectName("gpwear");
		locationDto.setLocationMode(LocationMode.WIFI);
		locationDto.setWifiInfoList(wifiList);
		System.out.println("locationDto = " + locationDto);

		// ★★★ 呼叫 API 並接收 GoogleGeoResponse ★★★
		GoogleGeoResponse geo = sendLocation(locationDto);

		System.out.println("GoogleGeoResponse = " + geo);
		GP21Request gP21Request = new GP21Request();
		gP21Request.setDeviceMac(deviceMac);
		gP21Request.setDeviceModelName("GP21");
		gP21Request.setLatitude(geo.getLatitude());
		gP21Request.setLongitude(geo.getLongitude());
		mqttDeviceDataService.send(gP21Request);
		log.info("gP21Request", gP21Request);

	}

	private ArrayList<WifiInfo> parseWifi(String apInfo) {

		ArrayList<WifiInfo> list = new ArrayList<>();

		if (apInfo == null || apInfo.isEmpty())
			return list;

		String[] array = apInfo.split(";");

		for (String one : array) {
			String[] parts = one.split(",");
			WifiInfo wi = new WifiInfo();
			wi.setMac(parts[0]);
			wi.setSignal(Integer.valueOf(parts[1]));
			list.add(wi);
		}
		return list;
	}

	// ★★★ 呼叫 location-service → 回傳 GoogleGeoResponse ★★★
	private GoogleGeoResponse sendLocation(LocationDto dto) throws Exception {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<LocationDto> request = new HttpEntity<>(dto, headers);

		ResponseEntity<GoogleGeoResponse> response = restTemplate.exchange(LOCATION_API, HttpMethod.POST, request,
				GoogleGeoResponse.class);

		return response.getBody();
	}
}
