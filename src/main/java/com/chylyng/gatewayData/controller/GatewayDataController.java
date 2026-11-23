package com.chylyng.gatewayData.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.chylyng.deviceData.DeviceDataBo;
import com.chylyng.deviceData.TaskDeviceDataRequest;
import com.chylyng.gatewayData.domain.CheckDeviceTaskResponse;
import com.chylyng.gatewayData.domain.DeviceMessagesTokenDto;
import com.chylyng.gatewayData.domain.GatewayDataConverterCg04;
import com.chylyng.gatewayData.domain.GatewayDataConverterCg05;
import com.chylyng.gatewayData.domain.GatewayDataConverterG1;
import com.chylyng.gatewayData.domain.GatewayDataConverterKerlink473;
import com.chylyng.gatewayData.domain.GatewayDataConverterUHF;
import com.chylyng.gatewayData.domain.GatewayDataConverterUconnect;
import com.chylyng.gatewayData.domain.GatewayDataDtoCg05;
import com.chylyng.gatewayData.domain.GatewayDataDtoCg05Json;
import com.chylyng.gatewayData.domain.GatewayDataDtoKerlink473;
import com.chylyng.gatewayData.domain.GatewayDataDtoThUHFJson;
import com.chylyng.gatewayData.domain.GatewayDataDtoUHF;
import com.chylyng.gatewayData.domain.GatewayDataDtoUHFJson;
import com.chylyng.gatewayData.domain.GatewayDataDtoUconnect;
import com.chylyng.gatewayData.domain.GatewayDataService;
import com.chylyng.gatewayData.domain.GatewayJsonBH13pAndBD04s;
import com.chylyng.gatewayData.domain.TagHumidDto;
import com.chylyng.gatewayData.domain.TagIdDto;
import com.chylyng.gatewayData.domain.TagTemperDto;
import com.chylyng.gatewayData.domain.TaskDataSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.DecoderException;

import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/receiver")
@Log4j2
public class GatewayDataController {
	
	@Autowired
	private GatewayDataConverterCg04 cg04DataConverter;
	@Autowired
	private GatewayDataConverterG1 g1DataConverter;
	@Autowired
	private GatewayDataConverterCg05 cg05DataConverter;
	@Autowired
	private GatewayDataConverterUconnect uconnectDataConverter;
	@Autowired
	private GatewayDataService gatewayDataService;
	@Autowired
	private TaskDataSender taskDataSender;
	@Value("${eleploCore.url}")
	private String eleploCoreUrl;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ObjectMapper objectMapper;
	private Object checkDeviceTaskResponse;
	@Autowired
	private GatewayDataConverterKerlink473 Kerlink473DataConverter;
	@Autowired
	private GatewayDataConverterUHF UHFDataConverter;

	private String lastGatewayMac = null;
	
	// 在 class 最上面加入這個 static 欄位，程式啟動時載入一次字型檔，
	// 避免每次呼叫 generateEpaperBinPacket() 都重新讀檔 (效能差，也容易踩到 classpath 問題)
	private static Font kaiuBaseFont = loadKaiuFont();
	 
	private static Font loadKaiuFont() {
	    try (InputStream fontStream = GatewayDataController.class.getResourceAsStream("/fonts/kaiu.ttf")) {
	        if (fontStream == null) {
	            log.error("[FONT] 找不到 /fonts/kaiu.ttf，請確認檔案放在 src/main/resources/fonts/kaiu.ttf，" +
	                    "且已重新編譯/打包 (mvn/gradle build)，不是只有存檔在 IDE 裡");
	            return null;
	        }
	        Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
	        log.info("[FONT] 成功載入自訂字型: {}", font.getFontName());
	        return font;
	    } catch (Exception e) {
	        log.error("[FONT] 載入 kaiu.ttf 失敗，將 fallback 使用系統 SansSerif (中文可能無法正確顯示)", e);
	        return null;
	    }
	}
	
	// 已推送過的目標 MAC 集合，確保同一片電子紙只自動推送一次
    // 需要允許之後更新內容再推送時，可改用 Caffeine Cache 搭配 TTL，
    // 或提供一支手動 reset 的 API 把對應 MAC 從這個 Set 移除
    private static final Set<String> sentMacSet = ConcurrentHashMap.newKeySet();
	
	
	
	
    //人流門窗設備
	@PostMapping("/bh13pAndBd04sJson")
	@Async
	public void receiveBh13pAndBd04sJson(@RequestBody String body) throws IOException {

		log.info("收到 bh13pAndBd04sJson 原始資料: {}", body);

		ObjectMapper mapper = new ObjectMapper();

		// -------------------------
		// 單筆 JSON（BH13p 單筆）
		// -------------------------
		if (body.trim().startsWith("{")) {
			GatewayJsonBH13pAndBD04s item = mapper.readValue(body, GatewayJsonBH13pAndBD04s.class);

			processJsonItem(item);
			return;
		}

		// -------------------------
		// 多筆 JSON（BD04s 或混合 array）
		// -------------------------
		if (body.trim().startsWith("[")) {
			List<GatewayJsonBH13pAndBD04s> list = mapper.readValue(body,
					new TypeReference<List<GatewayJsonBH13pAndBD04s>>() {
					});
			list.forEach(this::processJsonItem);
			return;
		}

		log.warn("bh13pAndBd04sJson 收到非 JSON 格式資料，忽略");
	}

	/**
	 * JSON → DeviceDataBo → EleploCore
	 */
	private void processJsonItem(GatewayJsonBH13pAndBD04s item) {

		// 1. Gateway 訊號：只用來記錄 gatewayMac
		if ("Gateway".equalsIgnoreCase(item.getType())) {
			this.lastGatewayMac = item.getMac();
			log.info("記錄 Gateway MAC: {}", this.lastGatewayMac);
			return;
		}

		// 2. 建立可送入 Eleplo Core 的 DeviceDataBo
		DeviceDataBo bo = new DeviceDataBo();

		bo.setDeviceMac(item.getMac());
		bo.setGatewayMac(this.lastGatewayMac);
		if(this.lastGatewayMac==null) {
			bo.setGatewayMac(item.getMac());
		}

		// timestamp
		if (item.getTimestamp() != null) {
			bo.setTime(LocalDateTime.now());
		}

		// BD04s fields
		bo.setRawData(item.getRawData());
		if (item.getRssi() != null) {
			bo.setRssi(item.getRssi().byteValue());
		}

		// BH13p fields
		bo.setSn(item.getSn());

		if (item.getRadar() != null) {
			DeviceDataBo.RadarDto radarDto = new DeviceDataBo.RadarDto();
			radarDto.setFlow(new DeviceDataBo.RadarDto.Flow());

			if (item.getRadar().getFlow() != null) {
				radarDto.getFlow().setEnter(item.getRadar().getFlow().getEnter());
				radarDto.getFlow().setExit(item.getRadar().getFlow().getExit());
			}

			bo.setRadarType(item.getRadar().getType());
			bo.setRadar(radarDto);
		}

		// 3. EleploCore 接收需要的是 Map<String, DeviceDataBo>
		HashMap<String, DeviceDataBo> map = new HashMap<>();
		map.put(bo.getDeviceMac(), bo);

		// 4. 丟給 EleploCore 讓它判斷型號 → parse → send()
		gatewayDataService.findDeviceModelFromEleploCore("BH13p_BD04s_JSON", map);

		log.info("bh13pAndBd04sJson 已送入 EleploCore 流程, deviceMac={}", bo.getDeviceMac());
	}

	// 純粹測試
	@RequestMapping("/easytest")
	@Async
	public void easytest(@RequestBody String gatewayDataString) {
		String info = "收到easytest ===>>> " + gatewayDataString;
		System.out.println(info);
	}

	// 華成白色小台的
	@RequestMapping("/cg04")
	@Async
	public void cg04(@RequestBody String gatewayDataString) {
		String info = "收到cg04的rawData ===>>> " + gatewayDataString;
		if (gatewayDataString != null) {
			log.info(info);
			HashMap<String, DeviceDataBo> deviceDataMap = cg04DataConverter.parseToDeviceDataMap(gatewayDataString);
			gatewayDataService.findDeviceModelFromEleploCore("CG04", deviceDataMap);
		} else {
			System.out.println(info);
		}
	}

	// 雲里物里
	@RequestMapping("/g1")
	@Async
	public void g1Receive(@RequestBody String rawDataString) {

		// 目前只有宜眾在用
		/*
		 * String info = "收到g1的rawData(暫時不處理) ===>>> " + rawDataString;
		 * 
		 * 
		 * if(rawDataString!=null) { log.info(info);
		 * 
		 * 
		 * 
		 * //** 收到g1的rawData , 會有只有Gateway或是包含Device的 //**
		 * GatewayDataG1Dto跟GatewayDataCg05Dto的差別在於GatewayDataG1Dto是用Type來區分Gateway還是一般設備
		 * //===>>>
		 * [{"timestamp":"2021-09-28T05:59:25Z","type":"Gateway","mac":"AC233FC03A19",
		 * "gatewayFree":100,"gatewayLoad":0.16}] //===>>>
		 * [{"timestamp":"2021-11-01T04:21:00Z","type":"Gateway","mac":"AC233FC03A19",
		 * "gatewayFree":99,"gatewayLoad":0.15},{"timestamp":"2021-11-01T04:21:21Z",
		 * "type":"Unknown","mac":"C07EDEB98E75","bleName":"SH09U","rssi":-80,"rawData":
		 * "02010613FF0A0AC07EDEB98E756A24CC28360201CFB30006095348303955"},{"timestamp":
		 * "2021-11-01T04:21:21Z","type":"Unknown","mac":"C07EDEB98E75","bleName":
		 * "SH09U","rssi":-81,"rawData":
		 * "02010613FF0A0AC07EDEB98E756A24CC28360201CFB30006095348303955"},{"timestamp":
		 * "2021-11-01T04:21:31Z","type":"Unknown","mac":"C07EDEB98E75","bleName":
		 * "SH09U","rssi":-80,"rawData":
		 * "02010613FF0A0AC07EDEB98E756924CC283B0201CFB30006095348303955"}]
		 * 
		 * 
		 * try { //目前G1尚未支付費用，所以準備暫時停止 ObjectMapper objectMapper = new ObjectMapper();
		 * List<GatewayDataDtoG1> g1DtoList = objectMapper.readValue(rawDataString, new
		 * TypeReference<List<GatewayDataDtoG1>>(){});
		 * 
		 * HashMap<String, DeviceDataBo> deviceDataMap =
		 * g1DataConverter.parseToDeviceDataMap(g1DtoList);
		 * gatewayDataService.findDeviceModelFromEleploCore("G1", deviceDataMap);
		 * }catch(Exception e) { //e.printStackTrace();
		 * log.info("Error in g1:"+e.getMessage()); }
		 * 
		 * 
		 * }else { System.out.println(info); }
		 */

	}

