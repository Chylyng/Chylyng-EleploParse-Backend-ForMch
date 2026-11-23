package com.chylyng.receiver.mqtt;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WifiLocationDto {
	/**
     * 例如：
     * "1c:3b:f3:da:be:3f,-58;5c:f4:ab:6f:eb:80,-66"
     */
    private String apInfo;

    /**
     * type: "0"（固定 0 = WiFi）
     */
    private String type;

    /**
     * AP 數量，例如 "2"
     */
    private String apNum;

    /**
     * e.g. "860422072110658"
     */
    private String imei;

    /**
     * 時間戳，例如 "1773683222"
     */
    private String time;

    /**
     * dev: "1773683345,50C0F0C9ED0B"
     * → 左邊是 seq（時間序號），右邊是 deviceId
     */
    private String dev;
}

