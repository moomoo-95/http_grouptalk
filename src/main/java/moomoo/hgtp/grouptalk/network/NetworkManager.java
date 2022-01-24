package moomoo.hgtp.grouptalk.network;

import instance.BaseEnvironment;
import instance.DebugLevel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import moomoo.hgtp.grouptalk.config.ConfigManager;
import moomoo.hgtp.grouptalk.network.handler.HgtpChannelHandler;
import moomoo.hgtp.grouptalk.network.handler.HttpChannelHandler;
import moomoo.hgtp.grouptalk.service.AppInstance;
import network.definition.NetAddress;
import network.socket.GroupSocket;
import network.socket.SocketManager;
import network.socket.SocketProtocol;
import service.ResourceManager;
import service.scheduler.schedule.ScheduleManager;

public class NetworkManager {

    private static AppInstance appInstance = AppInstance.getInstance();

    private static NetworkManager networkManager = null;

    // NetAddress 생성
    private BaseEnvironment baseEnvironment = null;
    private SocketManager socketManager = null;

    private NetAddress hgtpLocalAddress = null;
    private NetAddress hgtpTargetAddress = null;

    private NetAddress httpLocalAddress = null;
    private NetAddress httpTargetAddress = null;

    public NetworkManager() {
        // nothing
    }

    public static NetworkManager getInstance() {
        if (networkManager == null) {
            networkManager = new NetworkManager();
        }
        return networkManager;
    }

    public void startSocket(){
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();

        // 인스턴스 생성
        baseEnvironment = new BaseEnvironment( new ScheduleManager(), new ResourceManager(configManager.getHttpMinPort(), configManager.getHttpMaxPort()), DebugLevel.DEBUG );

        // SocketManager 생성
        socketManager = new SocketManager( baseEnvironment, true, 10, configManager.getSendBufSize(), configManager.getRecvBufSize() );

        // HGTP local / target 주소 설정
        hgtpLocalAddress = new NetAddress(configManager.getLocalListenIp(), configManager.getHgtpListenPort(),true, SocketProtocol.UDP);
        hgtpTargetAddress = new NetAddress(configManager.getTargetListenIp(), configManager.getHgtpTargetPort(), true, SocketProtocol.UDP);

        // HTTP local / target 주소 설정
        httpLocalAddress = new NetAddress(configManager.getLocalListenIp(), configManager.getHttpListenPort(), true, SocketProtocol.TCP);
        // todo register 등록시 저장되도록 설정
        //httpTargetAddress = new NetAddress(configManager.getTargetListenIp(), configManager.getHttpListenPort(), true, SocketProtocol.TCP);

        ChannelInitializer<NioDatagramChannel> hgtpChannelInitializer = new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel datagramChannel) {
                final ChannelPipeline channelPipeline = datagramChannel.pipeline();
                channelPipeline.addLast(new HgtpChannelHandler());
            }
        };

        ChannelInitializer<NioSocketChannel> httpChannelInitializer = new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel socketChannel) {
                final ChannelPipeline channelPipeline = socketChannel.pipeline();
                channelPipeline.addLast(new HttpChannelHandler());
            }
        };

        socketManager.addSocket(hgtpLocalAddress, hgtpChannelInitializer);
        socketManager.addSocket(httpLocalAddress, httpChannelInitializer);

        GroupSocket hgtpGroupSocket = socketManager.getSocket(hgtpLocalAddress);
        hgtpGroupSocket.getListenSocket().openListenChannel();

        if (appInstance.getMode() == AppInstance.CLIENT_MODE) {
            hgtpGroupSocket.addDestination(hgtpTargetAddress, null, AppInstance.SERVER_SESSION_ID, hgtpChannelInitializer);
        }

        GroupSocket httpGroupSocket = socketManager.getSocket(httpLocalAddress);
        httpGroupSocket.getListenSocket().openListenChannel();
    }

    public void stopSocket() {
        // 소켓 삭제
        if (socketManager != null) {
            if (socketManager.getSocket(hgtpLocalAddress) != null) {
                socketManager.removeSocket(hgtpLocalAddress);
            }

            if (socketManager.getSocket(httpLocalAddress) != null) {
                socketManager.removeSocket(httpLocalAddress);
            }
        }

        // 인스턴스 삭제
        if (baseEnvironment != null) {
            baseEnvironment.stop();
        }
    }

    public BaseEnvironment getBaseEnvironment() {return baseEnvironment;}

    public GroupSocket getHgtpGroupSocket() {return socketManager.getSocket(hgtpLocalAddress);}

    public GroupSocket getHttpGroupSocket() {return socketManager.getSocket(httpLocalAddress);}
}
