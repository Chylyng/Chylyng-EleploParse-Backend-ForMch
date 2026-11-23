package com.chylyng.gatewayData.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @NoArgsConstructor
public class GatewayJsonBH13pAndBD04s {
	
	    private String timestamp;   // ISO8601
	    private String type;        // Gateway / Device（BH13p 不一定有 type）
	    private String mac;         // deviceMac or gatewayMac

	    // BD04s fields
	    private Integer rssi;
	    private String rawData;

	    // BH13p fields
	    private String sn;
	    private Radar radar;

	    // gateway nums
	    private Integer nums;

	    @Getter
	    @Setter
	    @ToString
	    public static class Radar {
	        private String type;    // flow / other types
	        private Flow flow;
	    }

	    @Getter
	    @Setter
	    @ToString
	    public static class Flow {
	        private Integer enter;
	        private Integer exit;
	    }
	

}
