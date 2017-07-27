package orz.xuchao.server.code;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import orz.xuchao.server.bean.CustomMsg;

/**
 * Created by Administrator on 2017/7/7 0007.
 */
public class CustomDecoder extends LengthFieldBasedFrameDecoder {
    //判断传送客户端传送过来的数据是否按照协议传输，头部信息的大小应该是 byte+byte+int = 1+1+4 = 6
    private static final int HEADER_SIZE =2;
    private ByteBuf flag;//开始标识符号
    private Short len;//长度
    private byte channel;//信道
    private byte protocolVersion;//协议版本
    private ByteBuf body;//包体
    private ByteBuf end;//包尾

    /**
     *
     * @param maxFrameLength 解码时，处理每个帧数据的最大长度
     * @param lengthFieldOffset 该帧数据中，存放该帧数据的长度的数据的起始位置
     * @param lengthFieldLength 记录该帧数据长度的字段本身的长度
     * @param lengthAdjustment 修改帧数据长度字段中定义的值，可以为负数
     * @param initialBytesToStrip 解析的时候需要跳过的字节数
     * @param failFast 为true，当frame长度超过maxFrameLength时立即报TooLongFrameException异常，为false，读取完整个帧再报异常
     */
    public CustomDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                         int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength,
                lengthAdjustment, initialBytesToStrip, failFast);
    }



    //    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf mByteBuf= (ByteBuf) super.decode(ctx, in);

//        ByteBuf mByteBuf=in;


        if (mByteBuf == null) {
            return null;
        }
        if (mByteBuf.readableBytes() < 5) {
            throw new Exception("可读信息段比头部信息都小，你在逗我？");
        }
        //注意在读的过程中，readIndex的指针也在移动
        flag=mByteBuf.readBytes(2);
        len = mByteBuf.readShort();
        if (mByteBuf.readableBytes() < len) {
            throw new Exception("body字段你告诉我长度是"+len+",但是真实情况是"+mByteBuf.readableBytes()+"，你又逗我？");
        }
        channel = mByteBuf.readByte();
        protocolVersion = mByteBuf.readByte();//协议版本
        body=mByteBuf.readBytes(len-4);
        end=mByteBuf.readBytes(2);
        CustomMsg customMsg = new  CustomMsg(flag,len,channel,protocolVersion,body, end);
        return customMsg;
    }
}