	// 雲里物里
	@PostMapping("/g1syncmode")
	public void g1newReceive(@RequestBody String rawDataString) throws JsonMappingException, JsonProcessingException {

		String info = "收到g1(new)的rawData (暫時不處理)===>>> " + rawDataString;
		if (rawDataString != null) {
			log.info(info);

			/*
			 * //** 收到g1的rawData , 會有只有Gateway或是包含Device的 //**
			 * GatewayDataG1Dto跟GatewayDataCg05Dto的差別在於GatewayDataG1Dto是用Type來區分Gateway還是一般設備
			 * //===>>>
			 * [{"timestamp":"2021-09-28T05:59:25Z","type":"Gateway","mac":"AC233FC03A19",
			 * "gatewayFree":100,"gatewayLoad":0.16}] //===>>>
			 * [{"timestamp":"2021-11-01T04:21:00Z","type":"Gateway","mac":"AC233FC03A19",
			 * "gatewayFree":99,"gatewayLoad":0.15},{"timestamp":"2021-11-01T04:21:21Z",
			 * "type":"Unknown","mac":"C07EDEB98E75","bleName":"SH09U","rssi":-80,"rawData":
			 * "02010613FF0A0AC07EDEB98E756A24CC28360201CFB30006095348303955"},{"timestamp":
			 * "2021-11-01T04:21:21Z","type":"Unknown","mac":"C07EDEB98E75","bleName":
			 * "SH09U","rssi":-81,"rawData":
			 * "02010613FF0A0AC07EDEB98E756A24CC28360201CFB30006095348303955"},{"timestamp":
			 * "2021-11-01T04:21:31Z","type":"Unknown","mac":"C07EDEB98E75","bleName":
			 * "SH09U","rssi":-80,"rawData":
			 * "02010613FF0A0AC07EDEB98E756924CC283B0201CFB30006095348303955"}]
			 * 
			 * 
			 * try { //目前G1尚未支付費用，所以準備暫時停止 ObjectMapper objectMapper = new ObjectMapper();
			 * List<GatewayDataDtoG1> g1DtoList = objectMapper.readValue(rawDataString, new
			 * TypeReference<List<GatewayDataDtoG1>>(){});
			 * 
			 * HashMap<String, DeviceDataBo> deviceDataMap =
			 * g1DataConverter.parseToDeviceDataMap(g1DtoList);
			 * gatewayDataService.findDeviceModelFromEleploCore("G1", deviceDataMap);
			 * }catch(Exception e) { //e.printStackTrace();
			 * log.info("Error in g1:"+e.getMessage()); }
			 * 
			 */
		} else {
			System.out.println(info);
		}
	}

	// Eleplo Receiver Cloud
	// 華成白色大台的(cg05-Json格式版本)
	@RequestMapping("/cg05json")
	@Async
	public void cg05jsonReceive(@RequestBody String rawDataString)
			throws JsonMappingException, JsonProcessingException {
		String info = "收到CG05-Json/Eleplo Receiver Cloud版的rawData ===>>> " + rawDataString;

		if (rawDataString != null) {
			log.info(info);

			ObjectMapper objectMapper = new ObjectMapper();
			// 這個版本的轉換是Json版本
			GatewayDataDtoCg05Json cg05Dto = objectMapper.readValue(rawDataString, GatewayDataDtoCg05Json.class);
			// log.info("json轉換成功 ===>>>" + cg05Dto);

			System.out.println("準備DtoList");
			ArrayList<GatewayDataDtoCg05> cg05DtoList = new ArrayList<GatewayDataDtoCg05>();

			String gatewayMac = cg05Dto.getBluetoothMac();
			String gatewayVersion = cg05Dto.getFirmwareVersion();
			int companyId = cg05Dto.getCompanyId(); // 這樣才能知道要掛在哪一個公司下，能方便做設定
			System.out.println("傳入的是隸屬companyId" + companyId);

			// Eleplo Receiver Mobile IOS版的IMEI會是：199A3260-CB71-4C16-8FE3-6907C6011C0F
			if (gatewayMac != null) {
				if (gatewayMac.length() > 12 && gatewayMac.contains("-")) {
					gatewayMac = gatewayMac.substring(gatewayMac.length() - 12, gatewayMac.length());
				}
				log.info("gatewayMac = " + gatewayMac);
			}

			List<BluetoothData> bluetoothDataList = cg05Dto.getBluetoothDataList();
			for (int i = 0; i < bluetoothDataList.size(); i++) {

				BluetoothData bluetoothData = bluetoothDataList.get(i);

				String deviceMac = bluetoothData.getBluetoothMac();
				deviceMac = deviceMac.toUpperCase(); // 轉為大寫
				log.info("deviceMac = " + deviceMac);

				Byte rssi = bluetoothData.getRssi();
				log.info("rssi = " + rssi);
				String deviceRawData = bluetoothData.getRawData();
				log.info("deviceRawData = " + deviceRawData);

				GatewayDataDtoCg05 gatewayCg05Dto = new GatewayDataDtoCg05();
				gatewayCg05Dto.setCompanyId(companyId);
				gatewayCg05Dto.setDeviceMac(deviceMac);
				gatewayCg05Dto.setGatewayFirmwareVersion(gatewayVersion);
				gatewayCg05Dto.setRawData(deviceRawData);
				gatewayCg05Dto.setRssi(rssi);
				gatewayCg05Dto.setGatewayMac(gatewayMac);
				cg05DtoList.add(gatewayCg05Dto);
			}
			HashMap<String, DeviceDataBo> deviceDataMap = cg05DataConverter.parseToDeviceDataMap(cg05DtoList);
			gatewayDataService.findDeviceModelFromEleploCore("CG05Jsons", deviceDataMap);

		} else {
			System.out.println(info);
		}
	}

	// Eleplo Receiver Cloud(回傳任務)
	// 華成白色大台的(cg05-Json格式回傳任務版本)
	@RequestMapping("/cg05jsonSyncmode")
	@Async
	public ResponseEntity<?> cg05jsonSyncmodeReceive(@RequestBody String rawDataString)
			throws JsonMappingException, JsonProcessingException {
		String info = "收到CG05-Json/Eleplo Receiver Cloud版的rawData ===>>> " + rawDataString;
		List<ModelMap> mReturnAllDeviceTask = new ArrayList<ModelMap>();
		if (rawDataString != null) {
			log.info(info);

			ObjectMapper objectMapper = new ObjectMapper();
			// 這個版本的轉換是Json版本
			GatewayDataDtoCg05Json cg05Dto = objectMapper.readValue(rawDataString, GatewayDataDtoCg05Json.class);
			// log.info("json轉換成功 ===>>>" + cg05Dto);

			System.out.println("準備DtoList");
			ArrayList<GatewayDataDtoCg05> cg05DtoList = new ArrayList<GatewayDataDtoCg05>();

			String gatewayMac = cg05Dto.getBluetoothMac();
			String gatewayVersion = cg05Dto.getFirmwareVersion();
			int companyId = cg05Dto.getCompanyId(); // 這樣才能知道要掛在哪一個公司下，能方便做設定
			System.out.println("傳入的是隸屬companyId" + companyId);

			// Eleplo Receiver Mobile IOS版的IMEI會是：199A3260-CB71-4C16-8FE3-6907C6011C0F
			if (gatewayMac != null) {
				if (gatewayMac.length() > 12 && gatewayMac.contains("-")) {
					gatewayMac = gatewayMac.substring(gatewayMac.length() - 12, gatewayMac.length());
				}
				log.info("gatewayMac = " + gatewayMac);
			}

			List<BluetoothData> bluetoothDataList = cg05Dto.getBluetoothDataList();
			for (int i = 0; i < bluetoothDataList.size(); i++) {

				BluetoothData bluetoothData = bluetoothDataList.get(i);

				String deviceMac = bluetoothData.getBluetoothMac();
				deviceMac = deviceMac.toUpperCase(); // 轉為大寫
				log.info("deviceMac = " + deviceMac);

				Byte rssi = bluetoothData.getRssi();
				log.info("rssi = " + rssi);
				String deviceRawData = bluetoothData.getRawData();
				log.info("deviceRawData = " + deviceRawData);

				GatewayDataDtoCg05 gatewayCg05Dto = new GatewayDataDtoCg05();
				gatewayCg05Dto.setCompanyId(companyId);
				gatewayCg05Dto.setDeviceMac(deviceMac);
				gatewayCg05Dto.setGatewayFirmwareVersion(gatewayVersion);
				gatewayCg05Dto.setRawData(deviceRawData);
				gatewayCg05Dto.setRssi(rssi);
				gatewayCg05Dto.setGatewayMac(gatewayMac);
				cg05DtoList.add(gatewayCg05Dto);
			}
			HashMap<String, DeviceDataBo> deviceDataMap = cg05DataConverter.parseToDeviceDataMap(cg05DtoList);
			gatewayDataService.findDeviceModelFromEleploCore("CG05Jsons", deviceDataMap);
			mReturnAllDeviceTask = gettaskforgateway(deviceDataMap);
		} else {
			System.out.println(info);
		}
		return ResponseEntity.ok(mReturnAllDeviceTask);
	}

