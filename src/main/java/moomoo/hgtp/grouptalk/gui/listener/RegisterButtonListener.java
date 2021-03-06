package moomoo.hgtp.grouptalk.gui.listener;

import moomoo.hgtp.grouptalk.config.ConfigManager;
import moomoo.hgtp.grouptalk.protocol.hgtp.message.request.HgtpRegisterRequest;
import moomoo.hgtp.grouptalk.protocol.hgtp.message.request.handler.HgtpRequestHandler;
import moomoo.hgtp.grouptalk.service.AppInstance;
import moomoo.hgtp.grouptalk.session.SessionManager;
import moomoo.hgtp.grouptalk.session.base.UserInfo;
import moomoo.hgtp.grouptalk.util.NetworkUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterButtonListener implements ActionListener {

    private static SessionManager sessionManager = SessionManager.getInstance();

    private final HgtpRequestHandler hgtpRequestHandler = new HgtpRequestHandler();

    @Override
    public void actionPerformed(ActionEvent e) {
        AppInstance appInstance = AppInstance.getInstance();
        ConfigManager configManager = appInstance.getConfigManager();

        UserInfo userInfo = sessionManager.getUserInfo(appInstance.getUserId());

        String inputName = JOptionPane.showInputDialog(null, "Put your name.");
        if (inputName.equals("")) {
            JOptionPane.showConfirmDialog(
                    null, "Fail : The name is blank.",
                    "REGISTER FAIL", JOptionPane.YES_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null
            );
            return;
        } else if (inputName.length() > 10) {
            JOptionPane.showConfirmDialog(
                    null, "Fail : The name is too long. (10 < ["+ inputName.length() +"])",
                    "REGISTER FAIL", JOptionPane.YES_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null
            );
            return;
        }
        userInfo.setHostName(inputName);

        // create request Register
        HgtpRegisterRequest hgtpRegisterRequest = new HgtpRegisterRequest(
                appInstance.getUserId(), AppInstance.SEQ_INCREMENT,
                configManager.getHgtpExpireTime(), configManager.getLocalListenIp(), configManager.getHgtpListenPort()
        );
        String encodeHostName = NetworkUtil.messageEncoding(userInfo.getHostName());
        hgtpRequestHandler.sendRegisterRequest(hgtpRegisterRequest, encodeHostName);
    }
}
