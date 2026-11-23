package com.chylyng.gatewayData.domain;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatewayDataDtoG1 {


	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",timezone="GMT")    //這個地方很重要
	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime timestamp;

	private String mac;
	private String bleName;    //這是原廠的BleName, 非公司定義的產品名稱，所以, 後續還是需要到Server上抓Model名稱
	private Byte rssi;
	private String rawData;
	private String type;
	
	//這兩個是非必要欄位，原本會發生錯誤，後來加入JsonIgnoreProperties(ignoreUnknown = true)，就搞定
	//private String gatewayFree;  
	//private String gatewayLoad;
}
