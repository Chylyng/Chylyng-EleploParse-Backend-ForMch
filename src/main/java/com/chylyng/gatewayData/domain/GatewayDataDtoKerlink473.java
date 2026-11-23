package com.chylyng.gatewayData.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class GatewayDataDtoKerlink473 {
	private String gatewayMac;
	private String deviceName;
	private LocalDateTime systemTime = LocalDateTime.now();
	private String deviceMac;
	private String rawData;
	private Byte rssi;
}
