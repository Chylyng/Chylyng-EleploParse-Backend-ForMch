package com.chylyng.deviceData;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chylyng.hardware.BV01M.domain.Bv01mConverter;
import com.chylyng.hardware.BV03ht.domain.BV03htConverter;
import com.chylyng.hardware.BV04.domain.BV04tConverter;
import com.chylyng.hardware.BV05.domain.BV05Converter;
import com.chylyng.hardware.BV06.domain.Bv06Converter;
import com.chylyng.hardware.Bv01.domain.Bv01Converter;
import com.chylyng.hardware.Bv02i.domain.Bv02iConverter;
import com.chylyng.hardware.Bv02m.domain.Bv02mConverter;
import com.chylyng.hardware.Bv06tBv09t.domain.Bv06tBv09tConverter;
import com.chylyng.hardware.Bv07Bv08.domain.Bv07Bv08Converter;
import com.chylyng.hardware.Bv09.domain.Bv09Converter;
import com.chylyng.hardware.Bv09sBv17s.domain.Bv09sBv17sConverter;
import com.chylyng.hardware.Bv10b.domain.Bv10bConverter;
import com.chylyng.hardware.Bv10t.domain.Bv10tConverter;
import com.chylyng.hardware.Bv11c.domain.Bv11cConverter;
import com.chylyng.hardware.Bv19uBv17i.domain.Bv19uBv17iConverter;
import com.chylyng.hardware.Bv30xBv30s.domain.Bv30xBV30sConverter;
import com.chylyng.hardware.Bv7c.domain.Bv7cConverter;
import com.chylyng.hardware.Bv8e.domain.Bv8eConverter;
import com.chylyng.hardware.GP03lo.domain.GP03loConverter;
import com.chylyng.hardware.GP18bl.domain.GP18blConverter;
import com.chylyng.hardware.GP18lo.domain.GP18loConverter;
import com.chylyng.hardware.GP86lo.domain.GP86loConverter;
import com.chylyng.hardware.GeneralUHF.domain.GeneralUHFConverter;
import com.chylyng.hardware.SSW.domain.SswConverter;
import com.chylyng.hardwareBD04s.domain.BD04sConverter;
import com.chylyng.hardwareBH13p.domain.BH13pConverter;
import com.chylyng.hardwareBu3120A.domain.Bu3120AConverter;
import com.chylyng.hardwareBu3200A.domain.Bu3200AConverter;

@Service
public class DeviceDataService {
	@Autowired
	private Bv7cConverter bv7cConverter;
	@Autowired
	private Bv06tBv09tConverter bv06And09tConverter;
	@Autowired
	private Bv09sBv17sConverter bv09sAnd17sConverter;
	@Autowired
	private Bv19uBv17iConverter bv19uBv17iConverter;
	@Autowired
	private Bv30xBV30sConverter bv30xBV30sConverter;
	@Autowired
	private Bv10bConverter bv10bConverter;
	@Autowired
	private Bv01Converter bv01Converter;
	@Autowired
	private GP03loConverter gP03loConverter;
	@Autowired
	private BV03htConverter bV03htConverter;
	@Autowired
	private GeneralUHFConverter generalUHFConverter;
	@Autowired
	private Bv02iConverter bv02iConverter;
	@Autowired
	private Bu3200AConverter bu3200AConverter;
	@Autowired
	private Bv8eConverter bv8eConverter;
	@Autowired
	private Bv11cConverter bv11cConverter;
	@Autowired
	private GP18loConverter gP18loConverter;
	@Autowired
	private BV04tConverter bV04tConverter;
	@Autowired
	private Bu3120AConverter bu3120AConverter;
	@Autowired
	private GP86loConverter gP86loConverter;
	@Autowired
	private Bv01mConverter bv01mConverter;
	@Autowired
	private Bv10tConverter bv10tConverter;
	@Autowired
	private SswConverter sswConverter;
	@Autowired
	private GP18blConverter GP18blConverter;
	@Autowired
	private Bv02mConverter bv02mConverter;
	@Autowired
	private Bv06Converter bv06Converter;
	@Autowired
	private Bv07Bv08Converter bv07Bv08Converter;
	@Autowired
	private Bv09Converter bv09Converter;
	@Autowired
	private BD04sConverter bd04sConverter;

