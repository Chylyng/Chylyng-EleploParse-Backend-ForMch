package com.chylyng.receiver.mqtt;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WifiInfo {
	String mac;
	Integer signal;
}
