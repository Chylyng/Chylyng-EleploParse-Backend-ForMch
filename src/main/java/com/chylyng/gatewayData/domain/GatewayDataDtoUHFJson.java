package com.chylyng.gatewayData.domain;

import java.util.List;

import com.chylyng.gatewayData.controller.BluetoothData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GatewayDataDtoUHFJson {
	private String readerMac;
	private List<TagIdDto> deviceTagIdList;

}
