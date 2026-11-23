package com.chylyng.hardwareBD04s.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class BD04sRequest extends DeviceDataRequest{
	    private Boolean doorOpen;
	    private Integer openCount;
	    private Integer closeCount;
	    private Boolean tamper;//Anti 防拆
	    private Integer tamperCount;

	    private Boolean triggered; // bit2

}
