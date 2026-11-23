package com.chylyng.receiver.Nfg01Tcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 隨 Spring Boot 應用程式啟動時,一併啟動 TCP Server 監聽指定 Port
 * 接收 Gateway 設備(TCP Client)主動連入的資料
 *
 * TCP Port 可在 application.properties / application.yml 中透過 tcp.server.port 設定
 */
//@Component
public class TcpServerRunner implements CommandLineRunner, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(TcpServerRunner.class);

    @Value("${tcp.server.port:5090}")
    private int tcpPort;

    // 這裡的 key 對應你 application.yml 裡的 eleploCore.url
    @Value("${eleploCore.url}")
    private String eleploCoreUrl;

    // 執行緒池,預留給最多 50 台設備同時連線(20台 + 緩衝)
    private final ExecutorService pool = Executors.newFixedThreadPool(50);
    private final ConnectionRegistry registry;
    private final RestTemplate restTemplate;

    private ServerSocket serverSocket;

    public TcpServerRunner(ConnectionRegistry registry, RestTemplate restTemplate) {
        this.registry = registry;
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) {
        Thread acceptThread = new Thread(this::startListening, "tcp-accept-thread");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    private void startListening() {
        try {
            serverSocket = new ServerSocket(tcpPort);
            log.info("========================================");
            log.info(" TCP Server 已啟動,監聽 Port: {}", tcpPort);
            log.info(" 等待 Gateway 設備連線中...");
            log.info("========================================");

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new GatewayClientHandler(clientSocket, registry, restTemplate, eleploCoreUrl));
            }
        } catch (IOException e) {
            if (serverSocket == null || !serverSocket.isClosed()) {
                log.error("TCP Server 發生錯誤: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Spring Context 關閉時會自動呼叫這個方法,做連線資源的清理
     */
    @Override
    public void destroy() {
        log.info("正在關閉 TCP Server...");
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
        pool.shutdownNow();
    }
}