	// 華成白色大台的(cg05-PoE格式版本)
	// Async的意思是丟了就不管回傳
	@RequestMapping("/cg05poe")
	@Async
	public void cg05poeReceive(HttpServletRequest request)
			throws JsonMappingException, JsonProcessingException, IOException {

		if (request != null) {

			// 第一步驟是要先把Gateway收到的資料轉為物件的清單
			InputStream inputStream = request.getInputStream();
			try {
				byte[] bytes = inputStream.readAllBytes();
				if (bytes.length > 35) {
					String getRawDataString_FromGateway = Hex.encodeHexString(bytes);
// TODO Eleplo-parser:TEST資料，測試完畢要拿掉
//				String getRawDataString_FromGateway="ceeece83b1c0023f049c139ee8a432400201050dffd5060001009c139ee8a4320005094256303907ff0047004a004a84b4dbc30cd34d0201050dffff02aabc01d30cc3dbb4841c05094256303307ff66e5b84c0000c300005b0a82550201061aff4c000215fda50693a4e24fb1afcfc6eb0764782500000000c500cf56769991525302010614094750313843415f4534322e4257472e434c303400000000000000";
					String info = "收到CG05 Poe版的rawData ===>>> " + getRawDataString_FromGateway;
					log.info(info);
					// log.info("rawDataString = " + rawDataString + ", rawDataString.length = " +
					// rawDataString.length());
					String gatewayMac = getRawDataString_FromGateway.substring(0, 12);
					gatewayMac = gatewayMac.toUpperCase();
					// gatewayMac = macAddColon(gatewayMac); //在Eleplo核心不用加：
					log.info("gatewayMac = " + gatewayMac);
					String gatewayVersion = getRawDataString_FromGateway.substring(12, 16);
					String deviceQuantity = getRawDataString_FromGateway.substring(16, 18);
					log.info("deviceQuantity = " + deviceQuantity + ",gatewayVersion=" + gatewayVersion);
					String totalDeviceData = getRawDataString_FromGateway.substring(18,
							getRawDataString_FromGateway.length());
					// log.info("totalDeviceData = " + totalDeviceData+"\n");

					int beginIndex = 0;
					int endIndex = 76;
					String deviceMac = null;
					ArrayList<GatewayDataDtoCg05> cg05DtoList = new ArrayList<GatewayDataDtoCg05>();
					int intdeviceQuantity = Integer.valueOf(deviceQuantity, 16);
					if (intdeviceQuantity <= 11) { // 這一版韌體CG05最多是 11筆
						// log.info("--For Start--");
						for (int i = 1; i <= Integer.valueOf(deviceQuantity, 16); i++) {

							String onedeviceData = totalDeviceData.substring(beginIndex, endIndex);
							// log.info("onedeviceData = " + onedeviceData);
							deviceMac = onedeviceData.substring(0, 12);
							deviceMac = deviceMac.toUpperCase();
							// deviceMac = macAddColon(deviceMac); //在Eleplo核心不用加：
							log.info("deviceMac = " + deviceMac);
							String rssiString = onedeviceData.substring(12, 14);
							String deviceRawData = onedeviceData.substring(14, 76);
							log.info("deviceRawData = " + deviceRawData);

							if (deviceRawData.substring(0, 8).equals("03095357")) {
								deviceMac = deviceRawData.substring(54, 62).toUpperCase();
								log.info("三星手環deviceMac = " + deviceMac);
							}
							Integer rssiInt = Integer.valueOf(rssiString, 16) * -1;
							Byte rssi = Byte.valueOf(rssiInt.toString());
							log.info("rssi = " + rssi);
							GatewayDataDtoCg05 gatewayCg05Dto = new GatewayDataDtoCg05();
							gatewayCg05Dto.setGatewayMac(gatewayMac);
							gatewayCg05Dto.setDeviceMac(deviceMac);
							gatewayCg05Dto.setGatewayFirmwareVersion(gatewayVersion);
							gatewayCg05Dto.setRssi(rssi);
							gatewayCg05Dto.setRawData(deviceRawData);
							cg05DtoList.add(gatewayCg05Dto);
							
							

							beginIndex += 76;
							endIndex += 76;
						}
						// log.info("--For End--");

						HashMap<String, DeviceDataBo> deviceDataMap = cg05DataConverter
								.parseToDeviceDataMap(cg05DtoList);
						gatewayDataService.findDeviceModelFromEleploCore("CG05Poe", deviceDataMap);
					}
				}
			} finally {
				inputStream.close();
			}

			// 第二步驟就是將Gateway收到的物件清單，整理成為每一筆都是Device資料，Gateway Mac只是來源的註記說明
			// 過去g1轉換完，GatewayDataG1Dto有包含第一筆為gateway資料，所以有需要再轉換成deviceDataMap，但目前對於已經是一個Device一筆資料的，GatewayDataCg05Dto來說就會有點多餘。
			// 保留的目的是為了統一流程

		}

	}

	// 華成白色大台的(cg05-PoE格式版本)
	// 傳送訊息手環專用,回傳任務
	@RequestMapping("/cg05poesyncmode")
	public ResponseEntity<?> cg05poesyncmodeReceive(HttpServletRequest request)
			throws JsonMappingException, JsonProcessingException, IOException {
		List<ModelMap> mReturnAllDeviceTask = new ArrayList<ModelMap>();
		if (request != null) {

			// 第一步驟是要先把Gateway收到的資料轉為物件的清單
			InputStream inputStream = request.getInputStream();
			try {
				byte[] bytes = inputStream.readAllBytes();
				if (bytes.length > 35) {

					String getRawDataString_FromGateway = Hex.encodeHexString(bytes);

// TODO Eleplo-parser:TEST資料，測試完畢要拿掉
//		String getRawDataString_FromGateway = "e5e2e5e36cf2020406e16c27f414a45602010613ff0a0ae16c27f414a40000005000004c6fa7000709534830364854c1aea3d18ec34e02010613ff0a0ac1aea3d18ec30000003c00000000b60007094937504c5553e94dc28bc8e2320bff00018000800040f8170200000000000000000000000000000000000000d78f63f4a381470bff000180f7c0ff0000000000000000000000000000000000000000000000ddfef08e73085c02010617ff0a0a0000000000000000000000000000cc0d00003c0003095333c201ce00007f5c0201061aff4c000215fda50693a4e24fb1afcfc6eb0764782527105c5cc500";
					// bv10t 有配戴
//				String getRawDataString_FromGateway = "94b555c366e501070250c0f0b4dea25902010216ffb831200002fb7b670000ff8105000000000000617d000000000050c0f0fef4396402010216ffb83120000000a546f5670000ff810700000000000023a90000000000";
					String info = "收到CG05 Poesyncmode版的rawData ===>>> " + getRawDataString_FromGateway;

					log.info(info);
					// log.info("rawDataString = " + rawDataString + ", rawDataString.length = " +
					// rawDataString.length());
					String gatewayMac = getRawDataString_FromGateway.substring(0, 12);
					gatewayMac = gatewayMac.toUpperCase();
					// gatewayMac = macAddColon(gatewayMac); //在Eleplo核心不用加：
					log.info("gatewayMac = " + gatewayMac);
					String gatewayVersion = getRawDataString_FromGateway.substring(12, 16);
					String deviceQuantity = getRawDataString_FromGateway.substring(16, 18);
					log.info("deviceQuantity = " + deviceQuantity);
					String totalDeviceData = getRawDataString_FromGateway.substring(18,
							getRawDataString_FromGateway.length());
					log.info("totalDeviceData = " + totalDeviceData + "\n");

					int beginIndex = 0;
					int endIndex = 76;

					ArrayList<GatewayDataDtoCg05> cg05DtoList = new ArrayList<GatewayDataDtoCg05>();
					int intdeviceQuantity = Integer.valueOf(deviceQuantity, 16);
					if (intdeviceQuantity <= 11) { // 這一版韌體CG05最多是 11筆
						// log.info("--For Start--");
						for (int i = 1; i <= Integer.valueOf(deviceQuantity, 16); i++) {

							String onedeviceData = totalDeviceData.substring(beginIndex, endIndex);
							// log.info("onedeviceData = " + onedeviceData);
							String deviceMac = onedeviceData.substring(0, 12);
							deviceMac = deviceMac.toUpperCase();
							// deviceMac = macAddColon(deviceMac); //在Eleplo核心不用加：
							log.info("deviceMac = " + deviceMac);
							String rssiString = onedeviceData.substring(12, 14);
							String deviceRawData = onedeviceData.substring(14, 76);
							log.info("deviceRawData = " + deviceRawData);
							Integer rssiInt = Integer.valueOf(rssiString, 16) * -1;
							Byte rssi = Byte.valueOf(rssiInt.toString());
							log.info("rssi = " + rssi);
							GatewayDataDtoCg05 gatewayCg05Dto = new GatewayDataDtoCg05();
							gatewayCg05Dto.setGatewayMac(gatewayMac);
							gatewayCg05Dto.setDeviceMac(deviceMac);
							gatewayCg05Dto.setGatewayFirmwareVersion(gatewayVersion);
							gatewayCg05Dto.setRssi(rssi);
							gatewayCg05Dto.setRawData(deviceRawData);
							cg05DtoList.add(gatewayCg05Dto);

							beginIndex += 76;
							endIndex += 76;
						}
						// log.info("--For End--");

						HashMap<String, DeviceDataBo> deviceDataMap = cg05DataConverter
								.parseToDeviceDataMap(cg05DtoList);
						gatewayDataService.findDeviceModelFromEleploCore("CG05Poe", deviceDataMap);
						mReturnAllDeviceTask = gettaskforgateway(deviceDataMap);
						log.info("--回傳給Gateway任務--" + mReturnAllDeviceTask);
					}

				}

			} finally {

				inputStream.close();
			}

			// 第二步驟就是將Gateway收到的物件清單，整理成為每一筆都是Device資料，Gateway Mac只是來源的註記說明
			// 過去g1轉換完，GatewayDataG1Dto有包含第一筆為gateway資料，所以有需要再轉換成deviceDataMap，但目前對於已經是一個Device一筆資料的，GatewayDataCg05Dto來說就會有點多餘。
			// 保留的目的是為了統一流程

		}
		return ResponseEntity.ok(mReturnAllDeviceTask);

	}

	// 華成白色大台的(華成自己的版本)
	@RequestMapping("/uconnect")
	@Async
	public void partnerUconnectReceive(@RequestBody String rawDataString)
			throws JsonMappingException, JsonProcessingException {
		String info = "收到uconnect合作版本的rawData ===>>> " + rawDataString;

		// #$1,F228D90EDE42,11,16888880DE3F,74,11,0,0B0B0B0B0B0B0B0B00800B,-33#

		/*
		 * 1. $ start of report 2. msg type Type of message ex. 0: reserved, 1:
		 * Uconnect™ tag 2: iBeacon™, 3: Eddystone™, 4: Altbeacon™ 3. reader id 6 bytes
		 * ID of reader in hex => 12 chars 4. tag type (*) type of tag ex. 1: tag w/o
		 * g-sensor, 2: tag w/ g-sensor .. 5. tag id 6 bytes ID of tag in hex => 12
		 * chars 6. tag batt (*)(**) batt voltage of tag in 1/10 volt unit 7. tag button
		 * status (*) button status ex. 0: released, 1: pushed 8. tag motion status (*)
		 * motion status ex. 0: non-moving, 1: moving 9. Sensor (*)(***) Various sensor
		 * data (11 bytes) 10. tag rssi tag read rssi 11. # end of report
		 */

		if (rawDataString != null) {
			log.info(info);

			try {
				String delimiter_forTag = "#";
				String[] result_forTag = rawDataString.split(delimiter_forTag);
				ArrayList<GatewayDataDtoUconnect> uconnectDtoList = new ArrayList<GatewayDataDtoUconnect>();

				for (int i = 0; i < result_forTag.length; i++) {

					String strEachTagRawData = result_forTag[i];
					if (strEachTagRawData != null && strEachTagRawData.contains("$")) {

						System.out.println("收到數值 : " + strEachTagRawData);
						String delimiter_forEachInfo = ",";
						String[] result_forEachInfo = strEachTagRawData.split(delimiter_forEachInfo);

						String StrMsgType = result_forEachInfo[0];

						String StrReaderId = result_forEachInfo[1];
						String StrCounter = result_forEachInfo[2];
						String StrTagId = result_forEachInfo[3];
						// String StrReserve=result_forEachInfo[4];
						String StrTagBattery = result_forEachInfo[5];
						String StrTagButtonStatus = result_forEachInfo[6];
						String StrOtherRawData = result_forEachInfo[7];
						String StrRssi = result_forEachInfo[8];

						int intCounter = Integer.parseInt(StrCounter);
						// -----加工處理Battery----
						int intBattery = Integer.parseInt(StrTagBattery);
						System.out.println("Battery(Org Value) = " + intBattery);
						if (intBattery < intCounter) {
							intBattery = intBattery + 256;
						}
						float volt = (float) ((intBattery - intCounter) / 10.0);
						String strAfterConvert = Float.toString(volt);
						System.out.println("Battery(After convert) = " + strAfterConvert + "V");
						// -----加工處理Battery----

						// -----加工處理按鍵----
						int intButton = Integer.parseInt(StrTagButtonStatus);
						System.out.println("Button(Org Value)=" + intButton);
						int inButtonAfterConvert = intButton - intCounter;
						System.out.println("Button(After convert) = " + inButtonAfterConvert);
						if (inButtonAfterConvert == 1) {
							System.out.println("button = ON");
						} else {
							System.out.println("button = OFF");
						}
						// -----加工處理按鍵----

						System.out.println("\nMsg Type : " + StrMsgType);
						System.out.println("Reader Id : " + StrReaderId); // 有用到
						System.out.println("Counter : " + StrCounter);
						System.out.println("Tag Id : " + StrTagId); // 有用到
						// System.out.println("Tag Battery : "+strAfterConvert);
						System.out.println("Tag Button Status : " + inButtonAfterConvert);
						System.out.println("Other RawData : " + StrOtherRawData);
						System.out.println("Rssi : " + StrRssi); // 有用到
						Byte rssi = Byte.valueOf(StrRssi);

						GatewayDataDtoUconnect gatewayDataDtoUconnect = new GatewayDataDtoUconnect();
						gatewayDataDtoUconnect.setGatewayMac(StrReaderId);
						gatewayDataDtoUconnect.setGatewayFirmwareVersion("uconnect v1.0");
						gatewayDataDtoUconnect.setDeviceMac(StrTagId);
						gatewayDataDtoUconnect.setRssi(rssi);
						gatewayDataDtoUconnect.setRawData(strEachTagRawData);
						uconnectDtoList.add(gatewayDataDtoUconnect);
					} // if End
				} // for End

				HashMap<String, DeviceDataBo> deviceDataMap = uconnectDataConverter
						.parseToDeviceDataMap(uconnectDtoList);
				gatewayDataService.findDeviceModelFromEleploCore("Uconnect", deviceDataMap);

			} catch (Exception e) {
				log.info("Error in uconnect:" + e.getMessage());
			}

		} else {
			System.out.println(info);
		}

	}

