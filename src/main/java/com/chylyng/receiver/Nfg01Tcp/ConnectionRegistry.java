package com.chylyng.receiver.Nfg01Tcp;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 追蹤目前所有連線中的 Gateway 設備狀態
 * 供 REST API 查詢使用,方便掌握 20 台設備目前的連線狀況
 */
@Component
public class ConnectionRegistry {

    private final Map<String, DeviceInfo> connections = new ConcurrentHashMap<>();

    public void register(String clientKey, String remoteIp, int remotePort) {
        DeviceInfo info = new DeviceInfo(remoteIp, remotePort, LocalDateTime.now());
        connections.put(clientKey, info);
    }

    public void unregister(String clientKey) {
        connections.remove(clientKey);
    }

    public void recordDataReceived(String clientKey, int byteCount) {
        DeviceInfo info = connections.get(clientKey);
        if (info != null) {
            info.lastDataAt = LocalDateTime.now();
            info.totalBytesReceived.addAndGet(byteCount);
            info.messageCount.incrementAndGet();
        }
    }

    public Map<String, DeviceInfo> getAllConnections() {
        return connections;
    }

    public int getConnectionCount() {
        return connections.size();
    }

    /**
     * 單一設備連線資訊
     */
    public static class DeviceInfo {
        public final String remoteIp;
        public final int remotePort;
        public final LocalDateTime connectedAt;
        public volatile LocalDateTime lastDataAt;
        public final AtomicLong totalBytesReceived = new AtomicLong(0);
        public final AtomicLong messageCount = new AtomicLong(0);

        public DeviceInfo(String remoteIp, int remotePort, LocalDateTime connectedAt) {
            this.remoteIp = remoteIp;
            this.remotePort = remotePort;
            this.connectedAt = connectedAt;
            this.lastDataAt = connectedAt;
        }
    }
}

