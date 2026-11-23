package com.chylyng.gatewayData.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CheckDeviceTaskResponse {
	private long id;
	private Integer taskType;
	private Integer deviceType;
	private String deviceMac;
	private String content;
	

}
