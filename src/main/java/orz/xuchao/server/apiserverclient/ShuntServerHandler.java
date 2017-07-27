package orz.xuchao.server.apiserverclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import orz.xuchao.server.bean.BasePackage;
import orz.xuchao.server.bean.CustomMsg;
import orz.xuchao.server.callback.MessageCallBack;
import orz.xuchao.server.utils.CRCUtil;

import java.util.Calendar;
import java.util.Map;


/**
 * Created by xuchao on 2017/7/6 0006.
 * 中转服务器，就三个功能，一个是和门锁服务器服务器集群保持心跳包，二是给门锁端端分配服务器。
 */
public class ShuntServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(ShuntServerHandler.class.getName());


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        String uuid=ctx.channel().id().asLongText();
        System.out.println("设备id为"+uuid+"的设备断开了连接");
        logger.info("设备id为"+uuid+"的设备断开了连接");


        Map<String, SocketChannel> map = ShuntGatewayService.getChannels();
        //遍历map中的值

        for (String key : map.keySet()) {

            if(map.get(key).equals(ctx.channel()))
                System.out.println("移除通道到通道列表"+key);
                ShuntGatewayService.removeGatewayChannel(key);
        }
    }



    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String uuid=ctx.channel().id().asLongText();

        System.out.println("一个客户端连接进来了："+uuid);
        logger.info("一个客户端连接进来了："+uuid);
        logger.info("目前有："+ShuntGatewayService.getChannels().size()+"个设备");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        System.out.println("----------服务器收到命令----------------");
        if(msg instanceof CustomMsg) {
            CustomMsg customMsg = (CustomMsg)msg;
            ByteBuf customMsgFlag=customMsg.getFlag();
            Short customMsgLen=customMsg.getLen();
            byte customMsgChannel=customMsg.getChannel();
            byte customMsgProtocolVersion=customMsg.getProtocolVersion();
            ByteBuf customMsgEnd=customMsg.getEnd();
            ByteBuf customMsgBody=customMsg.getBody();
            byte[] req=new byte[customMsgBody.readableBytes()];
            customMsgBody.readBytes(req);


            switch (req[0]){


                case 0x01: {
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);
                    sb.append("中转服务器收到的包0x01，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！");
                    }else {
                        System.out.println("CRC验证失败！");
                    }

                    byte[] mac = new byte[6];
                    System.arraycopy(req, 1, mac, 0, 6);
                    byte[] time = new byte[4];
                    System.arraycopy(req, 7, time, 0, 4);
                    CRCUtil.bytesToHexString(time);
                    System.out.println("mac地址是："+CRCUtil.bytesToHexString(mac));
                    System.out.println("客户端发出的时间是："+CRCUtil.bytesToTime(time));
                    BasePackage mBasePackage2=new BasePackage();
                    ByteBuf flag=Unpooled.buffer(2);
                    flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                    mBasePackage2.setFlag(flag);
                    mBasePackage2.setChannel((byte) 0x11);
                    mBasePackage2.setProtocolVersion((byte) 0x01);
                    byte[] order={0x01};
                    byte[] byte1=new byte[order.length+mac.length];
                    System.arraycopy(order, 0, byte1, 0, order.length);
                    System.arraycopy(mac, 0, byte1, order.length , mac.length);
                    System.out.println("  api服务器返回门锁0x01指令的结果");
/*
url和port应该是后台管理界面添加的到数据库的
遍历门锁服务器所连接的锁的通道个数，取出一个压力小的服务器给请求的门锁
设置好 url和port就好了
 */
//                    域名凑到200
                    String urls="183.131.66.171";
                    byte[] url1=urls.getBytes();
                    if(url1.length>200){
                        logger.info("门锁服务器url大于200个字节，无法分配对应服务器");
                        return;
                    }
                    byte[] url2=new byte[200-url1.length];
                    byte[] url = new byte[200];
                    System.arraycopy(url1, 0, url, 0, url1.length);
                    System.arraycopy(url2, 0, url, url1.length , url2.length);
//                   port
                    byte[] port={0x23,0x19};
                    byte[] result={0x01};
                    ByteBuf buf=Unpooled.copiedBuffer(port,time,result);
                    byte[] byte2=new byte[buf.readableBytes()];
                    buf.readBytes(byte2);
                    byte[] data = new byte[byte1.length + url.length + byte2.length];
                    System.arraycopy(byte1, 0, data, 0, byte1.length);
                    System.arraycopy(url, 0, data, byte1.length, url.length);
                    System.arraycopy(byte2, 0, data, byte1.length + url.length, byte2.length);
                    ByteBuf body=Unpooled.buffer(data.length);
                    body.writeBytes(data);
                    mBasePackage2.setBody(body);
                    CustomMsg customMsgaa=mBasePackage2.getCustomMsg();
                    byte[] ee2=new byte[2];
                    customMsgaa.getEnd().getBytes(0,ee2);
                    ctx.writeAndFlush(customMsgaa);
                    System.out.println("api服务器给锁分配所服务器，地址为："+urls);



                }
                    break;

                case 0x02:{
                    StringBuffer sb=new StringBuffer();
//                    读取锁服务器和门锁的mac地址
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);


                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！");
                    }else {
                        System.out.println("CRC验证失败");
                    }
                    System.out.println(CRCUtil.bytesToHexString(req)+".");
                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);
                    byte[] serverMac = new byte[6];
                    System.arraycopy(req, 7, serverMac, 0, 6);

                    byte[] time = new byte[4];
                    System.arraycopy(req, 13, time, 0, 4);

                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));
                    System.out.println("门锁服务器的mac地址是："+CRCUtil.bytesToHexString(serverMac));
                    System.out.println("时间是："+CRCUtil.bytesToTime(time));
                    System.out.println("服务器返回客户端0x21指令的结果");

                    /*
                    存储锁的mac地址lockMac以及其所连接的门锁服务器的mac地址serverMac,以及存储时间
                     */

                    ShuntGatewayService.addGatewayChannel(CRCUtil.bytesToHexString(serverMac), (SocketChannel) ctx.channel());
                    TempMacManagerService.lockMAC=CRCUtil.bytesToHexString(lockMac);
                    TempMacManagerService.serverMAC=CRCUtil.bytesToHexString(serverMac);
                    TempMacManagerService.lockMACB=lockMac;
                    TempMacManagerService.serverMACB=serverMac;
                    BasePackage mBasePackage2=new BasePackage();
                    ByteBuf flag=Unpooled.buffer(2);
                    flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                    mBasePackage2.setFlag(flag);
                    mBasePackage2.setChannel((byte) 0x11);
                    mBasePackage2.setProtocolVersion((byte) 0x01);
                    byte[] result={0x01};
                    ByteBuf returnByteBuf=Unpooled.copiedBuffer(req,result);
                    mBasePackage2.setBody(returnByteBuf);
                    CustomMsg customMsgaa=mBasePackage2.getCustomMsg();
                    byte[] ee2=new byte[2];
                    customMsgaa.getEnd().getBytes(0,ee2);
                    ctx.writeAndFlush(customMsgaa);


                }
                break;
                case 0x03:{
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);
                    System.out.println("api 收到的命令0x03");
                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);

                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功");
                    }else {
                        System.out.println("CRC验证失败");
                    }

                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);

                    byte[] id = new byte[8];
                    System.arraycopy(req, 7, id, 0, 8);

                    byte[] time = new byte[4];
                    System.arraycopy(req, 15, time, 0, 4);


                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));
                    System.out.println("id是："+CRCUtil.bytesToHexString(id));
                    System.out.println("时间是："+CRCUtil.bytesToHexString(time));
                    System.out.println("0x03指令操作完成 ");

                    /*
                    记录从mac地址为lockMac上读取了一个条ui为 id的信息，时间为time
                     */




                }
                break;


                case 0x06:{
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);
                    sb.append("中转服务器/api 收到的包0x36，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    System.out.println("收到指令0x06");
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功");
                    }else {
                        System.out.println("CRC验证失败！");
                    }
                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);

                    byte[] id = new byte[8];
                    System.arraycopy(req, 7, id, 0, 8);

                    byte[] type = new byte[1];
                    System.arraycopy(req, 15, type, 0, 1);

                    byte[] time = new byte[4];
                    System.arraycopy(req, 16, time, 0, 4);


                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));
                    System.out.println("id是："+CRCUtil.bytesToHexString(id));
                    System.out.println("开门type是："+CRCUtil.bytesToHexString(type));
                    System.out.println("0x06指令操作完成 ");

   /*
                    记录身份证为id的用户在时间time的时候，开了mac为lockMac的锁 到数据库


                     */




                }
                break;

                case 0x0C:{
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);

                    sb.append("中转服务器/api 收到的包0x3C，包尾是--->"+ CRCUtil.bytesToHexString(ee));

                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    System.out.println("收到指令0x0C");
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！\r\n");
                    }else {
                        System.out.println("CRC验证失败！\r\n");
                    }
                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);
                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));
                    System.out.println("0x0C指令操作完成 ");

   /*

                     给定时间范围内的开门日志写入到数据库


                     */




                }
                break;
                case 0x0D:{
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);

                    sb.append("中转服务器/api 收到的包0x3D，包尾是--->"+ CRCUtil.bytesToHexString(ee));

                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    System.out.println("收到指令0x0D");
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！\r\n");
                    }else {
                        System.out.println("CRC验证失败！\r\n");
                    }

                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);
                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));


                    System.out.println("0x0D指令操作完成 ");
   /*
                     发送升级指令完成


                     */




                }
                break;
                case 0x0E:{
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);

                    sb.append("中转服务器/api 收到的包0x3E，包尾是--->"+ CRCUtil.bytesToHexString(ee));

                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    System.out.println("收到指令0x0E");
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！\r\n");
                    }else {
                        System.out.println("CRC验证失败！\r\n");
                    }

                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);
                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));

                    System.out.println("0x0E指令操作完成 ");

   /*
                     给定时间范围内的开门日志写入到数据库


                     */




                }
                break;
                case 0x04:{
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);

                    sb.append("中转服务器/api 收到的包0x34，包尾是--->"+ CRCUtil.bytesToHexString(ee));

                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    System.out.println("收到指令0x04");
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！\r\n");
                    }else {
                        System.out.println("CRC验证失败！\r\n");
                    }
                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);

                    byte[] id = new byte[8];
                    System.arraycopy(req, 7, id, 0, 8);

                    byte[] effectiveDate = new byte[4];
                    System.arraycopy(req, 15, effectiveDate, 0, 4);

                    byte[] time = new byte[4];
                    System.arraycopy(req, 19, time, 0, 4);


                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));
                    System.out.println("id是："+CRCUtil.bytesToHexString(id));
                    System.out.println("有效期是："+CRCUtil.bytesToTime(effectiveDate));
                    System.out.println("时间是："+CRCUtil.bytesToTime(time));

                    System.out.println("0x04指令操作完成 ");




   /*
                     写一条UID和有效期到智能门禁完成
                     */




                }
                break;

                case 0x05:{
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);
                    sb.append("中转服务器/api 收到的包0x35，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    System.out.println("收到指令0x05");
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！\r\n");
                    }else {
                        System.out.println("CRC验证失败！\r\n");
                    }

                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);

                    byte[] id = new byte[8];
                    System.arraycopy(req, 7, id, 0, 8);

                    byte[] time = new byte[4];
                    System.arraycopy(req, 15, time, 0, 4);


                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));
                    System.out.println("d是："+CRCUtil.bytesToHexString(id));
                    System.out.println("时间是："+CRCUtil.bytesToTime(time));

                    System.out.println("0x05指令操作完成 ");




   /*
                     删除一条UID和有效期到智能门禁完成
                     */



                }
                break;

                case 0x07:{
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);
                    sb.append("中转服务器/api 收到的包0x37，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    System.out.println("api服务器收到指令0x07");
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！");
                    }else {
                        System.out.println("CRC验证失败！");
                    }
                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);

                    byte[] id = new byte[8];
                    System.arraycopy(req, 7, id, 0, 8);

                    byte[] type = new byte[1];
                    System.arraycopy(req, 15, type, 0, 1);

                    byte[] time = new byte[4];
                    System.arraycopy(req, 16, time, 0, 4);


                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));
                    System.out.println("id是："+CRCUtil.bytesToHexString(id));
                    System.out.println("开门方式是："+CRCUtil.bytesToHexString(type));
                    System.out.println("时间是："+CRCUtil.bytesToTime(time));
                    System.out.println("0x07指令操作完成 ");



   /*
                     记录身份证为(id)的用户在时间(time)的时候，用(type)的方式开了mac为(lockMac)的锁到数据库

                     */

                    BasePackage mBasePackage2=new BasePackage();
                    ByteBuf flag=Unpooled.buffer(2);
                    flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                    mBasePackage2.setFlag(flag);
                    mBasePackage2.setChannel((byte) 0x11);
                    mBasePackage2.setProtocolVersion((byte) 0x01);
                    byte[] result={0x01};
                    ByteBuf returnByteBuf=Unpooled.copiedBuffer(req,result);
                    mBasePackage2.setBody(returnByteBuf);
                    CustomMsg customMsgaa=mBasePackage2.getCustomMsg();
                    byte[] ee2=new byte[2];
                    customMsgaa.getEnd().getBytes(0,ee2);
                    ctx.writeAndFlush(customMsgaa);


                }
                break;
                case 0x0A:{
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);

                    sb.append("中转服务器/api 收到的包0x3A，包尾是--->"+ CRCUtil.bytesToHexString(ee));

                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    System.out.println("收到指令0x0A");
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！\r\n");
                    }else {
                        System.out.println("CRC验证失败！\r\n");
                    }

                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);
                    byte[] time = new byte[4];
                    System.arraycopy(req, 7, time, 0, 4);

                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));
                    System.out.println("时间是："+CRCUtil.bytesToTime(time));


                    System.out.println("0x0A指令操作完成 ");



                    BasePackage mBasePackage2=new BasePackage();
                    ByteBuf flag=Unpooled.buffer(2);
                    flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                    mBasePackage2.setFlag(flag);
                    mBasePackage2.setChannel((byte) 0x11);
                    mBasePackage2.setProtocolVersion((byte) 0x01);
                    Calendar calendar2= Calendar.getInstance();
                    byte[] rightTime= CRCUtil.timeToBytes(calendar2);

                    byte[] result={0x01};
                    ByteBuf returnByteBuf=Unpooled.copiedBuffer(req,rightTime,result);
                    mBasePackage2.setBody(returnByteBuf);
                    CustomMsg customMsgaa=mBasePackage2.getCustomMsg();
                    byte[] ee2=new byte[2];
                    customMsgaa.getEnd().getBytes(0,ee2);
                    ctx.writeAndFlush(customMsgaa);


                }
                break;
                case 0x0B:{
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);

                    sb.append("中转服务器/api 收到的包0x3B，包尾是--->"+ CRCUtil.bytesToHexString(ee));

                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    System.out.println("收到指令0x0B");
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！\r\n");
                    }else {
                        System.out.println("CRC验证失败！\r\n");
                    }

                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);
                    byte[] warningType = new byte[1];
                    System.arraycopy(req, 7, warningType, 0, 1);
                    byte[] time = new byte[4];
                    System.arraycopy(req, 8, time, 0, 4);


                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));
                    System.out.println("报警类型是："+CRCUtil.bytesToHexString(warningType));
                    System.out.println("时间是："+CRCUtil.bytesToTime(time));
                    System.out.println("服务器返回客户端0x3B指令的结果 ");

                    System.out.println("0x0B指令操作完成 ");




   /*
                     记录mac地址为（lockMac）的门锁在（time）时间发出了(warningType)类型的警报 到数据库

                     */

                    BasePackage mBasePackage2=new BasePackage();
                    ByteBuf flag=Unpooled.buffer(2);
                    flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                    mBasePackage2.setFlag(flag);
                    mBasePackage2.setChannel((byte) 0x11);
                    mBasePackage2.setProtocolVersion((byte) 0x01);
                    Calendar calendar2= Calendar.getInstance();
                    byte[] rightTime= CRCUtil.timeToBytes(calendar2);

                    byte[] result={0x01};
                    ByteBuf returnByteBuf=Unpooled.copiedBuffer(req,rightTime,result);
                    mBasePackage2.setBody(returnByteBuf);
                    CustomMsg customMsgaa=mBasePackage2.getCustomMsg();
                    byte[] ee2=new byte[2];
                    customMsgaa.getEnd().getBytes(0,ee2);
                    ctx.writeAndFlush(customMsgaa);


                }
                break;
                case 0x08:{
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);

                    sb.append("中转服务器/api 收到的包0x08，包尾是--->"+ CRCUtil.bytesToHexString(ee));

                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);
                    System.out.println("收到指令0x08");
                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！\r\n");
                    }else {
                        System.out.println("CRC验证失败！\r\n");
                    }
                    byte[] order=new byte[1];
                    System.arraycopy(req, 0, order, 0, 1);
                    byte[] lockMac = new byte[6];
                    System.arraycopy(req, 1, lockMac, 0, 6);
                    byte[] type = new byte[1];
                    System.arraycopy(req, 7, type, 0, 1);
                    byte[] time = new byte[4];
                    System.arraycopy(req, 8, time, 0, 4);


                    System.out.println("门锁的mac地址是："+CRCUtil.bytesToHexString(lockMac));
                    System.out.println("门状态是："+CRCUtil.bytesToHexString(type));
                    System.out.println("时间是："+CRCUtil.bytesToTime(time));
                    System.out.println("服务器返回客户端0x08指令的结果");




   /*
                     记录mac地址为（lockMac）的门锁在（time）时间发出了(warningType)类型的警报 到数据库

                     */

                    BasePackage mBasePackage2=new BasePackage();
                    ByteBuf flag=Unpooled.buffer(2);
                    flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                    mBasePackage2.setFlag(flag);
                    mBasePackage2.setChannel((byte) 0x11);
                    mBasePackage2.setProtocolVersion((byte) 0x01);
                    Calendar calendar2= Calendar.getInstance();
                    byte[] rightTime= CRCUtil.timeToBytes(calendar2);

                    byte[] result={0x01};
                    ByteBuf returnByteBuf=Unpooled.copiedBuffer(order,lockMac,time,rightTime,result);
                    mBasePackage2.setBody(returnByteBuf);
                    CustomMsg customMsgaa=mBasePackage2.getCustomMsg();
                    byte[] ee2=new byte[2];
                    customMsgaa.getEnd().getBytes(0,ee2);
                    ctx.writeAndFlush(customMsgaa);


                }
                break;
                case 0x09:{
                    StringBuffer sb=new StringBuffer();
                    BasePackage mBasePackage=new BasePackage();
                    ByteBuf customMsgBody2=Unpooled.buffer(req.length);
                    customMsgBody2.writeBytes(req);
                    byte[] ee=new byte[2];
                    customMsgEnd.getBytes(0,ee);

                    mBasePackage.setCustomMsgAttribute(customMsgFlag,customMsgLen,customMsgChannel,customMsgProtocolVersion,customMsgBody2,customMsgEnd);

                    if( mBasePackage.checkCRC()){
                        System.out.println("CRC验证成功！\r\n");
                    }else {
                        System.out.println("CRC验证失败！\r\n");
                    }
                   System.out.println("清空所有uid和有效期0x09");


                }
                break;

               default:
                break;
            }
        }



    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
