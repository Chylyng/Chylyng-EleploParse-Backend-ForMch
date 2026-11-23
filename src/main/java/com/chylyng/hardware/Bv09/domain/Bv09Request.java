package com.chylyng.hardware.Bv09.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Bv09Request extends DeviceDataRequest {

	private Integer page;
	private Integer status;

	// Page 1
	private Float pm1;
	private Float pm25;
	private Float pm4;

	// Page 2
	private Float pm10;
	private Float temperature;
	private Integer humidity;

	// Page 3
	private Float voc;
	private Integer co2;
	private Float hcho;
}