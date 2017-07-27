package orz.xuchao.server.bean;

import io.netty.buffer.ByteBuf;

/**
 * Created by Administrator on 2017/7/7 0007.
 */
public class CustomMsg {

    private ByteBuf flag;//开始标识符号
    private Short len;//长度
    private byte channel;//信道
    private byte protocolVersion;//协议版本
    private ByteBuf body;//包体
    private ByteBuf end;//包尾


    public CustomMsg(  ByteBuf flag, Short len, byte channel, byte protocolVersion, ByteBuf body, ByteBuf end){
        this.flag=flag;
        this.len=len;//长度
        this.channel=channel;//信道
        this.protocolVersion=protocolVersion;//协议版本
        this.body=body;//包体
        this.end=end;//包尾
    }

    public ByteBuf getFlag() {
        return flag;
    }

    public void setFlag(ByteBuf flag) {
        this.flag = flag;
    }

    public Short getLen() {
        return len;
    }

    public void setLen(Short len) {
        this.len = len;
    }

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public byte getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public ByteBuf getBody() {
        return body;
    }

    public void setBody(ByteBuf body) {
        this.body = body;
    }

    public ByteBuf getEnd() {
        return end;
    }

    public void setEnd(ByteBuf end) {
        this.end = end;
    }
}
