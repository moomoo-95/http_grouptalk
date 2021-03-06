package moomoo.hgtp.grouptalk.protocol.hgtp.message.request;

import moomoo.hgtp.grouptalk.protocol.hgtp.exception.HgtpException;
import moomoo.hgtp.grouptalk.protocol.hgtp.message.base.HgtpHeader;
import moomoo.hgtp.grouptalk.protocol.hgtp.message.base.HgtpMessage;
import moomoo.hgtp.grouptalk.protocol.hgtp.message.base.HgtpMessageType;
import moomoo.hgtp.grouptalk.protocol.hgtp.message.base.content.HgtpRegisterContent;
import moomoo.hgtp.grouptalk.service.AppInstance;
import util.module.ByteUtil;


public class HgtpRegisterRequest extends HgtpMessage {

    private final HgtpHeader hgtpHeader;
    private final HgtpRegisterContent hgtpContent;

    public HgtpRegisterRequest(byte[] data) throws HgtpException {
        if (data.length >= HgtpHeader.HGTP_HEADER_SIZE + ByteUtil.NUM_BYTES_IN_LONG + ByteUtil.NUM_BYTES_IN_SHORT + ByteUtil.NUM_BYTES_IN_INT) {
            int index = 0;

            byte[] headerByteData = new byte[HgtpHeader.HGTP_HEADER_SIZE];
            System.arraycopy(data, index, headerByteData, 0, headerByteData.length);
            this.hgtpHeader = new HgtpHeader(headerByteData);
            index += headerByteData.length;

            byte[] contextByteData = new byte[hgtpHeader.getBodyLength()];
            System.arraycopy(data, index, contextByteData, 0, contextByteData.length);
            this.hgtpContent = new HgtpRegisterContent(contextByteData);
        } else {
            this.hgtpHeader = null;
            this.hgtpContent = null;
        }
    }

    public HgtpRegisterRequest(String userId, int seqNumber, long expires, String listenIp, short listenPort) {
        this.hgtpContent = new HgtpRegisterContent(expires, listenIp, listenPort);
        this.hgtpHeader = new HgtpHeader(AppInstance.MAGIC_COOKIE, HgtpMessageType.REGISTER, HgtpMessageType.REGISTER, userId, seqNumber, AppInstance.getInstance().getTimeStamp(), hgtpContent.getBodyLength());
    }

    @Override
    public byte[] getByteData() {
        byte[] data = new byte[HgtpHeader.HGTP_HEADER_SIZE + this.hgtpHeader.getBodyLength()];
        int index = 0;

        byte[] headerByteData = this.hgtpHeader.getByteData();
        System.arraycopy(headerByteData, 0, data, index, headerByteData.length);
        index += headerByteData.length;

        byte[] contextByteData = this.hgtpContent.getByteData();
        System.arraycopy(contextByteData, 0, data, index, contextByteData.length);

        return data;
    }

    public HgtpHeader getHgtpHeader() {return hgtpHeader;}

    public HgtpRegisterContent getHgtpContent() {return hgtpContent;}
}