	@Autowired
	private BH13pConverter bh13pConverter;
	
	@Autowired
	private BV05Converter bV05Converter;

	public Optional<DeviceDataRequest> parseDeviceData(DeviceDataBo deviceDataBo) {
		String deviceModelName = deviceDataBo.getDeviceModelName();
		System.out.println("----->>>>>>!!!deviceModelName:" + deviceModelName);
		if (deviceModelName != null) {
			switch (deviceModelName) {
			case "BV05":
	            return Optional.ofNullable(bV05Converter.convert(deviceDataBo));
	        case "BD04s":
	            return Optional.ofNullable(bd04sConverter.convert(deviceDataBo));

	        case "BH13p":
	            return Optional.ofNullable(bh13pConverter.convert(deviceDataBo));

			case "BV09":
				return Optional.ofNullable(bv09Converter.convert(deviceDataBo));
			case "BV07":
			case "BV08":
				return Optional.ofNullable(bv07Bv08Converter.convert(deviceDataBo));
			case "BV06":
				return Optional.ofNullable(bv06Converter.convert(deviceDataBo));
			case "GP18bl":
				return Optional.ofNullable(GP18blConverter.convert(deviceDataBo));
			case "SSWatchBT":
				return Optional.ofNullable(sswConverter.convert(deviceDataBo));
			case "BV10t":
				return Optional.ofNullable(bv10tConverter.convert(deviceDataBo));
			case "GP86lo":
				return Optional.ofNullable(gP86loConverter.convert(deviceDataBo));
			case "Bu3120A":
				return Optional.ofNullable(bu3120AConverter.convert(deviceDataBo));
			case "BV04":
				return Optional.ofNullable(bV04tConverter.convert(deviceDataBo));
			case "GP18lo":
				return Optional.ofNullable(gP18loConverter.convert(deviceDataBo));
			case "BV11c":
				return Optional.ofNullable(bv11cConverter.convert(deviceDataBo));
			case "BV8e":
				return Optional.ofNullable(bv8eConverter.convert(deviceDataBo));
			case "Bu3200A":
				return Optional.ofNullable(bu3200AConverter.convert(deviceDataBo));
			case "BV02i":
				return Optional.ofNullable(bv02iConverter.convert(deviceDataBo));
			case "GeneralUHF":
				return Optional.ofNullable(generalUHFConverter.convert(deviceDataBo));
			case "BV03ht":
				return Optional.ofNullable(bV03htConverter.convert(deviceDataBo));
			case "GP03lo":
				return Optional.ofNullable(gP03loConverter.convert(deviceDataBo));
			case "BV01M":
				return Optional.ofNullable(bv01mConverter.convert(deviceDataBo));
			case "BV02M":
				return Optional.ofNullable(bv02mConverter.convert(deviceDataBo));
			case "BV7c":
				return Optional.ofNullable(bv7cConverter.convert(deviceDataBo));
			case "BV06t":
			case "BV09t":
				return Optional.ofNullable(bv06And09tConverter.convert(deviceDataBo));
			case "BV09s":
			case "BV17s":
				return Optional.ofNullable(bv09sAnd17sConverter.convert(deviceDataBo));
			case "BV19u":
			case "BV17i":
				return Optional.ofNullable(bv19uBv17iConverter.convert(deviceDataBo));
			case "BV30x":
				return Optional.ofNullable(bv30xBV30sConverter.convert(deviceDataBo));
			case "BV10b":
				return Optional.ofNullable(bv10bConverter.convert(deviceDataBo));
			case "BV01":
				return Optional.ofNullable(bv01Converter.convert(deviceDataBo));

			}
		}
		return Optional.empty();
	}
}
