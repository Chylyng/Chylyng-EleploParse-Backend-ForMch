package com.chylyng.receiver.Nfg01Tcp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供 REST API 查看目前連線中的 Gateway 設備狀態
 *
 * GET /api/devices        -> 查看所有目前連線的設備清單
 * GET /api/devices/count  -> 查看目前連線總數
 */
@RestController
public class DeviceController {

    private final ConnectionRegistry registry;

    public DeviceController(ConnectionRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/api/devices")
    public List<Map<String, Object>> listDevices() {
        List<Map<String, Object>> result = new ArrayList<>();

        registry.getAllConnections().forEach((key, info) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("clientKey", key);
            item.put("remoteIp", info.remoteIp);
            item.put("remotePort", info.remotePort);
            item.put("connectedAt", info.connectedAt.toString());
            item.put("lastDataAt", info.lastDataAt.toString());
            item.put("totalBytesReceived", info.totalBytesReceived.get());
            item.put("messageCount", info.messageCount.get());
            result.add(item);
        });

        return result;
    }

    @GetMapping("/api/devices/count")
    public Map<String, Integer> deviceCount() {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("connectedCount", registry.getConnectionCount());
        return result;
    }
}

