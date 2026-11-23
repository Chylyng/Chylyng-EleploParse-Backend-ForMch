package com.chylyng.deviceData;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TaskDeviceDataRequest {
	private String gatewayMac;
	private String Mac;
	private long id;
}
