package com.chylyng.receiver.mqtt;

import lombok.Data;

@Data
public class SleepDataDto {
	private String ackSleepData;
    private String sleeptime;
    private String type;
    private String pack;
    private String total;
    private String date;
    private String dev;
}
