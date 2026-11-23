package com.chylyng.gatewayData.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CheckMacRequestBody {
	private String gatewayMacAddress;
	private int companyId=0;  //這是新增加了，為了讓Eleplo Receiver Cloud能識別是哪一個公司
	private List<DeviceInfoBo> deviceInfoList;
	
	
	
	
	
	

	public CheckMacRequestBody(Map<String, DeviceDataBo> deviceDataMap) {
		this.deviceInfoList = new ArrayList<DeviceInfoBo>();
		for (Entry<String, DeviceDataBo> entry : deviceDataMap.entrySet()) {
			if (this.gatewayMacAddress == null) {
				this.gatewayMacAddress = entry.getValue().getGatewayMac();
			}
			
			if (companyId==0) {
				this.companyId = entry.getValue().getCompanyId();
			}
			
			DeviceInfoBo DeviceInfoBo = new DeviceInfoBo();
			DeviceInfoBo.setDeviceMac(entry.getValue().getDeviceMac());
			this.deviceInfoList.add(DeviceInfoBo);
		}
	}

}
