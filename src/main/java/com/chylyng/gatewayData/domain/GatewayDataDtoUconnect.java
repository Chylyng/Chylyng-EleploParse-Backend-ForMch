package com.chylyng.gatewayData.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GatewayDataDtoUconnect {
	private String gatewayMac;
	private String gatewayFirmwareVersion;
	private LocalDateTime systemTime = LocalDateTime.now();
	
	private String deviceMac;
	private Byte rssi;
	private String rawData;
}
