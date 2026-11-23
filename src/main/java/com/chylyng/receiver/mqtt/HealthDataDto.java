package com.chylyng.receiver.mqtt;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthDataDto {

	private OutdoorLocationData outdoorLocationData;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class OutdoorLocationData {
		private String LocationData;
		private String locationTime;
		private AdditionMessage additionMessage;
	}

	@Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdditionMessage {
        private String battery;         
        private String step;
        private String isWear;
        private Integer heart;          
        private Integer spo2;           
        private String celsius;         
        private Integer systolic;
        private Integer diastolic;
        private Integer calorie;
        
    }

	

}