	private String macAddColon(String mac) {
		final int[] MAC_OFFSET_ARRAY = { 2, 5, 8, 11, 14 };
		StringBuffer sb = new StringBuffer(mac);
		for (int offset : MAC_OFFSET_ARRAY) {
			sb.insert(offset, ":");
		}
		return sb.toString();
	}
	// $1,F228D90EDE42,33,162107150007,40,65,33,2121212121212121211121,-29#
	// $1,F228D90EDE42,133,162107150000,133,170,133,858585858585858585A785,-48#
	// $2,F228D90EDE42,0,59561DE2C191,0,0,0,-65,-66#
	// $1,F228D90EDE42,16,16888880DE3F,79,16,0,1010101010101010002B10,-19#

	private List<ModelMap> gettaskforgateway(HashMap<String, DeviceDataBo> deviceDataMap) {
		ModelMap DeviceTaskResponseForGatway = null;
		byte taskRssi = -85;
		List<ModelMap> mReturnAllDeviceTask = new ArrayList<ModelMap>();
		for (String key : deviceDataMap.keySet()) {

			System.out.println("key= " + key + " and value= " + deviceDataMap.get(key));
			DeviceDataBo deviceDataBo = deviceDataMap.get(key);
			String deviceMac = deviceDataBo.getDeviceMac();

			try {
				ResponseEntity<String> response = restTemplate.postForEntity(
						URI.create(eleploCoreUrl + "/api/v2/gatewayParser/getDevicePo"), deviceMac, String.class);
				String responseString = response.getBody();
				if (responseString != null) {
					// 這裡可以處理返回的 responseString，如需要進一步邏輯。
					System.out.println("Response from getDevicePo: " + responseString);
					// 使用 ObjectMapper 將 JSON 轉換為 Map
					JsonNode rootNode = objectMapper.readTree(responseString);
					JsonNode companyNode = rootNode.path("company");
					int taskRssiInt = companyNode.get("taskRssi").asInt();
					taskRssi = (byte) taskRssiInt; // 轉為 byte 型別
					System.out.println("Company Extracted taskRssi as byte: " + taskRssi);
				}
			} catch (Exception e) {
				log.warn("查無 CompanyTaskDeviceRssi for deviceMac: " + deviceMac, e);
			}

			String gatewayMac = deviceDataBo.getGatewayMac();
			Byte rssi = deviceDataBo.getRssi();
			if (rssi >= taskRssi) { // 抓取公司設定taskRssi欄位進行比對
				TaskDeviceDataRequest taskDeviceDataVo = new TaskDeviceDataRequest();
				taskDeviceDataVo.setMac(deviceMac);
				taskDeviceDataVo.setGatewayMac(gatewayMac);
				System.out.println(taskDeviceDataVo);
				try {
					ResponseEntity<String> response = restTemplate.postForEntity(
							URI.create(eleploCoreUrl + "/api/v1/taskforgateway/gettask"), taskDeviceDataVo,
							String.class);
					String responseString = response.getBody();
					if (responseString != null) {

						CheckDeviceTaskResponse checkDeviceTaskResponse = objectMapper.readValue(responseString,
								CheckDeviceTaskResponse.class); // 變為物件，並且得知每個設備的型號名稱
						System.out.println("checkDeviceTaskResponse = " + checkDeviceTaskResponse);
						// log.info("checkDeviceTaskResponse = " + checkDeviceTaskResponse+"\n");
						DeviceTaskResponseForGatway = new ModelMap();
						DeviceTaskResponseForGatway.put("id", checkDeviceTaskResponse.getId());
						DeviceTaskResponseForGatway.put("taskType", checkDeviceTaskResponse.getTaskType());
						DeviceTaskResponseForGatway.put("deviceType", checkDeviceTaskResponse.getDeviceType());
						DeviceTaskResponseForGatway.put("deviceMac", deviceMac);
						DeviceTaskResponseForGatway.put("content", checkDeviceTaskResponse.getContent());

						break;
//					陣列測試多個使用
//						mReturnAllDeviceTask.add(DeviceTaskResponseForGatway);
//						System.out.println("mReturnAllDeviceTask:" + mReturnAllDeviceTask);
					}

				} catch (Exception e) {
					log.warn("查無taskDeviceDataVo任務 : " + taskDeviceDataVo);
				}

			} else {
				System.out.println("抓取DeviceTaskRssi太遠不發送任務,Rssi:" + rssi);

			}
		}
//	    陣列單個回傳使用
		mReturnAllDeviceTask.add(DeviceTaskResponseForGatway);
		System.out.println("mReturnAllDeviceTask:" + mReturnAllDeviceTask);
		return mReturnAllDeviceTask;
	}

