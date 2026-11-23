package com.chylyng.receiver.mqtt;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import lombok.extern.slf4j.Slf4j;

//@Service
@Slf4j
public class MqttSubscriberService {
	@Autowired
	private DeviceOnlineService deviceOnlineService; // ⭐新增

	@Autowired
	private MessageDispatcher dispatcher;

	private Mqtt3AsyncClient client;

	@Value("${eleploCore.url}")
	private String coreUrl;

	@Value("${mqtt.broker.host:eleplo-mqtt-broker}")
	private String brokerHost;

	@Value("${mqtt.broker.port:1883}")
	private int brokerPort;

	@Value("${mqtt.broker.username:admin}")
	private String brokerUsername;

	@Value("${mqtt.broker.password:admin_secret}")
	private String brokerPassword;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private MqttPublisher mqttPublisher;

	@PostConstruct
	public void init() {
		connectMqtt();
	}

	/** ★★★ 初始化 MQTT client（只建一次） */
	private void connectMqtt() {

		try {
			client = MqttClient.builder().useMqttVersion3()
					.identifier("subscriber-" + InetAddress.getLocalHost().getHostName())
					.serverHost(brokerHost)
					.serverPort(brokerPort)
					.simpleAuth()
						.username(brokerUsername)
						.password(brokerPassword.getBytes(StandardCharsets.UTF_8))
						.applySimpleAuth()
					.automaticReconnectWithDefaultConfig() // ⭐新增
					.buildAsync();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		/** ★★★ Listener 永遠只能註冊一次 */
		registerListenerOnce();

		/** ★★★ 自動重連 */
		retryConnect();
	}

	/**
	 * ★★★ Listener 僅註冊一次（最重要的防重複）
	 */
	private void registerListenerOnce() {

		System.out.println("★★ Listener 已註冊（只會出現一次）★★");

		client.publishes(MqttGlobalPublishFilter.ALL, publish -> {
			try {
				String msg = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);

				String topicStr = publish.getTopic().toString();
				String deviceMac = topicStr.substring(topicStr.lastIndexOf("/") + 1);

				System.out.println("【MQTT 收到】Topic=" + topicStr);
				System.out.println("DeviceId=" + deviceMac);
				System.out.println("內容=" + msg);
				log.info("DeviceId=" + deviceMac);
				log.info("內容=" + msg);
				// ⭐新增：每一筆 MQTT 都當心跳
				deviceOnlineService.updateHeartbeat(deviceMac);
				dispatcher.dispatch(deviceMac, msg);
				try {
					String url = coreUrl + "/api/v1/taskforgp21/gettask/" + deviceMac;

					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

					ResponseEntity<String> commandStatusResponse = restTemplate.postForEntity(URI.create(url),
							requestEntity, String.class);

					String responseString = commandStatusResponse.getBody();
					System.out.println("GP21訊息回傳:" + responseString);

					if (responseString != null && !responseString.isEmpty()) {
						JSONObject json = new JSONObject(responseString);
						String content = json.getString("content");
						Integer msgId = json.getInt("id");
						String mac = json.getJSONObject("devicePo").getString("mac");

						// content -> sendMsg
//				    	String sendMsg;
//				    	if (content.matches(".*[\\u4e00-\\u9fa5].*")) {
//				    	    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
//				    	    StringBuilder hex = new StringBuilder();
//				    	    for (byte b : bytes) {
//				    	        hex.append(String.format("%02x", b));
//				    	    }
//				    	    sendMsg = hex.toString();
//				    	} else {
//				    	    sendMsg = content;
//				    	}

						JSONObject mqttJson = new JSONObject();
						mqttJson.put("sendMsg", msgId + "," + mac + ",msg-" + content);
						mqttJson.put("dev", msgId + "," + mac);

						mqttPublisher.publish("dev/" + mac + "/cloud", mqttJson.toString());
					}
				} catch (HttpClientErrorException e) {
					log.warn("無任務或裝置不存在：" + e.getMessage());
				} catch (HttpServerErrorException e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				System.out.println("MQTT 訊息處理錯誤：" + e.getMessage());
			}
		});

	}

	/**
	 * ★★★ 自動重連 Thread（獨立且不會重複建立）
	 */
	private void retryConnect() {

		new Thread(() -> {

			while (true) {
				try {
					client.connect().get();
					System.out.println("MQTT 連線成功");

					subscribeWildcardTopic(); // ★★★ 每次重連後只重新訂閱，不動 listener
					return;

				} catch (Exception e) {
					System.out.println("MQTT 連線失敗，3 秒後重試: " + e.getMessage());
				}

				try {
					Thread.sleep(3000);
				} catch (InterruptedException ignored) {
				}
			}

		}, "MQTT-Reconnect-Thread").start();
	}

	/**
	 * ★★★ 訂閱通配符主題 dev/cloud/#
	 */
	private void subscribeWildcardTopic() {
		String topic = "dev/cloud/#";
		client.subscribeWith()
				.topicFilter(topic)
				.qos(MqttQos.AT_LEAST_ONCE)
				.send()
				.whenComplete((ack, ex) -> {
					if (ex != null) {
						System.out.println("MQTT 訂閱失敗：" + topic + "，原因：" + ex.getMessage());
					} else {
						System.out.println("MQTT 訂閱成功：" + topic);
					}
				});
	}
}
//最終穩定版 MqttSubscriberService（支援 dev/cloud/#）
//@Service
//public class MqttSubscriberService {
//
//    @Autowired
//    private MessageDispatcher dispatcher;
//
//    private Mqtt3AsyncClient client;
//
//    private final String WILDCARD_TOPIC = "dev/cloud/#";
//
//    @PostConstruct
//    public void init() {
//        connectMqtt();
//    }
//
//    /** 初始化 MQTT */
//    private void connectMqtt() {
//        client = MqttClient.builder()
//                .useMqttVersion3()
//                .identifier("spring-subscriber")
//                .serverHost("mqtest.imyfit.com")
//                .serverPort(1883)
//                .buildAsync();
//
//        listenMessages();   // ★ Listener 永遠只註冊 1 次
//        retryConnect();     // ★ 啟動重連 Thread
//    }
//
//    /** 重連邏輯（Thread 只會建立 1 次） */
//    private void retryConnect() {
//
//        new Thread(() -> {
//
//            while (true) {
//                try {
//                    client.connect().get();
//                    System.out.println("MQTT 連線成功");
//
//                    subscribeWildcardTopic(); // ★ 成功後訂閱單一通配符 topic
//                    return;                   // ★ 跳出迴圈，Thread 結束
//                } catch (Exception e) {
//                    System.out.println("MQTT 連線失敗，3 秒後重試：" + e.getMessage());
//                }
//
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException ignored) {}
//            }
//
//        }, "MQTT-Reconnect-Thread").start();
//    }
//
//    /** 訂閱 dev/cloud/# */
//    private void subscribeWildcardTopic() {
//
//        client.subscribeWith()
//                .topicFilter(WILDCARD_TOPIC)
//                .qos(MqttQos.AT_LEAST_ONCE)
//                .send()
//                .whenComplete((subAck, ex) -> {
//                    if (ex != null) {
//                        System.out.println("MQTT 訂閱失敗：" + ex.getMessage());
//                    } else {
//                        System.out.println("MQTT 訂閱成功：" + WILDCARD_TOPIC);
//                    }
//                });
//    }
//
//    /** 單一訊息監聽器（不重複註冊） */
//    private void listenMessages() {
//        client.publishes(MqttGlobalPublishFilter.ALL, publish -> {
//            try {
//                String msg = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
//
//                String topicStr = publish.getTopic().toString();
//                String[] parts = topicStr.split("/");
//                String deviceMac = parts[parts.length - 1];
//
//                System.out.println("【MQTT 收到】Topic=" + topicStr);
//                System.out.println("DeviceId=" + deviceMac);
//                System.out.println("內容=" + msg);
//
//                dispatcher.dispatch(deviceMac, msg);
//
//            } catch (Exception e) {
//                System.out.println("MQTT 訊息處理錯誤：" + e.getMessage());
//            }
//        });
//    }
//}
