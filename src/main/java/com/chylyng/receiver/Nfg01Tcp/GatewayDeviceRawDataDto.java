package com.chylyng.receiver.Nfg01Tcp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GatewayDeviceRawDataDto {
	private String gatewayMac;
    private String deviceMac;
    private String deviceModelName;
}
