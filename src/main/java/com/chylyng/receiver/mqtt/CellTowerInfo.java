package com.chylyng.receiver.mqtt;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CellTowerInfo {
	private Integer cellId;
	private Integer locationAreaCode;
	private Integer mobileCountryCode;
	private Integer mobileNetworkCode;

}
