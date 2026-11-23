package com.chylyng.receiver.mqtt;

import lombok.Data;

@Data
public class GoogleGeoResponse {
	
	private Double latitude;
    private Double longitude;
    private Double accuracy;

}
