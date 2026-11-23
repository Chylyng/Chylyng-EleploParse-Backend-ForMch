package com.chylyng.receiver.Nfg01Tcp;
import com.chylyng.receiver.Nfg01Tcp.GatewayDeviceRawDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 處理單一 Gateway (TCP Client) 連線的邏輯
 * 每個連線在獨立執行緒中運作,互不影響
 *
 * 注意:這個類別不是 Spring Bean(是被 TcpServerRunner 用 new 手動建立的),
 * 所以 @Autowired / @Value 在這裡完全不會生效,必須透過建構子把
 * restTemplate 和 eleploCoreUrl 從 TcpServerRunner(它才是 Spring Bean)傳進來。
 */
public class GatewayClientHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(GatewayClientHandler.class);

    private final Socket socket;
    private final String clientKey;
    private final ConnectionRegistry registry;
    private final RestTemplate restTemplate;
    private final String eleploCoreUrl;

    public GatewayClientHandler(Socket socket,
                                 ConnectionRegistry registry,
                                 RestTemplate restTemplate,
                                 String eleploCoreUrl) {
        this.socket = socket;
        this.registry = registry;
        this.restTemplate = restTemplate;
        this.eleploCoreUrl = eleploCoreUrl;
        this.clientKey = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    @Override
    public void run() {
        registry.register(clientKey, socket.getInetAddress().getHostAddress(), socket.getPort());
        log.info("新設備已連線 -> {}", clientKey);

        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int len;

            while ((len = in.read(buffer)) != -1) {
                if (len > 0) {
                    registry.recordDataReceived(clientKey, len);

                    String hex = bytesToHex(buffer, len);
                    String text = new String(buffer, 0, len).trim();

                    log.info("收到 NFG01 Reader 資料 [來源: {}] 長度={} bytes", clientKey, len);
                    log.info("  HEX : {}", hex);
                    log.info("  TEXT: {}", text);

                    // hex 是帶空格的字串(例如 "AA BB CC..."),解析 MAC 前必須先去掉空格
                    String hexCompact = hex.replace(" ", "");

                    if (hexCompact.length() < 24) {
                        log.warn("收到的資料長度不足,無法解析 MAC,長度={} , hexCompact={}", hexCompact.length(), hexCompact);
                        continue;
                    }

                    String gatewayMac = hexCompact.substring(0, 12);
                    log.info("gatewayMac = {}", gatewayMac);

                    String deviceMac = reverseHexBytes(hexCompact.substring(12, 26));
                    log.info("deviceMac NV01 = {}", deviceMac);

                    String deviceModelName = "NV01";

                    // 包成 DTO 物件,取代原本手動拼 JSON 字串
                    GatewayDeviceRawDataDto newRawData = new GatewayDeviceRawDataDto();
                    newRawData.setGatewayMac(gatewayMac);
                    newRawData.setDeviceMac(deviceMac);
                    newRawData.setDeviceModelName(deviceModelName);
                   
                    sendToBackend(newRawData);

                    // 若需要回覆設備(例如 ACK),可在此處寫回
                    // out.write("OK\n".getBytes());
                    // out.flush();
                }
            }
        } catch (IOException e) {
            log.warn("連線讀取發生錯誤 [{}]: {}", clientKey, e.getMessage());
        } finally {
            registry.unregister(clientKey);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            log.info("設備已斷線 -> {}", clientKey);
        }
    }

    /**
     * 將解析後的 DTO 物件轉發給 eleploCore 後端 API
     * RestTemplate 會自動把物件序列化成 JSON(前提是 classpath 上有 Jackson,
     * spring-boot-starter-web 預設就會帶進來)
     */
    private void sendToBackend(GatewayDeviceRawDataDto newRawData) {
        try {
            URI uri = URI.create(eleploCoreUrl + "/api/v2/gatewayParser/getDeviceVo");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<GatewayDeviceRawDataDto> requestEntity = new HttpEntity<>(newRawData, headers);

            restTemplate.postForEntity(uri, requestEntity, String.class);
            log.info("成功將 newRawData 傳送至 getDeviceVo: {}", newRawData);
        } catch (Exception e) {
            log.warn("呼叫 getDeviceVo 發生錯誤,newRawData={}", newRawData, e);
        }
    }

    private String bytesToHex(byte[] bytes, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString().trim();
    }

    /**
     * 將 hex 字串以「每 2 個字元(1 byte)」為單位反轉順序
     * 例如輸入 "AABBCC" -> 輸出 "CCBBAA"
     */
    public static String reverseHexBytes(String reversedHex) {
        if (reversedHex == null || reversedHex.trim().isEmpty()) {
            return "";
        }

        String cleanHex = reversedHex.replaceAll("\\s+", "").toUpperCase();

        if (cleanHex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string length must be even.");
        }

        List<String> bytesList = new ArrayList<>();
        for (int i = 0; i < cleanHex.length(); i += 2) {
            bytesList.add(cleanHex.substring(i, i + 2));
        }

        Collections.reverse(bytesList);

        return String.join("", bytesList);
    }
}