	// 新加坡LoRa(Kerlink473-Json格式版本)
	@RequestMapping("/Kerlink473json")
	@Async
	public void Kerlink473jsonReceive(@RequestBody String rawDataString)
			throws JsonMappingException, JsonProcessingException {
		String info = "收到Kerlink473版的rawData ===>>> " + rawDataString;

		if (rawDataString != null) {
			log.info(info);

			ObjectMapper objectMapper = new ObjectMapper();
			// 這個版本的轉換是Json版本

			try {
				// 将原始 JSON 数据解析为 JsonNode 对象
				JsonNode originalJsonNode = objectMapper.readTree(rawDataString);

				// 提取所需字段
				String deviceMac = originalJsonNode.get("deviceId").asText();
				log.info("deviceMac = " + deviceMac);
//				String deviceName = originalJsonNode.get("deviceName").asText();
				String gatewayMac = originalJsonNode.get("gatewayId").asText();
				String rawData = originalJsonNode.get("data").get("payload").get("raw").asText();
				log.info("deviceRawData = " + rawData);
				// rora是判斷beacon rssi 所以這裡先註解
				// int rssiInt = originalJsonNode.get("data").get("rssi").asInt();
				// 将 rssi 值转换为 byte 类型，无需额外处理
				// byte rssi = (byte) rssiInt;
				ArrayList<GatewayDataDtoKerlink473> Kerlink473DtoList = new ArrayList<GatewayDataDtoKerlink473>();
				GatewayDataDtoKerlink473 gatewayDataKerlink473Dto = new GatewayDataDtoKerlink473();
				gatewayDataKerlink473Dto.setGatewayMac(gatewayMac);
//				gatewayDataKerlink473Dto.setDeviceName(deviceName);
				gatewayDataKerlink473Dto.setDeviceMac(deviceMac);
				gatewayDataKerlink473Dto.setRawData(rawData);
//				gatewayDataKerlink473Dto.setRssi(rssi);
				Kerlink473DtoList.add(gatewayDataKerlink473Dto);

				HashMap<String, DeviceDataBo> deviceDataMap = Kerlink473DataConverter
						.parseToDeviceDataMap(Kerlink473DtoList);
				System.out.println("deviceDataMap:" + deviceDataMap);
				gatewayDataService.findDeviceModelFromEleploCore("Kerlink473json", deviceDataMap);

			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	// 研華LoRaWAN WISE6610 json版本
	@RequestMapping("/wise6610json")
	@Async
	public void wise6610jsonReceive(@RequestBody String rawDataString)
			throws JsonMappingException, JsonProcessingException {

		String info = "收到wise6610json版的rawData ===>>> " + rawDataString;
		if (rawDataString != null) {
			log.info(info);

			ObjectMapper objectMapper = new ObjectMapper();
			// 這個版本的轉換是Json版本

			try {
				// 将原始 JSON 数据解析为 JsonNode 对象
				JsonNode originalJsonNode = objectMapper.readTree(rawDataString);
				int length = originalJsonNode.get("data").asText().length();
				System.out.println("length:" + length);
				// 判斷 data 長度是否為 136
				if (originalJsonNode.get("data").asText().length() >= 14) {
					// 提取所需字段
					String deviceMac = originalJsonNode.get("deveui").asText();
					log.info("deviceMac = " + deviceMac);
					System.out.println("deviceMac " + deviceMac);
//					String deviceName = originalJsonNode.get("deviceName").asText();
					String gatewayMac = originalJsonNode.get("mac").asText();
					String rawData = originalJsonNode.get("data").asText();
					log.info("deviceRawData = " + rawData);
					if (rawData.startsWith("BDBDBDBD")) {
						// 如果是，去掉前面的三個 "DBDBDB"
						rawData = rawData.replaceFirst("BDBDBDBD", "BD"); // 用 "BD" 取代前面的 "BDBDBDBD"
						System.out.println("rawData has been changed to: " + rawData);
					}
					// rora是判斷beacon rssi 所以這裡先註解
					// int rssiInt = originalJsonNode.get("data").get("rssi").asInt();
					// 将 rssi 值转换为 byte 类型，无需额外处理
					// byte rssi = (byte) rssiInt;
					ArrayList<GatewayDataDtoKerlink473> Kerlink473DtoList = new ArrayList<GatewayDataDtoKerlink473>();
					GatewayDataDtoKerlink473 gatewayDataKerlink473Dto = new GatewayDataDtoKerlink473();
					gatewayDataKerlink473Dto.setGatewayMac(gatewayMac);
//					gatewayDataKerlink473Dto.setDeviceName(deviceName);
					gatewayDataKerlink473Dto.setDeviceMac(deviceMac);
					gatewayDataKerlink473Dto.setRawData(rawData);
//					gatewayDataKerlink473Dto.setRssi(rssi);
					Kerlink473DtoList.add(gatewayDataKerlink473Dto);

					HashMap<String, DeviceDataBo> deviceDataMap = Kerlink473DataConverter
							.parseToDeviceDataMap(Kerlink473DtoList);
					System.out.println("deviceDataMap:" + deviceDataMap);
					gatewayDataService.findDeviceModelFromEleploCore("Kerlink473json", deviceDataMap);
				}

			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	// 富華(uHFjson格式版本)
	@RequestMapping("/uHFjson")
	@Async
	public void uHFjsonReceive(@RequestBody String rawDataString) throws JsonMappingException, JsonProcessingException {
		String info = "收到uHFjson/Eleplo Receiver Cloud版的rawData ===>>> " + rawDataString;

		if (rawDataString != null) {
			log.info(info);

			ObjectMapper objectMapper = new ObjectMapper();
			// 這個版本的轉換是Json版本
			GatewayDataDtoUHFJson UHFDto = objectMapper.readValue(rawDataString, GatewayDataDtoUHFJson.class);
			log.info("json轉換成功 ===>>>" + UHFDto);

			System.out.println("準備DtoList");
			ArrayList<GatewayDataDtoUHF> UHFDtoList = new ArrayList<GatewayDataDtoUHF>();
			String gatewayMac = UHFDto.getReaderMac();
			// Eleplo Receiver Mobile IOS版的IMEI會是：199A3260-CB71-4C16-8FE3-6907C6011C0F
			if (gatewayMac != null) {
				if (gatewayMac.length() > 12 && gatewayMac.contains("-")) {
					gatewayMac = gatewayMac.substring(gatewayMac.length() - 15, gatewayMac.length());
				}
				log.info("gatewayMac = " + gatewayMac);
			}

			List<TagIdDto> deviceTagIdList = UHFDto.getDeviceTagIdList();
			for (int i = 0; i < deviceTagIdList.size(); i++) {

				TagIdDto tagIdDto = deviceTagIdList.get(i);

				String deviceMac = tagIdDto.getTagId();
				deviceMac = deviceMac.toUpperCase(); // 轉為大寫
				log.info("deviceMac = " + deviceMac);
				GatewayDataDtoUHF gatewayDataUHFDto = new GatewayDataDtoUHF();
				gatewayDataUHFDto.setDeviceMac(deviceMac);
				gatewayDataUHFDto.setGatewayMac(gatewayMac);
				if (tagIdDto.getAreaName() != null) {
					gatewayDataUHFDto.setAreaName(tagIdDto.getAreaName());
				}
				UHFDtoList.add(gatewayDataUHFDto);
			}
			HashMap<String, DeviceDataBo> deviceDataMap = UHFDataConverter.parseToDeviceDataMap(UHFDtoList);
			gatewayDataService.findDeviceModelFromEleploCore("UHFJsons", deviceDataMap);

		} else {
			System.out.println(info);
		}
	}

	// 富華(UHF THjson格式版本)
	@RequestMapping("/bu02rjson")
	@Async
	public void tHuHFjsonReceive(@RequestBody String rawDataString)
			throws JsonMappingException, JsonProcessingException {
		String info = "收到bu02rjson/Eleplo Receiver Cloud版的rawData ===>>> " + rawDataString;

		if (rawDataString != null) {
			log.info(info);

			ObjectMapper objectMapper = new ObjectMapper();
			// 這個版本的轉換是Json版本
			GatewayDataDtoThUHFJson UHFDto = objectMapper.readValue(rawDataString, GatewayDataDtoThUHFJson.class);
			log.info("json轉換成功 ===>>>" + UHFDto);

			System.out.println("準備DtoList");
			ArrayList<GatewayDataDtoUHF> UHFDtoList = new ArrayList<GatewayDataDtoUHF>();
			String gatewayMac = UHFDto.getReaderMac();
			// Eleplo Receiver Mobile IOS版的IMEI會是：199A3260-CB71-4C16-8FE3-6907C6011C0F
			if (gatewayMac != null) {
				if (gatewayMac.length() > 12 && gatewayMac.contains("-")) {
					gatewayMac = gatewayMac.substring(gatewayMac.length() - 12, gatewayMac.length());
				}
				log.info("gatewayMac = " + gatewayMac);
			}

			List<TagIdDto> deviceTagIdList = UHFDto.getDeviceTagIdList();
			List<TagTemperDto> tagTemperList = UHFDto.getDeviceTemperature();
			List<TagHumidDto> tagHumidList = UHFDto.getDeviceHumidity();
			for (int i = 0; i < deviceTagIdList.size(); i++) {

				TagIdDto tagIdDto = deviceTagIdList.get(i);
				TagTemperDto tagTemperDto = tagTemperList.get(i);
				TagHumidDto tagHumidDto = tagHumidList.get(i);
				String deviceMac = tagIdDto.getTagId();
				float temperature = Float.parseFloat(tagTemperDto.getTagTemper());
				int humidity = Integer.parseInt(tagHumidDto.getTagHumid());
				deviceMac = deviceMac.toUpperCase(); // 轉為大寫
				log.info("deviceMac = " + deviceMac);
				GatewayDataDtoUHF gatewayDataUHFDto = new GatewayDataDtoUHF();
				gatewayDataUHFDto.setDeviceMac(deviceMac);
				gatewayDataUHFDto.setGatewayMac(gatewayMac);
				gatewayDataUHFDto.setTemperature(temperature);
				gatewayDataUHFDto.setHumidity(humidity);
				UHFDtoList.add(gatewayDataUHFDto);
			}
			HashMap<String, DeviceDataBo> deviceDataMap = UHFDataConverter.parseToDeviceDataMap(UHFDtoList);
			gatewayDataService.findDeviceModelFromEleploCore("UHFJsons", deviceDataMap);

		} else {
			System.out.println(info);
		}
	}

	// 華成 溫溼度TH Tag測試for RoLa
	@RequestMapping("/THjson")
	@Async
	public void THjsonReceive(@RequestBody String rawDataString) throws JsonMappingException, JsonProcessingException {
		String info = "收到華成 溫溼度TH Tag測試forRoLa/Eleplo Receiver Cloud版的rawData ===>>> " + rawDataString;

		if (rawDataString != null) {
			log.info(info);

			ObjectMapper objectMapper = new ObjectMapper();
			// 這個版本的轉換是Json版本
			GatewayDataDtoCg05Json cg05Dto = objectMapper.readValue(rawDataString, GatewayDataDtoCg05Json.class);
			// log.info("json轉換成功 ===>>>" + cg05Dto);

			System.out.println("準備DtoList");
			ArrayList<GatewayDataDtoCg05> cg05DtoList = new ArrayList<GatewayDataDtoCg05>();

			String gatewayMac = cg05Dto.getBluetoothMac();
			String gatewayVersion = cg05Dto.getFirmwareVersion();
			int companyId = cg05Dto.getCompanyId(); // 這樣才能知道要掛在哪一個公司下，能方便做設定
			System.out.println("傳入的是隸屬companyId" + companyId);

			if (gatewayMac != null) {
				if (gatewayMac.length() > 12 && gatewayMac.contains("-")) {
					gatewayMac = gatewayMac.substring(gatewayMac.length() - 12, gatewayMac.length());
				}
				log.info("gatewayMac = " + gatewayMac);
			}

			List<BluetoothData> bluetoothDataList = cg05Dto.getBluetoothDataList();
			for (int i = 0; i < bluetoothDataList.size(); i++) {

				BluetoothData bluetoothData = bluetoothDataList.get(i);

				String deviceMac = bluetoothData.getBluetoothMac();
				deviceMac = deviceMac.toUpperCase(); // 轉為大寫
				log.info("deviceMac = " + deviceMac);

				Byte rssi = bluetoothData.getRssi();
				log.info("rssi = " + rssi);
				String deviceRawData = bluetoothData.getRawData();
				log.info("deviceRawData = " + deviceRawData);

				GatewayDataDtoCg05 gatewayCg05Dto = new GatewayDataDtoCg05();
				gatewayCg05Dto.setCompanyId(companyId);
				gatewayCg05Dto.setDeviceMac(deviceMac);
				gatewayCg05Dto.setGatewayFirmwareVersion(gatewayVersion);
				gatewayCg05Dto.setRawData(deviceRawData);
				gatewayCg05Dto.setRssi(rssi);
				gatewayCg05Dto.setGatewayMac(gatewayMac);
				cg05DtoList.add(gatewayCg05Dto);
			}
			HashMap<String, DeviceDataBo> deviceDataMap = cg05DataConverter.parseToDeviceDataMap(cg05DtoList);
			gatewayDataService.findDeviceModelFromEleploCore("CG05Jsons", deviceDataMap);

		} else {
			System.out.println(info);
		}
	}

	// 華成UHF+Gateway(CG06UHF格式版本)
	// Async的意思是丟了就不管回傳
	@RequestMapping("/cg06uhf")
	@Async
	public void cg06uhfReceive(HttpServletRequest request)
			throws JsonMappingException, JsonProcessingException, IOException {

		if (request != null) {

			// 第一步驟是要先把Gateway收到的資料轉為物件的清單
			InputStream inputStream = request.getInputStream();
			try {
				byte[] bytes = inputStream.readAllBytes();
				if (bytes.length > 35) {
					String getRawDataString_FromGateway = Hex.encodeHexString(bytes);

					// TODO Eleplo-parser:TEST資料，測試完畢要拿掉

					// AXZON AZN3120
//					String getRawDataString_FromGateway= "94b555c3aa3a0105023120f220700088160011094400d000000000000000000000000000000000000000000000f0f03120f22070008817001f096400a900000000000000000000000000000000000000000000f0f03120f220700088160011094400d000000000000000000000000000000000000000000000f0f0";

					String info = "收到cg06uhf版的rawData ===>>> " + getRawDataString_FromGateway;
					log.info(info);
					// log.info("rawDataString = " + rawDataString + ", rawDataString.length = " +
					// rawDataString.length());

					String gatewayMac = getRawDataString_FromGateway.substring(0, 12);
					gatewayMac = gatewayMac.toUpperCase();
					// gatewayMac = macAddColon(gatewayMac); //在Eleplo核心不用加：
					log.info("gatewayMac = " + gatewayMac);
					String gatewayVersion = getRawDataString_FromGateway.substring(12, 16);
					String deviceQuantity = getRawDataString_FromGateway.substring(16, 18);
					log.info("deviceQuantity = " + deviceQuantity + ",gatewayVersion=" + gatewayVersion);
					String totalDeviceData = getRawDataString_FromGateway.substring(18,
							getRawDataString_FromGateway.length());
					// log.info("totalDeviceData = " + totalDeviceData+"\n");

					int beginIndex = 0;
					int endIndex = 76;
					// 使用 substring() 取出最後四個字元，再與 "f0f0" 比較
					if (getRawDataString_FromGateway.substring(getRawDataString_FromGateway.length() - 4)
							.equals("f0f0")) {
						// 如果最後四個字元等於 "f0f0"，執行以下程式碼
						System.out.println("最後四個字元為 f0f0");
						ArrayList<GatewayDataDtoCg05> cg05DtoList = new ArrayList<GatewayDataDtoCg05>();
						int intdeviceQuantity = Integer.valueOf(deviceQuantity, 16);
						if (intdeviceQuantity <= 11) { // 這一版韌體CG05最多是 11筆
							// log.info("--For Start--");
							for (int i = 1; i <= Integer.valueOf(deviceQuantity, 16); i++) {

								String onedeviceData = totalDeviceData.substring(beginIndex, endIndex);
								// log.info("onedeviceData = " + onedeviceData);
								String deviceMac = onedeviceData.substring(0, 16);
								deviceMac = deviceMac.toUpperCase();
								// deviceMac = macAddColon(deviceMac); //在Eleplo核心不用加：
								log.info("deviceMac = " + deviceMac);
								String rssiString = onedeviceData.substring(16, 20);
								System.out.println("rssiString:" + rssiString);
								String deviceRawData = onedeviceData.substring(16, 76);
								log.info("deviceRawData = " + deviceRawData);
								Integer rssiInt = Integer.valueOf(rssiString, 16) * -1;
								Byte rssi = Byte.valueOf(rssiInt.toString());
								log.info("rssi = " + rssi);
								GatewayDataDtoCg05 gatewayCg05Dto = new GatewayDataDtoCg05();
								gatewayCg05Dto.setGatewayMac(gatewayMac);
								gatewayCg05Dto.setDeviceMac(deviceMac);
								gatewayCg05Dto.setGatewayFirmwareVersion(gatewayVersion);
								gatewayCg05Dto.setRssi(rssi);
								gatewayCg05Dto.setRawData(deviceRawData);
								cg05DtoList.add(gatewayCg05Dto);
//									System.out.println("cg05DtoList:"+cg05DtoList);
								beginIndex += 76;
								endIndex += 76;
							}
							// log.info("--For End--");

							HashMap<String, DeviceDataBo> deviceDataMap = cg05DataConverter
									.parseToDeviceDataMap(cg05DtoList);
//								System.out.println("deviceDataMap:"+deviceDataMap);
							gatewayDataService.findDeviceModelFromEleploCore("CG05Poe", deviceDataMap);
						}

					} else {
						// 否則執行以下程式碼
						System.out.println("最後四個字元不為 f0f0");
						ArrayList<GatewayDataDtoCg05> cg05DtoList = new ArrayList<GatewayDataDtoCg05>();
						int intdeviceQuantity = Integer.valueOf(deviceQuantity, 16);
						if (intdeviceQuantity <= 11) { // 這一版韌體CG05最多是 11筆
							// log.info("--For Start--");
							for (int i = 1; i <= Integer.valueOf(deviceQuantity, 16); i++) {

								String onedeviceData = totalDeviceData.substring(beginIndex, endIndex);
								// log.info("onedeviceData = " + onedeviceData);
								String deviceMac = onedeviceData.substring(0, 12);
								deviceMac = deviceMac.toUpperCase();
								// deviceMac = macAddColon(deviceMac); //在Eleplo核心不用加：
								log.info("deviceMac = " + deviceMac);
								String rssiString = onedeviceData.substring(12, 14);
								String deviceRawData = onedeviceData.substring(14, 76);
								log.info("deviceRawData = " + deviceRawData);
								Integer rssiInt = Integer.valueOf(rssiString, 16) * -1;
								Byte rssi = Byte.valueOf(rssiInt.toString());
								log.info("rssi = " + rssi);
								GatewayDataDtoCg05 gatewayCg05Dto = new GatewayDataDtoCg05();
								gatewayCg05Dto.setGatewayMac(gatewayMac);
								gatewayCg05Dto.setDeviceMac(deviceMac);
								gatewayCg05Dto.setGatewayFirmwareVersion(gatewayVersion);
								gatewayCg05Dto.setRssi(rssi);
								gatewayCg05Dto.setRawData(deviceRawData);
								cg05DtoList.add(gatewayCg05Dto);

								beginIndex += 76;
								endIndex += 76;
							}
							// log.info("--For End--");

							HashMap<String, DeviceDataBo> deviceDataMap = cg05DataConverter
									.parseToDeviceDataMap(cg05DtoList);
							gatewayDataService.findDeviceModelFromEleploCore("CG05Poe", deviceDataMap);
						}
					}

				}
			} finally {
				inputStream.close();
			}

			// 第二步驟就是將Gateway收到的物件清單，整理成為每一筆都是Device資料，Gateway Mac只是來源的註記說明
			// 過去g1轉換完，GatewayDataG1Dto有包含第一筆為gateway資料，所以有需要再轉換成deviceDataMap，但目前對於已經是一個Device一筆資料的，GatewayDataCg05Dto來說就會有點多餘。
			// 保留的目的是為了統一流程

		}

	}

	// 三星手錶
	@RequestMapping("/jsonSSWatch")
	@Async
	public void SSWatchReceive(@RequestBody String rawDataString) throws JsonMappingException, JsonProcessingException {
		try {
			System.out.println("收到三星手錶的rawData ===>>> " + rawDataString);

			// 1️⃣ 解析 JSON 拿出 imei
			JsonNode rootNode = objectMapper.readTree(rawDataString);
			String imei = rootNode.path("imei").asText();

			if (imei == null || imei.isEmpty()) {
				System.err.println("⚠️ rawData中缺少 imei 欄位！");
				return;
			}
			String deviceModelName = null;
			try {
				ResponseEntity<String> response = restTemplate.postForEntity(
						URI.create(eleploCoreUrl + "/api/v2/gatewayParser/getDevicePo"), imei, String.class);
				String responseString = response.getBody();
				if (responseString != null) {
					JsonNode deviceNode = objectMapper.readTree(responseString);
					JsonNode deviceModelNode = deviceNode.path("deviceModel");
					deviceModelName = deviceModelNode.path("name").asText(null);

					if (deviceModelName != null) {
						System.out.println("📱 來自裝置型號: " + deviceModelName);
					} else {
						System.out.println("⚠️ 無法從裝置資料中解析 deviceModel.name");
					}
				} else {
					System.out.println("⚠️ getDevicePo 回傳空資料，imei: " + imei);
				}
			} catch (HttpClientErrorException.NotFound e) {
				System.out.println("imei 未新增: " + imei);
				return; // 找不到裝置，直接結束

			}

			// 3️⃣ 生成新的 rawData JSON，加入 deviceModelName
			ObjectNode newRawData = (ObjectNode) rootNode;
			if (deviceModelName != null) {
				newRawData.put("deviceModelName", deviceModelName);
			}

			// 4️⃣ 呼叫 /getDeviceVo 傳送完整 newRawData
			try {
				URI uri = URI.create(eleploCoreUrl + "/api/v2/gatewayParser/getDeviceVo");
				restTemplate.postForEntity(uri, newRawData.toString(), String.class);
				System.out.println("✅ 成功將 newRawData 傳送至 getDeviceVo: " + newRawData.toString());
			} catch (Exception e) {
				log.warn("⚠️ 呼叫 getDeviceVo 發生錯誤，newRawData=" + newRawData.toString(), e);
			}

		} catch (Exception e) {
			System.err.println("❌ 呼叫 findByMac 發生錯誤：" + e.getMessage());
			e.printStackTrace();
		}
	}

	@RequestMapping("/deviceMessagesToken")
	@Async
	public void deviceMessagesTokenReceive(@RequestBody DeviceMessagesTokenDto deviceMessagesTokenDto)
			throws JsonMappingException, JsonProcessingException {

		System.out.println("收到FCM deviceMessagesTokenReceive ===>>> " + deviceMessagesTokenDto);
		try {
			restTemplate.postForEntity(URI.create(eleploCoreUrl + "/api/v2/gatewayParser/setDeviceMessagesToken"),
					deviceMessagesTokenDto, String.class);
		} catch (Exception e) {
			log.warn("deviceMessagesTokenDto = " + deviceMessagesTokenDto);
		}
	}
	/* 新加坡程式掃描 無使用任務先註解
	// 給Gateway 呼叫,更改任務id 為,完成任務
	@PostMapping("/completetask")
	public ResponseEntity<?> completedTask(@RequestBody TaskDeviceDataRequest taskDeviceDataRequest) {
		Long longTaskId = taskDeviceDataRequest.getId();
		// 還要判斷處理結果

		System.out.println("longTaskId:" + longTaskId);
		TaskDeviceDataRequest taskDeviceDataVo = new TaskDeviceDataRequest();
		taskDeviceDataVo.setId(longTaskId);
		try {
			ResponseEntity<String> response = restTemplate.postForEntity(
					URI.create(eleploCoreUrl + "/api/v1/taskforgateway/completetask"), taskDeviceDataVo, String.class);
			String responseString = response.getBody();
			return ResponseEntity.ok(responseString);
		} catch (Exception e) {
			log.warn("查無taskDeviceDataVo任務ID : " + taskDeviceDataVo);
		}

		return ResponseEntity.ok(1);
	}
    */
	
	/*新加坡程式掃描 無使用任務先註解
	// 給Gateway 呼叫,更改任務id 為,待重新發送
	@PostMapping("/setresendtask")
	public ResponseEntity<?> setresendTask(@RequestBody TaskDeviceDataRequest taskDeviceDataRequest) {
		Long longTaskId = taskDeviceDataRequest.getId();
		// 還要判斷處理結果

		System.out.println("longTaskId:" + longTaskId);
		TaskDeviceDataRequest taskDeviceDataVo = new TaskDeviceDataRequest();
		taskDeviceDataVo.setId(longTaskId);
		try {
			ResponseEntity<String> response = restTemplate.postForEntity(
					URI.create(eleploCoreUrl + "/api/v1/taskforgateway/setresendtask"), taskDeviceDataVo, String.class);
			String responseString = response.getBody();
			return ResponseEntity.ok(responseString);
		} catch (Exception e) {
			log.warn("查無taskDeviceDataVo任務ID : " + taskDeviceDataVo);
		}

		return ResponseEntity.ok(null);
	}
    */
	
	/**
	 * 接收外部傳入的 TaskId，並轉發 PATCH 請求至本機 Eleplo 服務 * @param taskId 任務識別碼
	 * 
	 * @return 轉發後的結果
	 */
	/*新加坡程式掃描 無使用任務先註解
	@PostMapping("/task/{taskId}")
	public ResponseEntity<String> forwardTaskStatus(@PathVariable("taskId") String taskId) {
		log.info("收到外部請求，準備處理 TaskId: {}", taskId);

		String targetUrl = eleploCoreUrl + "/api/v2/gatewayParser/" + taskId + "/mark-as-read";

		try {
			// 正確寫法：使用預設建構子
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// 明確宣告泛型避免 Java 11 型別推斷問題
			HttpEntity<Void> requestEntity = new HttpEntity<Void>(null, headers);

			log.info("發起 PATCH 請求至: {}", targetUrl);

			ResponseEntity<String> response = restTemplate.exchange(targetUrl, HttpMethod.POST, requestEntity,
					String.class);

			return response;

		} catch (Exception e) {
			log.error("轉發請求失敗: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
		}
	}
	*/
	private DeviceDataBo.RadarDto convertRadar(GatewayJsonBH13pAndBD04s.Radar radar) {

		if (radar == null || radar.getFlow() == null)
			return null;

		DeviceDataBo.RadarDto dto = new DeviceDataBo.RadarDto();
		DeviceDataBo.RadarDto.Flow f = new DeviceDataBo.RadarDto.Flow();

		f.setEnter(radar.getFlow().getEnter());
		f.setExit(radar.getFlow().getExit());

		dto.setFlow(f);
		return dto;
	}
	
	// ⚠️ 這支端點是同步的（不能加 @Async）：因為要把 2036 bytes 的電子紙封包
    // 透過 HTTP response 直接回傳給 Gateway。@Async + void 會導致 Servlet 容器
    // 提早認定 request 已處理完畢、關閉 response，背景執行緒之後想寫入會失敗。
	// ⚠️ 提醒：這支拿掉了 @Async、回傳型別從 void 改成 ResponseEntity<byte[]>。
	// 原因：需要把 2036 bytes 的電子紙封包同步透過 HTTP response 回傳給 Gateway，
	// @Async + void 會讓 Servlet 容器提早認定 request 處理完畢、關閉 response，
	// 背景執行緒真正要寫入時反而會失敗。除此之外，解析邏輯跟 cg05poe 完全一樣，一行沒改。
	 @PostMapping("/Cg06dual")
	    public ResponseEntity<byte[]> handleCg06Dual(HttpServletRequest request) {
	        String targetMac = "84B4DBC8EB39";
	        byte[] epaperPacket = null;
	 
	        if (request != null) {
	            try (InputStream inputStream = request.getInputStream()) {
	                byte[] bytes = inputStream.readAllBytes();
	                log.info("收到Cg06版的dual bytes.length===>>> {}", bytes.length);
	                
	 
	                if (bytes.length > 41) {
	                    
	                    String getRawDataString_FromGateway = Hex.encodeHexString(bytes);
	                    log.info("收到Cg06版的rawData ===>>> {}", getRawDataString_FromGateway);
	 
	                    String gatewayMac = getRawDataString_FromGateway.substring(0, 12).toUpperCase();
	                    log.info("gatewayMac = {}", gatewayMac);
	 
	                    String gatewayVersion = getRawDataString_FromGateway.substring(12, 16);
	                    String deviceQuantity = getRawDataString_FromGateway.substring(16, 18);
	                    log.info("deviceQuantity = {},gatewayVersion={}", deviceQuantity, gatewayVersion);
	 
	                    String totalDeviceData = getRawDataString_FromGateway.substring(18);
	 
	                    int beginIndex = 0;
	                    int endIndex = 76;
	                    String deviceMac = null;
	                    int intdeviceQuantity = Integer.parseInt(deviceQuantity, 16);
	 
	                    // 依據原本 CG05 邏輯進行迴圈解析 (最多 11 筆)
	                    if (intdeviceQuantity <= 11) {
	                        for (int i = 1; i <= intdeviceQuantity; i++) {
	                            if (beginIndex >= totalDeviceData.length()) {
	                                break;
	                            }
	 
	                            String onedeviceData = totalDeviceData.substring(beginIndex, Math.min(endIndex, totalDeviceData.length()));
	                            deviceMac = onedeviceData.substring(0, 12).toUpperCase();
	                            log.info("deviceMac = {}", deviceMac);
	 
	                            String rssiString = onedeviceData.substring(12, 14);
	                            String deviceRawData = onedeviceData.substring(14, 76);
	                            log.info("deviceRawData = {}", deviceRawData);
	 
	                            // 特殊裝置/三星手環判斷
	                            if (deviceRawData.substring(0, 8).equals("03095357")) {
	                                String samsungMac = deviceRawData.substring(54, 62).toUpperCase();
	                                log.info("三星手環deviceMac = {}", samsungMac);
	                            }
	 
	                            int rssiInt = Integer.parseInt(rssiString, 16) * -1;
	                            byte rssi = (byte) rssiInt;
	                            log.info("rssi = {}", rssi);
	 
	                            // 🎯 核心判斷：當找到指定的 Target MAC 時，生成 2036 Bytes 封包
	                            // sentMacSet.add() 是原子操作：同一個 MAC 只有第一次呼叫會回傳 true，
	                            // 之後即使再被掃到也不會重複繪製 + 推送
	                            if (targetMac.equalsIgnoreCase(deviceMac)) {
	                                if (sentMacSet.add(targetMac)) {
	                                    log.info("🎯 首次偵測到目標電子紙 MAC: {}，開始繪製畫面並生成 BIN 封包...", targetMac);
	                                    try {
	                                        epaperPacket = generateEpaperBinPacket(targetMac, "信士  henk", "光明燈");
	                                    } catch (DecoderException e) {
	                                        log.error("MAC hex 解碼失敗: {}", targetMac, e);
	                                    }
	                                } else {
	                                    log.debug("目標電子紙 MAC: {} 先前已推送過，本次略過，不重複產生封包", targetMac);
	                                }
	                            }
	 
	                            beginIndex += 76;
	                            endIndex += 76;
	                        }
	                    }
	                }
	                if (bytes.length < 42) {
	                    if (tryHandleAsUartResult(bytes)) {
	                        return ResponseEntity.ok().build();
	                    }
	                    return ResponseEntity.ok().build();
	                }
	            } catch (Exception e) {
	                log.error("解析 Gateway Raw Data 過程發生錯誤", e);
	                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	            }
	        }
	 
	        // 若有比對到目標 MAC 且成功生成 2036 封包，則回傳 Binary 數據
	        if (epaperPacket != null) {
	            log.info("成功回傳 2036 Bytes BIN 封包給 Gateway！ (長度: {} bytes)", epaperPacket.length);
	            HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	            return new ResponseEntity<>(epaperPacket, headers, HttpStatus.OK);
	        }
	 
	        // 若無匹配 MAC 則回傳 200 OK 空白響應
	        return ResponseEntity.ok().build();
	    }
	 
	    // UART_RESULT_PROTOCOL20260826 解析：回傳是否為合法的狀態回報封包 (回傳 true 代表已處理完畢)
	    // 判斷依據：合法 CG05 廣播最少需要 9(表頭) + 38(至少1台裝置) = 47 bytes，
	    // 而 UART_RESULT_PROTOCOL 夾帶表頭後固定是 9 + 32 = 41 bytes，比最小合法廣播還短，
	    // 用這個長度界線先分流，避免拿 CG05 邏輯硬解狀態回報封包造成 StringIndexOutOfBoundsException
	    private boolean tryHandleAsUartResult(byte[] bytes) {
	        
	        for (int offset = 0; offset <= bytes.length - 32; offset++) {
	            int calcChecksum = 0;
	            for (int i = 0; i < 31; i++) {
	                calcChecksum += (bytes[offset + i] & 0xFF);
	            }
	            calcChecksum &= 0xFF;
	 
	            if (calcChecksum == (bytes[offset + 31] & 0xFF)) {
	                log.info("[Cg06dual] 偵測到這是 Gateway 回報的 UART_RESULT_PROTOCOL 狀態封包 (長度={} bytes)，" +
	                        "改走狀態解析，不套用 CG05 廣播邏輯", bytes.length);
	                byte[] resultPacket = Arrays.copyOfRange(bytes, offset, offset + 32);
	                parseUartResultProtocol(resultPacket);
	                return true;
	            }
	        }
	 
	        log.warn("[Cg06dual] 收到長度異常({} bytes)且找不到合法 checksum 的封包，忽略: {}",
	                bytes.length, Hex.encodeHexString(bytes));
	        return true; // 長度不足以是正常廣播，即使 checksum 對不上也不要拿去跑 CG05 邏輯
	    }
	 
	// =========================================================================
	// UART_RESULT_PROTOCOL20260826 解析：Gateway 透過 UART 回報的狀態封包 (32 Bytes)
	// 請在您實際收到 Gateway UART 回應資料的地方呼叫這個方法 (例如另一支
	// 讀取序列埠 / 接收狀態回呼的 Controller 或 Service)。
	// =========================================================================
	    public void parseUartResultProtocol(byte[] rawData) {
	        if (rawData == null || rawData.length < 32) {
	            log.warn("[UART_RESULT] 收到的資料長度不足 32 bytes，長度={}", rawData == null ? 0 : rawData.length);
	            return;
	        }
	 
	        // 實務上收到的資料前面可能夾帶多餘 bytes (framing 未對齊)，
	        // 用 checksum 往回找出真正的 32 bytes 封包邊界，而不是假設從 offset 0 開始
	        byte[] packet = null;
	        int foundOffset = -1;
	        for (int offset = 0; offset <= rawData.length - 32; offset++) {
	            int calcChecksum = 0;
	            for (int i = 0; i < 31; i++) {
	                calcChecksum += (rawData[offset + i] & 0xFF);
	            }
	            calcChecksum &= 0xFF;
	 
	            if (calcChecksum == (rawData[offset + 31] & 0xFF)) {
	                packet = Arrays.copyOfRange(rawData, offset, offset + 32);
	                foundOffset = offset;
	                break;
	            }
	        }
	 
	        if (packet == null) {
	            log.error("[UART_RESULT] 找不到合法的 32 bytes 封包 (checksum 全部對不上)，原始資料({} bytes): {}",
	                    rawData.length, Hex.encodeHexString(rawData));
	            return;
	        }
	 
	        if (foundOffset > 0) {
	            log.warn("[UART_RESULT] 封包前偵測到 {} bytes 多餘資料，可能是前一筆訊息殘留或序列埠讀取未對齊: {}",
	                    foundOffset, Hex.encodeHexString(Arrays.copyOfRange(rawData, 0, foundOffset)));
	        }
	 
	        int errorCode = packet[0] & 0xFF;
	        String mac = String.format("%02X:%02X:%02X:%02X:%02X:%02X",
	                packet[1] & 0xFF, packet[2] & 0xFF, packet[3] & 0xFF,
	                packet[4] & 0xFF, packet[5] & 0xFF, packet[6] & 0xFF);
	 
	        long statusCode = readUint32LE(packet, 7);
	        long detailInfo = readUint32LE(packet, 11);
	 
	        String errorName;
	        String detailDesc;
	        switch (errorCode) {
	            case 0xFF:
	                errorName = "SUCCESS";
	                if (detailInfo == 0x00) {
	                    detailDesc = "連線成功";
	                } else if (detailInfo == 0x01) {
	                    detailDesc = "資料傳送成功 (2030 bytes 已全部寫入 FFF0，僅代表 BLE 鏈路層 write 成功)";
	                } else {
	                    detailDesc = "未知 detail: 0x" + Long.toHexString(detailInfo);
	                }
	                break;
	            case 0x02:
	                errorName = "TARGET_NOT_FOUND";
	                detailDesc = "SCAN 30 秒未找到目標 MAC 裝置";
	                break;
	            case 0x03:
	                errorName = "CONNECT_FAILED";
	                detailDesc = "BLE 連線失敗，NimBLE status/rc = 0x" + Long.toHexString(statusCode);
	                break;
	            case 0x04:
	                errorName = "GATT_WRITE_FAIL";
	                detailDesc = "GATT 寫入失敗，失敗時的寫入偏移量 = " + detailInfo + " bytes";
	                break;
	            case 0x05:
	                errorName = "UART_RX_FAIL";
	                if (detailInfo == 0x01) {
	                    detailDesc = "UART 接收緩衝區溢位";
	                } else if (detailInfo == 0x02) {
	                    detailDesc = "封包校驗失敗 (data[6] 或 data[7] 不匹配 'B'/'G')";
	                } else if (detailInfo == 0x03) {
	                    detailDesc = "UART 接收逾時 (累積不足 2036 bytes 超過 1 秒)";
	                } else if (detailInfo == 0x04) {
	                    detailDesc = "封包 Queue 已滿，封包被丟棄";
	                } else {
	                    detailDesc = "未知 detail: 0x" + Long.toHexString(detailInfo);
	                }
	                break;
	            case 0x06:
	                errorName = "SCAN_START_FAIL";
	                detailDesc = "BLE 掃描啟動失敗，NimBLE rc = " + detailInfo;
	                break;
	            default:
	                errorName = "UNKNOWN_0x" + Integer.toHexString(errorCode);
	                detailDesc = "未定義狀態碼";
	        }
	 
	        log.info("[UART_RESULT] errorCode=0x{} ({}), MAC(依實測為小端序原始回報)={}, statusCode=0x{}, detailInfo=0x{} -> {}",
	                Integer.toHexString(errorCode).toUpperCase(), errorName, mac,
	                Long.toHexString(statusCode), Long.toHexString(detailInfo), detailDesc);
	 
	        // ⚠️ 重點提醒：SUCCESS/0x01 只代表 BLE GATT write 這個動作在鏈路層成功，
	        // 電子紙 MCU 內部收到資料後 header("BG")/CRC32 是否驗證通過完全沒有回傳機制，
	        // 無法從這個封包判斷電子紙實際有沒有顯示畫面
	        if (errorCode == 0xFF && detailInfo == 0x01) {
	            log.info("[UART_RESULT] 藍牙寫入完整成功，但電子紙韌體端 header/CRC32 是否驗證通過無法從此協定得知，需以電子紙實際顯示結果或直連測試確認");
	        }
	    }
	 
	    private long readUint32LE(byte[] data, int offset) {
	        return (data[offset] & 0xFFL)
	                | ((data[offset + 1] & 0xFFL) << 8)
	                | ((data[offset + 2] & 0xFFL) << 16)
	                | ((data[offset + 3] & 0xFFL) << 24);
	    }
	 
	    // =========================================================================
	    // 核心繪圖與封包建構邏輯：生成 2036 Bytes (MAC 6B + BG 2B + Display 2024B + CRC32 4B)
	    // =========================================================================
	    private byte[] generateEpaperBinPacket(String macHex, String text1, String text2) throws DecoderException {
	        long startTime = System.currentTimeMillis();
	        log.info("[EPAPER] === 開始生成電子紙封包 === MAC={}, text1='{}', text2='{}'", macHex, text1, text2);
	 
	        int logicalWidth = 184;
	        int logicalHeight = 88;
	        int entityWidth = 88;
	        int entityHeight = 184;
	 
	        int bytesPerRow = (entityWidth + 7) / 8; // 11 bytes
	        byte[][] buffer = new byte[entityHeight][bytesPerRow];
	 
	        // 預設填滿 0xFF (白色)
	        for (int r = 0; r < entityHeight; r++) {
	            Arrays.fill(buffer[r], (byte) 0xFF);
	        }
	        log.info("[EPAPER] Step1 完成: buffer 初始化為全白 0xFF, 尺寸={} rows x {} bytes/row", entityHeight, bytesPerRow);
	 
	        // 使用 Java AWT 繪製 184x88 文字畫面
	        BufferedImage img = new BufferedImage(logicalWidth, logicalHeight, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g = img.createGraphics();
	 
	        // 畫白底
	        g.setColor(Color.WHITE);
	        g.fillRect(0, 0, logicalWidth, logicalHeight);
	 
	        // 設定黑字
	        g.setColor(Color.BLACK);
	        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	 
	        Font font1, font2;
	        if (kaiuBaseFont != null) {
//	            font1 = kaiuBaseFont.deriveFont(Font.PLAIN, 20f);
//	            font2 = kaiuBaseFont.deriveFont(Font.BOLD, 24f);
	            font1 = kaiuBaseFont.deriveFont(Font.PLAIN, 32f);
	            font2 = kaiuBaseFont.deriveFont(Font.BOLD, 32f);
	        } else {
	            log.warn("[EPAPER] ⚠️ kaiuBaseFont 未成功載入，改用 SansSerif，中文很可能無法正確顯示！");
	            font1 = new Font("SansSerif", Font.PLAIN, 32);
	            font2 = new Font("SansSerif", Font.BOLD, 32);
	        }
	 
	        int missingIdx1 = font1.canDisplayUpTo(text1);
	        if (missingIdx1 != -1) {
	            log.warn("[EPAPER] ⚠️ font1 無法顯示 text1 中的某些字元 (第 {} 個字元開始缺字): '{}'",
	                    missingIdx1, text1.charAt(missingIdx1));
	        }
	        int missingIdx2 = font2.canDisplayUpTo(text2);
	        if (missingIdx2 != -1) {
	            log.warn("[EPAPER] ⚠️ font2 無法顯示 text2 中的某些字元 (第 {} 個字元開始缺字): '{}'",
	                    missingIdx2, text2.charAt(missingIdx2));
	        }
	 
	        g.setFont(font1);
	        g.drawString(text1, 10, 30); // 繪製 text1
	        log.info("[EPAPER] Step2 完成: 繪製 text1='{}' at (10,30), font={}", text1, font1.getFontName());
	 
	        g.setFont(font2);
	        g.drawString(text2, 45, 68); // 繪製 text2
	        log.info("[EPAPER] Step2 完成: 繪製 text2='{}' at (45,68), font={}", text2, font2.getFontName());
	 
	        g.dispose();
	 
	        // 座標轉換與 Bit Packing (entity_x = logical_y, entity_y = 183 - logical_x)
	        int blackPixelCount = 0;
	        for (int lx = 0; lx < logicalWidth; lx++) {
	            for (int ly = 0; ly < logicalHeight; ly++) {
	                int rgb = img.getRGB(lx, ly);
	                int r = (rgb >> 16) & 0xFF;
	                int gVal = (rgb >> 8) & 0xFF;
	                int b = rgb & 0xFF;
	 
	                // 若為黑色像素，將對應 bit 設為 0
	                if (r < 128 && gVal < 128 && b < 128) {
	                    int entityX = ly;
	                    int entityY = (entityHeight - 1) - lx;
	 
	                    if (entityX >= 0 && entityX < entityWidth && entityY >= 0 && entityY < entityHeight) {
	                        int row = entityY;
	                        int col = entityX / 8;
	                        int bitIdx = entityX % 8;
	                        buffer[row][col] &= ~(1 << (7 - bitIdx));
	                        blackPixelCount++;
	                    }
	                }
	            }
	        }
	 
	        log.info("[EPAPER] Step3 完成: 座標轉換 + Bit Packing，黑色像素數量 = {}", blackPixelCount);
	        // ⚠️ 這是排查「畫面空白」最先要看的數字：若為 0，代表文字根本沒被畫到畫布上
	        // (字型抓不到、座標超出範圍、text 為空字串等)，後面產生的 2024 bytes 必定全部是 0xFF
	        if (blackPixelCount == 0) {
	            log.warn("[EPAPER] ⚠️ 警告：黑色像素數量為 0，畫面上完全沒有內容被繪製，電子紙必定顯示全白！請檢查字型/座標/文字內容");
	        }
	 
	        // 攤平成 2024 Bytes
	        byte[] displayData = new byte[2024];
	        int idx = 0;
	        for (int r = 0; r < entityHeight; r++) {
	            for (int c = 0; c < bytesPerRow; c++) {
	                displayData[idx++] = buffer[r][c];
	            }
	        }
	        log.info("[EPAPER] Step4 完成: 攤平成 displayData，長度={} bytes (預期 2024)", displayData.length);
	 
	        // 計算 2024 Bytes 的 CRC32 (IEEE 802.3)
	        CRC32 crc32 = new CRC32();
	        crc32.update(displayData);
	        long crcVal = crc32.getValue();
	        log.info("[EPAPER] Step5 完成: CRC32(displayData, 2024 bytes) = 0x{}", Long.toHexString(crcVal).toUpperCase());
	 
	        // 組裝 2036 Bytes 封包
	        ByteBuffer packet = ByteBuffer.allocate(2036);
	        packet.order(ByteOrder.LITTLE_ENDIAN);
	 
	        // 1. MAC Address (6 Bytes) - 正向格式，廠商已確認不需反轉
	        packet.put(Hex.decodeHex(macHex.toCharArray()));
	        // 2. Header "BG" (2 Bytes)
	        packet.put((byte) 'B');
	        packet.put((byte) 'G');
	        // 3. Display Data (2024 Bytes)
	        packet.put(displayData);
	        // 4. CRC32 (4 Bytes, Little Endian)
	        packet.putInt((int) crcVal);
	 
	        byte[] result = packet.array();
	 
	        // 診斷用完整摘要，方便直接跟廠商韌體 log 或 main.py 直連測試結果逐項比對
	        log.info("[EPAPER] === 封包生成完成 ===");
	        log.info("[EPAPER]   總長度        : {} bytes (預期 2036)", result.length);
	        log.info("[EPAPER]   MAC (byte0-5) : {}", Hex.encodeHexString(Arrays.copyOfRange(result, 0, 6)).toUpperCase());
	        log.info("[EPAPER]   Header(byte6-7): {} (應為 4247 = 'BG')",
	                Hex.encodeHexString(Arrays.copyOfRange(result, 6, 8)).toUpperCase());
	        log.info("[EPAPER]   顯示資料前16B  : {}", Hex.encodeHexString(Arrays.copyOfRange(result, 8, 24)).toUpperCase());
	        log.info("[EPAPER]   顯示資料後16B  : {}", Hex.encodeHexString(Arrays.copyOfRange(result, 2016, 2032)).toUpperCase());
	        log.info("[EPAPER]   CRC32(封包內,LE): {}", Hex.encodeHexString(Arrays.copyOfRange(result, 2032, 2036)).toUpperCase());
	        log.info("[EPAPER]   耗時           : {} ms", System.currentTimeMillis() - startTime);
	 
	        return result;
	    }
	 
	}
	 
