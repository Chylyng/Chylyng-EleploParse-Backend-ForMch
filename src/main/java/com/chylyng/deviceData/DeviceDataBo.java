package com.chylyng.deviceData;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceDataBo {
	private String gatewayMac;
	private String deviceMac;
	private LocalDateTime time;
	private Byte rssi;
	private String rawData;
	private String deviceModelName;
	private int companyId; //這是新增加了，為了讓Eleplo Receiver Cloud能識別是哪一個公司
	private Float temperature;
	private Integer humidity;
	private String areaName;
	
	 // For BH13p
    private String sn;
    private String radarType;

    private RadarDto radar;

    @Getter @Setter @ToString
    public static class RadarDto {
        private Flow flow;
        @Getter @Setter @ToString
        public static class Flow {
            private Integer enter;
            private Integer exit;
        }
    }
	
}
