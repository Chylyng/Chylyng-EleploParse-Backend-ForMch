package com.chylyng.gatewayData.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GatewayDataDtoUHF { //這是轉化成可以往後傳的一個一個的結構
	private String gatewayMac;
	private LocalDateTime systemTime = LocalDateTime.now();
	private String deviceMac;
    private Float temperature;
    private Integer humidity;
    private String areaName;
    
}
