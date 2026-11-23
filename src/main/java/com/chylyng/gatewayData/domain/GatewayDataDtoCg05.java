package com.chylyng.gatewayData.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GatewayDataDtoCg05 {  //這是轉化成可以往後傳的一個一個的結構
	private String gatewayMac;
	private String gatewayFirmwareVersion;
	private int companyId; //這是新增加了，為了讓Eleplo Receiver Cloud能識別是哪一個公司
	private LocalDateTime systemTime = LocalDateTime.now();
	private String deviceMac;
	private Byte rssi;
	private String rawData;
}
