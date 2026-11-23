package com.chylyng.receiver.mqtt;

import com.fasterxml.jackson.annotation.JsonValue;

public enum LocationMode {
	WIFI, CELL_TOWER, WIFI_AND_CELL_TOWER;

	@JsonValue
	public int toValue() {
		return ordinal();
	}
}
