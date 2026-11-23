package com.chylyng.receiver.mqtt;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LocationDto {
	private String projectName;
	private LocationMode locationMode;
	private ArrayList<WifiInfo> wifiInfoList;
	private List<CellTowerInfo> cellTowerInfoList;

}
