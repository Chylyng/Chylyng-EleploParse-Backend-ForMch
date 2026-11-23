package com.chylyng.hardwareBD04s.domain;

import org.springframework.stereotype.Component;

import com.chylyng.deviceData.DeviceDataBo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BD04sConverter {

    public BD04sRequest convert(DeviceDataBo bo) {

        String raw = bo.getRawData();
        if (raw == null || raw.length() < 14) {  // 至少 14 bytes (28 hex chars)
            log.warn("BD04s rawData 長度不足: {}", raw);
            return null;
        }

        // PDF offset 9 = hex char 18~20
        int blockId = Integer.parseInt(raw.substring(18, 20), 16);
        if (blockId != 0x1F) {
            log.warn("BD04s blockId 錯誤：{}, raw={}", blockId, raw);
            return null;
        }

        // offset 10
//        int status = Integer.parseInt(raw.substring(20, 22), 16);
//        boolean doorOpen  = (status & 0b00000001) != 0;
//        boolean tamper    = (status & 0b00000010) != 0;
//        boolean triggered = (status & 0b00000100) != 0;
        
     // offset 10
        int status = Integer.parseInt(raw.substring(20, 22), 16);
        boolean doorOpen  = (status & 0x80) != 0;  // bit7: open=1 / close=0
        boolean tamper    = (status & 0x40) != 0;  // bit6: release=1(告警) / press=0(正常)
        boolean triggered = (status & 0x20) != 0;  // bit5: triggered=1

        // offset 11, 12, 13
        int openCount   = Integer.parseInt(raw.substring(22, 24), 16);
        int closeCount  = Integer.parseInt(raw.substring(24, 26), 16);
        int tamperCount = Integer.parseInt(raw.substring(26, 28), 16);

        // 填寫 Request
        BD04sRequest req = new BD04sRequest();
        req.setDeviceMac(bo.getDeviceMac());
        req.setGatewayMac(bo.getGatewayMac());
        req.setDeviceModelName(bo.getDeviceModelName());
        req.setRssi(bo.getRssi());
        req.setTime(bo.getTime());

        req.setDoorOpen(doorOpen);
        req.setOpenCount(openCount);
        req.setCloseCount(closeCount);
        req.setTamper(tamper);
        req.setTamperCount(tamperCount);
        req.setTriggered(triggered);

        log.info("BD04s parsed: {}", req);
        return req;
    }
}
