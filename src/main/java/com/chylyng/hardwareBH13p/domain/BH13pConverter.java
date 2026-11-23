package com.chylyng.hardwareBH13p.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BH13pConverter {

	public BH13pRequest convert(DeviceDataBo bo) {

		if (bo.getRadar() == null || bo.getRadar().getFlow() == null) {
			log.warn("BH13p 缺少 radar flow：{}", bo);
			return null;
		}

		BH13pRequest req = new BH13pRequest();
		req.setGatewayMac(bo.getDeviceMac());
		req.setDeviceMac(bo.getDeviceMac());
		req.setGatewayMac(bo.getGatewayMac());
		req.setTime(bo.getTime());
		req.setDeviceModelName(bo.getDeviceModelName());

		// enter / exit
		req.setEnter(bo.getRadar().getFlow().getEnter());
		req.setExit(bo.getRadar().getFlow().getExit());

		// sn 與 radarType（JSON 原樣帶入）
		req.setSn(bo.getSn());
		req.setRadarType(bo.getRadarType());

		log.info("BH13p parsed: {}", req);
		return req;
	}
}