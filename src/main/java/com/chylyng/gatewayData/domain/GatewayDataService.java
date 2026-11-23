package com.chylyng.gatewayData.domain;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.chylyng.deviceData.DeviceDataBo;
import com.chylyng.deviceData.DeviceDataRequest;
import com.chylyng.deviceData.DeviceDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GatewayDataService {
	@Autowired
	private DeviceDataService deviceDataService;
	@Autowired
	private ObjectMapper objectMapper;
	@Value("${eleploCore.url}")
	private String eleploCoreUrl;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private DeviceDataSender deviceDataSender;
	

	public void findDeviceModelFromEleploCore(String gatewayModel, Map<String, DeviceDataBo> deviceDataMap) {
		CheckMacRequestBody checkMacRequestBody = new CheckMacRequestBody(deviceDataMap);
		ResponseEntity<String> response = restTemplate.postForEntity(URI.create(eleploCoreUrl + "/api/v2/gatewayParser/checkMac"), checkMacRequestBody, String.class);
		String responseString = response.getBody();
//		log.info("responseString = " + responseString+"\n");
		System.out.println("responseString = " + responseString);
		try {
			CheckMacResponse checkMacResponse = objectMapper.readValue(responseString, CheckMacResponse.class); //變為物件，並且得知每個設備的型號名稱
			log.info("checkMacResponse = " + checkMacResponse+"\n");
			System.out.println("checkMacResponse = " + checkMacResponse);
			
			ifGatewayIsRegeistedThenSend(checkMacResponse, deviceDataMap);  //確認是否已註冊
		} catch (JsonProcessingException e) {
			throw new RuntimeException();
		}

	}

	private void ifGatewayIsRegeistedThenSend(CheckMacResponse checkMacResponse, Map<String, DeviceDataBo> deviceDataMap) {
		if (checkMacResponse.getGatewayRegeisted()) {
			List<DeviceInfoBo> deviceInfoList = checkMacResponse.getDeviceInfoList(); //這個是從詢問Eleplo core 所回傳的資訊（每個設備MAC，對應的模組名稱）
			deviceInfoList.forEach(deviceInfoBo -> setDeviceModelName(deviceDataMap, deviceInfoBo)); //把這些資訊，加入到DeviceDataMap當中
			deviceDataMap.forEach((deviceMac, deviceDataBo) -> {
				Optional<DeviceDataRequest> deviceDataRequestOpt = deviceDataService.parseDeviceData(deviceDataBo);    //依據型號進行處理
				//要轉換成每一個設備專屬的資料(DeviceDataRequest)，源頭的DeviceDataRequest是一樣的，但是繼承下來會有不同的生理資訊欄位
				if (deviceDataRequestOpt.isPresent()) {
					deviceDataSender.send(deviceDataRequestOpt.get());
				}
			}

			);
		}
	}

	private void setDeviceModelName(Map<String, DeviceDataBo> deviceDataMap, DeviceInfoBo deviceInfoBo) {
		String deviceMac = deviceInfoBo.getDeviceMac();
		String deviceModelName = deviceInfoBo.getDeiveModelName();
		
		Optional<DeviceDataBo> deviceDataBoOpt = Optional.ofNullable(deviceDataMap.get(deviceMac));
		if (deviceDataBoOpt.isPresent()) {
			DeviceDataBo deviceDataBo = deviceDataBoOpt.get();
			deviceDataBo.setDeviceModelName(deviceModelName);
		}
	}

}
