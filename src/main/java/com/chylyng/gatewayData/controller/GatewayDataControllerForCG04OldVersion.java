package com.chylyng.gatewayData.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chylyng.deviceData.DeviceDataBo;
import com.chylyng.gatewayData.domain.GatewayDataConverterCg04;
import com.chylyng.gatewayData.domain.GatewayDataService;

/**
 * @author Phil Chen
 * @deprecated 早期出貨的gateway有使用到這個api請注意
 */
@RestController
public class GatewayDataControllerForCG04OldVersion {
	@Autowired
	private GatewayDataConverterCg04 cg04DataConverter;
	@Autowired
	private GatewayDataService gatewayDataService;

	@RequestMapping("/CG04")
	@Async
	@Deprecated
	public void cg04(@RequestBody String gatewayDataString) { //華成白色小台的
		HashMap<String, DeviceDataBo> deviceDataMap = cg04DataConverter.parseToDeviceDataMap(gatewayDataString);
		gatewayDataService.findDeviceModelFromEleploCore("CG04 old", deviceDataMap);
	}

}
