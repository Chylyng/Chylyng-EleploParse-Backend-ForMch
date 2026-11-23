package com.chylyng.gatewayData.domain;


import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CheckMacResponse {
	private Boolean gatewayRegeisted;
	private List<DeviceInfoBo> deviceInfoList;
}