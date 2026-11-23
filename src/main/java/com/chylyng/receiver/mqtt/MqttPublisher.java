package com.chylyng.receiver.mqtt;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

//@Service
public class MqttPublisher {

    private final Mqtt3AsyncClient client;

    @Autowired
    public MqttPublisher(
            @Value("${mqtt.broker.host:eleplo-mqtt-broker}") String brokerHost,
            @Value("${mqtt.broker.port:1883}") int brokerPort,
            @Value("${mqtt.broker.username:admin}") String brokerUsername,
            @Value("${mqtt.broker.password:admin_secret}") String brokerPassword) {
        this.client = MqttClient.builder()
                .useMqttVersion3()
                .identifier("gp21-server"+ UUID.randomUUID())
                .serverHost(brokerHost)
                .serverPort(brokerPort)
                .simpleAuth()
                    .username(brokerUsername)
                    .password(brokerPassword.getBytes(StandardCharsets.UTF_8))
                    .applySimpleAuth()
                .buildAsync();

        connectRetry();
    }

    private void connectRetry() {
        new Thread(() -> {
            while (!client.getState().isConnected()) {
                try {
                    System.out.println("[MQTT-Publisher] Try connecting...");
                    client.connect().get();
                    System.out.println("[MQTT-Publisher] Connected SUCCESS");
                } catch (Exception e) {
                    System.out.println("[MQTT-Publisher] Connect FAILED, retry in 3 sec");
                    System.out.println("[MQTT-Publisher] Error: " + e.getMessage());
                    try { Thread.sleep(3000); } catch (Exception ignored) {}
                }
            }
        }, "MQTT-Publisher-Connect-Thread").start();
    }

    public void publish(String topic, String payload) {

        // === Log: 是否已連線 ===
        if (!client.getState().isConnected()) {
            System.out.println("[MQTT-Publisher] ERROR: MQTT not connected, cannot publish!");
            return;
        }

        System.out.println("[MQTT-Publisher] Publishing...");
        System.out.println("  Topic  : " + topic);
        System.out.println("  Payload: " + payload);

        client.publishWith()
                .topic(topic)
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .whenComplete((ack, throwable) -> {

                    // === 成功 ===
                    if (throwable == null) {
                        System.out.println("[MQTT-Publisher] Publish SUCCESS");
                        if (ack != null) {
                            System.out.println("  Ack: " + ack);
                        }
                    } 
                    // === 失敗 ===
                    else {
                        System.out.println("[MQTT-Publisher] Publish FAILED");
                        System.out.println("  ERROR: " + throwable.getMessage());
                    }
                });
    }
}
