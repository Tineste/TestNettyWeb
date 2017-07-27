package orz.xuchao.server.bean;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import orz.xuchao.server.utils.CRCUtil;

/**
 * Created by Administrator on 2017/7/13 0013.
 */
public class BasePackage {

    private static final byte[] flagB={(byte) 0xEF,0x3A};


//    标识符
    private ByteBuf flag;
//    长度
    private Short len;
//    信道
    private byte channel;
//    协议版本
    private byte protocolVersion;
//     包体
    private ByteBuf body;
//    包尾
    private ByteBuf end;


    public void setBody(ByteBuf body) {
        this.body = body;
    }

    public void setCustomMsgAttribute(ByteBuf flag, Short len,byte channel, byte protocolVersion,ByteBuf body,ByteBuf end) {
        this.flag =flag;
        this.len = len;
        this.channel = channel;
        this.protocolVersion = protocolVersion;
        this.body=body;
        this.end=end;

    }

    public CustomMsg getCustomMsg() {

        getLenAndEnd();
        CustomMsg customMsg = new CustomMsg(flag, len,channel,protocolVersion,body, end);
        return customMsg;
    }


        public static void main(String[] args)throws  Exception {

        BasePackage b=new BasePackage();
        b.setChannel((byte) 0x01);
        b.setProtocolVersion((byte) 0x01);
        byte[] body0= new byte[]{0x03,
                0x05, 0x01, 0x00, 0x01, 0x02, 0x03,
                0x59, 0x3D, 0x34, 0x11};
        ByteBuf body=Unpooled.buffer(body0.length);
        body.writeBytes(body0);
        b.setBody(body);

        byte[] end=b.getLenAndEnd().array();



    };



    public boolean checkCRC(){
        byte[] crc=new byte[this.end.readableBytes()];
        this.end.readBytes(crc);
        short checkCRC1=CRCUtil.bytesToShort(crc);

        ByteBuf temp=getLenAndEnd();

        byte[] crc2=new byte[temp.readableBytes()];
        temp.readBytes(crc2);
        short checkCRC2=CRCUtil.bytesToShort(crc2);
        if(checkCRC1==checkCRC2){
            return true;
        }else {
            return false;
        }
    }


    public ByteBuf getLenAndEnd() {
        byte[] templen=CRCUtil.short2Bytes((short) (body.readableBytes()+4));
        this.len=(short) (body.readableBytes()+4);
        byte[] len={templen[0],templen[1]};
        byte[] channelAndProtocolVersion={channel,protocolVersion};
        byte[] data2=new byte[body.readableBytes()];
        body.getBytes(0,data2);

        byte[] data=new byte[flagB.length+len.length+channelAndProtocolVersion.length+data2.length];
        System.arraycopy(flagB, 0, data, 0, flagB.length);
        System.arraycopy(len, 0, data,flagB.length, len.length);
        System.arraycopy(channelAndProtocolVersion, 0, data, flagB.length+ len.length, channelAndProtocolVersion.length);
        System.arraycopy(data2, 0, data, flagB.length+ len.length+channelAndProtocolVersion.length, data2.length);
        int n=CRCUtil.MyCrc16Check(data);
        byte[] tempEdn=CRCUtil.int2Bytes(n);
        byte[] check = new byte[2];
        System.arraycopy(tempEdn, 2, check, 0, 2);
        this.end= Unpooled.buffer(2);
        this.end.writeBytes(check);
//
//        return end;




//        byte[] templen=CRCUtil.short2Bytes((short) (body.readableBytes()+4));
//        this.len=(short) (body.readableBytes()+4);
//        int[] len={templen[0],templen[1]};
//        int[] channelAndProtocolVersion={channel,protocolVersion};
//        int[] data2=new int[body.readableBytes()];
//
//        for (int i=0;i<body.readableBytes();i++){
//            data2[i]=body.array()[i];
//        }
//        int[] data=new int[flagB.length+len.length+channelAndProtocolVersion.length+data2.length];
//        System.arraycopy(flagB, 0, data, 0, flagB.length);
//        System.arraycopy(len, 0, data,flagB.length, len.length);
//        System.arraycopy(channelAndProtocolVersion, 0, data, flagB.length+ len.length, channelAndProtocolVersion.length);
//        System.arraycopy(data2, 0, data, flagB.length+ len.length+channelAndProtocolVersion.length, data2.length);
//        int n=CRCUtil.MyCrc16Check(data);
//        byte[] tempEdn=CRCUtil.int2Bytes(n);
//        byte[] check = new byte[2];
//        System.arraycopy(tempEdn, 2, check, 0, 2);
//        this.end= Unpooled.buffer(2);
//        this.end.writeBytes(check);


        return end;
    }



    public Short getLen() {
        return len;
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






    public ByteBuf getFlag() {

        return flag;
    }

    public void setFlag(ByteBuf flag) {
        this.flag = flag;
    }
}
