package com.chylyng.receiver.mqtt;

import lombok.Data;

@Data
public class AckMessage {
	private String ackSendMsg; // ok
    private String dev;        // msgid,mac

}
