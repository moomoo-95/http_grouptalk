package moomoo.hgtp.client.service;

import moomoo.hgtp.client.protocol.hgtp.HgtpConsumer;
import instance.BaseEnvironment;
import instance.DebugLevel;
import network.definition.NetAddress;
import network.socket.SocketManager;
import network.socket.SocketProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ResourceManager;
import service.scheduler.schedule.ScheduleManager;

public class ServiceManager {

    private static final Logger log = LoggerFactory.getLogger(ServiceManager.class);
    private static final int DELAY_TIME = 1000;
    private static final int MIN_PORT = 5000;
    private static final int MAX_PORT = 7000;
    private static final int SEND_BUF = 1048576;
    private static final int RECV_BUF = 1048576;

    private final String localIp;
    private final int localHgtpPort;

    private static ServiceManager serviceManager = null;

    // NetAddress 생성
    private final NetAddress clientAddress;
    private final NetAddress serverAddress;

    private BaseEnvironment baseEnvironment = null;
    private SocketManager socketManager = null;
    private boolean isQuit = false;

    public ServiceManager() {
        AppInstance appInstance = AppInstance.getInstance();
        this.localIp = appInstance.getConfigManager().getLocalListenIp();
        this.localHgtpPort = appInstance.getConfigManager().getLocalListenPort();

        this.clientAddress = new NetAddress(localIp, localHgtpPort,true, SocketProtocol.TCP);
        this.serverAddress = new NetAddress(localIp, 6000,true, SocketProtocol.TCP);
    }

    public static ServiceManager getInstance() {
        if (serviceManager == null) {
            serviceManager = new ServiceManager();
        }
        return serviceManager;
    }

    public void loop() {
        if (!start()) {
            log.error("() () () Fail to start service");
        }

        while (!isQuit) {
            try {
                Thread.sleep(DELAY_TIME);
            } catch (Exception e) {
                log.error("ServiceManager.loop ", e);
            }
        }
    }

    public boolean start() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.error("Process is about to quit (Ctrl+C)");
            this.isQuit = true;
            this.stop();
        }));

        // HGTP Consumer 생성
        HgtpConsumer.getInstance();

        // 인스턴스 생성
        baseEnvironment = new BaseEnvironment( new ScheduleManager(), new ResourceManager(MIN_PORT, MAX_PORT), DebugLevel.DEBUG );

        // SocketManager 생성
        socketManager = new SocketManager( baseEnvironment, true, 10, SEND_BUF, RECV_BUF );
        return true;
    }

    public void stop() {
        // 소켓 삭제
        if (socketManager != null && socketManager.getSocket(clientAddress) != null) {
            socketManager.removeSocket(clientAddress);
        }

        // 인스턴스 삭제
        if (baseEnvironment != null) {
            baseEnvironment.stop();
        }
    }
}
