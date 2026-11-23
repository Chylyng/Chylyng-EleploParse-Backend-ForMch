package com.chylyng.receiver.mqtt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class DeviceOnlineService {
	// ⭐新增：記錄最後心跳時間
	private final Map<String, Long> lastSeenMap = new ConcurrentHashMap<>();

	// ⭐新增：離線判定（秒）
	private static final long OFFLINE_THRESHOLD = 300; // 60秒沒消息 = 離線

	// ⭐新增：更新設備心跳
	public void updateHeartbeat(String deviceId) {
		lastSeenMap.put(deviceId, System.currentTimeMillis());
	}

	// ⭐新增：是否在線
	public boolean isOnline(String deviceId) {
		Long last = lastSeenMap.get(deviceId);
		if (last == null)
			return false;

		long diff = (System.currentTimeMillis() - last) / 1000;
		return diff <= OFFLINE_THRESHOLD;
	}

	// ⭐新增：抓全部離線設備
	public List<String> getOfflineDevices() {
		long now = System.currentTimeMillis();

		return lastSeenMap.entrySet().stream().filter(e -> (now - e.getValue()) / 1000 > OFFLINE_THRESHOLD)
				.map(Map.Entry::getKey).collect(Collectors.toList());
	}

}
