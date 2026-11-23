package com.chylyng.gatewayData.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class GatewayDataDtoThUHFJson {//將溫溼度傳進來的資料轉換成物件
	private String readerMac;
	private List<TagIdDto> deviceTagIdList;
	private List<TagTemperDto> deviceTemperature;
	private List<TagHumidDto> deviceHumidity;
}
