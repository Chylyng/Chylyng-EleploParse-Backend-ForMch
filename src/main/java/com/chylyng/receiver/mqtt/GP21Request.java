package com.chylyng.receiver.mqtt;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class GP21Request extends DeviceDataRequest{
	private Byte battery;
	private Integer step;
	private Byte state;// 0為未配戴 1為配戴
    private Short  heart;
    private Short calorie;
    private Integer bloodOxygen;
    private Float bodyTemperature;
    private Integer systolic;
    private Integer diastolic;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    
 // 睡眠資料
    private Integer total;   // 總睡眠時間（分鐘）
    private Integer deep;    // 深睡
    private Integer light;   // 淺睡
}
