package com.chylyng.hardwareBH13p.domain;

import com.chylyng.deviceData.DeviceDataRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class BH13pRequest extends DeviceDataRequest {

    private Integer enter;
    private Integer exit;

    private String sn;           // 裝置 SN
    private String radarType;    // flow / presence / motion 等
}
