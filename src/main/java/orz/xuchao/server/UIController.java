package orz.xuchao.server;

import com.jfinal.core.Controller;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.SocketChannel;
import orz.xuchao.server.apiserverclient.ShuntGatewayService;
import orz.xuchao.server.apiserverclient.ShuntServer;
import orz.xuchao.server.apiserverclient.TempMacManagerService;
import orz.xuchao.server.bean.BasePackage;
import orz.xuchao.server.bean.CustomMsg;
import orz.xuchao.server.callback.MessageCallBack;
import orz.xuchao.server.utils.CRCUtil;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class UIController extends Controller{
    public void index(){
        render("/WEB-INF/tpl/index.html");
    }
    String message = "";



    public void callback(){
        int t = getParaToInt("t");



       if(t==1){
           int port = 8984;
           System.out.println("启动服务器");
           ShuntServer mTimeServer= new ShuntServer();
           //        启动客户端
           try {
               mTimeServer.bind(port);
           } catch (Exception e) {
               e.printStackTrace();
           }
           message = "启动服务器";
       }else if(t==2){


           try {
               System.out.println("发送开门指令0x06");
               System.out.println("从通道列表去出"+ TempMacManagerService.serverMAC+"的通道");
               SocketChannel obj = ShuntGatewayService.getGatewayChannel(TempMacManagerService.serverMAC);
               BasePackage mBasePackage=new BasePackage();
               ByteBuf flag= Unpooled.buffer(2);
               flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
               mBasePackage.setFlag(flag);
               mBasePackage.setChannel((byte) 0x01);
               mBasePackage.setProtocolVersion((byte) 0x01);
               byte[] orlder={0x06};
               byte[] mac=TempMacManagerService.lockMACB;
               byte[] id=Config.id;
               byte[] type={0x01};
               Calendar calendar = Calendar.getInstance();
               byte[] time= CRCUtil.timeToBytes(calendar);
               ByteBuf body=Unpooled.copiedBuffer(orlder,mac,id,type,time);
               mBasePackage.setBody(body);
               CustomMsg customMsg=mBasePackage.getCustomMsg();
               byte[] ee=new byte[2];
               customMsg.getEnd().getBytes(0,ee);
               obj.writeAndFlush(customMsg);

           } catch (Exception e1) {
               e1.printStackTrace();
           }
           message = "发送开门指令";
       }else if(t==3){
           try {
               System.out.println("从智能门禁读取一条UID指令");


               System.out.println("==UIController==>发送开门指令0x03");
              /* 根据app上传的锁的lockMac地址去数据库搜到所管理它的锁服务器serverMAC地址
              通过SocketChannel obj = ShuntGatewayService.getGatewayChannel(serverMAC)拿到通道obj
              同时把lockMac赋值给mac
               */


               SocketChannel obj = ShuntGatewayService.getGatewayChannel(TempMacManagerService.serverMAC);
               BasePackage mBasePackage=new BasePackage();
               ByteBuf flag= Unpooled.buffer(2);
               flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
               mBasePackage.setFlag(flag);
               mBasePackage.setChannel((byte) 0x01);
               mBasePackage.setProtocolVersion((byte) 0x01);

               byte[] orlder={0x03};
               byte[] mac=TempMacManagerService.lockMACB;
               Calendar calendar = Calendar.getInstance();
               byte[] time= CRCUtil.timeToBytes(calendar);
               ByteBuf body=Unpooled.copiedBuffer(orlder,mac,time);
               mBasePackage.setBody(body);
               CustomMsg customMsg=mBasePackage.getCustomMsg();
               byte[] ee=new byte[2];
               customMsg.getEnd().getBytes(0,ee);
               System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
               obj.writeAndFlush(customMsg);

           } catch (Exception e1) {
               e1.printStackTrace();
           }
           message = "从智能门禁读取一条UID指令";



       }else if(t==4){
           try {
               System.out.println("写一条UID和有效期到智能门禁指令");

              /* api服务器管理后台写一条UID（id） 和 有效日期(effectiveDate)到mac地址为(mac)的门禁
              根据(mac)去服务器搜到对应的服务器serverMAC
               通过SocketChannel obj = ShuntGatewayService.getGatewayChannel(serverMAC)拿到通道obj
               */


               SocketChannel obj = ShuntGatewayService.getGatewayChannel(TempMacManagerService.serverMAC);
               BasePackage mBasePackage=new BasePackage();
               ByteBuf flag=Unpooled.buffer(2);
               flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
               mBasePackage.setFlag(flag);
               mBasePackage.setChannel((byte) 0x01);
               mBasePackage.setProtocolVersion((byte) 0x01);
               byte[] orlder={0x04};
               byte[] mac=TempMacManagerService.lockMACB;
               byte[] id=Config.id;


               long curren = System.currentTimeMillis();
               curren +=  2*60* 1000;
               Date da = new Date(curren);
               Calendar calendar = Calendar.getInstance();
               calendar.setTime(da);
               byte[] effectiveDate= CRCUtil.timeToBytes(calendar);

               System.out.println("有效时间："+CRCUtil.bytesToTime(effectiveDate));

               Calendar calendar1 = Calendar.getInstance();
               byte[] time= CRCUtil.timeToBytes(calendar1);
               ByteBuf body=Unpooled.copiedBuffer(orlder,mac,id,effectiveDate,time);
               mBasePackage.setBody(body);
               CustomMsg customMsg=mBasePackage.getCustomMsg();
               byte[] ee=new byte[2];
               customMsg.getEnd().getBytes(0,ee);
               obj.writeAndFlush(customMsg);

           } catch (Exception e1) {
               e1.printStackTrace();
           }
           message = "从智能门禁读取一条UID指令";



       }else if(t==5){
           message = "删除一条UID和有效期到智能门禁指令";

           System.out.println("删除一条UID和有效期到智能门禁指令");
           /* api服务器管理后台到mac地址为(mac)的门禁删除一条UID(id)
              根据(mac)去数据库搜到对应的服务器serverMAC
               通过SocketChannel obj = ShuntGatewayService.getGatewayChannel(serverMAC)拿到通道obj
               */
           SocketChannel obj = ShuntGatewayService.getGatewayChannel(TempMacManagerService.serverMAC);
           BasePackage mBasePackage=new BasePackage();
           ByteBuf flag=Unpooled.buffer(2);
           flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
           mBasePackage.setFlag(flag);
           mBasePackage.setChannel((byte) 0x01);
           mBasePackage.setProtocolVersion((byte) 0x01);
           byte[] orlder={0x05};
           byte[] mac=TempMacManagerService.lockMACB;
           byte[] id=Config.id;
           Calendar calendar1 = Calendar.getInstance();
           byte[] time= CRCUtil.timeToBytes(calendar1);
           ByteBuf body=Unpooled.copiedBuffer(orlder,mac,id,time);
           mBasePackage.setBody(body);
           CustomMsg customMsg=mBasePackage.getCustomMsg();
           byte[] ee=new byte[2];
           customMsg.getEnd().getBytes(0,ee);
           System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
           obj.writeAndFlush(customMsg);
       }else if(t==6){
           message = "清空智能门禁所有UID和有效期指令";
           System.out.println("清空智能门禁所有UID和有效期指令");
           /* api服务器管理后台到mac地址为(mac)的门禁清空清空智能门禁所有UID和有效期指令
              根据(mac)去数据库搜到对应的服务器serverMAC
               通过SocketChannel obj = ShuntGatewayService.getGatewayChannel(serverMAC)拿到通道obj
               */
           SocketChannel obj = ShuntGatewayService.getGatewayChannel(TempMacManagerService.serverMAC);
           BasePackage mBasePackage=new BasePackage();
           ByteBuf flag=Unpooled.buffer(2);
           flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
           mBasePackage.setFlag(flag);
           mBasePackage.setChannel((byte) 0x01);
           mBasePackage.setProtocolVersion((byte) 0x01);
           byte[] orlder={0x09};
           byte[] mac=TempMacManagerService.lockMACB;
           Calendar calendar1 = Calendar.getInstance();
           byte[] time= CRCUtil.timeToBytes(calendar1);
           ByteBuf body=Unpooled.copiedBuffer(orlder,mac,time);
           mBasePackage.setBody(body);
           CustomMsg customMsg=mBasePackage.getCustomMsg();
           byte[] ee=new byte[2];
           customMsg.getEnd().getBytes(0,ee);
           System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
           obj.writeAndFlush(customMsg);
       }else if(t==7){
           message = "读取给定时间范围内开门日志指令";
           System.out.println("读取给定时间范围内开门日志指令");
           /* api服务器管理后台到mac地址为(mac)的门禁清空清空智能门禁所有UID和有效期指令
              根据(mac)去数据库搜到对应的服务器serverMAC
               通过SocketChannel obj = ShuntGatewayService.getGatewayChannel(serverMAC)拿到通道obj
               */
           SocketChannel obj = ShuntGatewayService.getGatewayChannel(TempMacManagerService.serverMAC);
           BasePackage mBasePackage=new BasePackage();
           ByteBuf flag=Unpooled.buffer(2);
           flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
           mBasePackage.setFlag(flag);
           mBasePackage.setChannel((byte) 0x01);
           mBasePackage.setProtocolVersion((byte) 0x01);
           byte[] orlder={0x0C};
           byte[] mac=TempMacManagerService.lockMACB;
           Calendar calendar1 = Calendar.getInstance();
           calendar1.set(2017,5,10);
           byte[] startTime= CRCUtil.timeToBytes(calendar1);
           Calendar calendar2 = Calendar.getInstance();
           calendar2.set(2017,6,22);
           byte[] endTime= CRCUtil.timeToBytes(calendar2);
           Calendar calendar3 = Calendar.getInstance();
           byte[] time= CRCUtil.timeToBytes(calendar3);
           byte[] groupNo={0x00,0x02};


           ByteBuf body=Unpooled.copiedBuffer(orlder,mac,startTime,endTime,groupNo,time);
           mBasePackage.setBody(body);
           CustomMsg customMsg=mBasePackage.getCustomMsg();
           byte[] ee=new byte[2];
           customMsg.getEnd().getBytes(0,ee);
           System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
           obj.writeAndFlush(customMsg);
       }else if(t==8){
           message = "服务器向智能门禁发送远程升级指令";
           System.out.println("服务器向智能门禁发送远程升级指令");
           /* api服务器管理后台到mac地址为(mac)的门禁,要求其去（url）这个网址的（port）端口,升级到(version)版本
              根据(mac)去数据库搜到对应的服务器serverMAC
               通过SocketChannel obj = ShuntGatewayService.getGatewayChannel(serverMAC)拿到通道obj
               */
           SocketChannel obj = ShuntGatewayService.getGatewayChannel(TempMacManagerService.serverMAC);
           BasePackage mBasePackage=new BasePackage();
           ByteBuf flag=Unpooled.buffer(2);
           flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
           mBasePackage.setFlag(flag);
           mBasePackage.setChannel((byte) 0x01);
           mBasePackage.setProtocolVersion((byte) 0x01);


           try {
               byte[] orlder={0x0D};
               byte[] mac=TempMacManagerService.lockMACB;
               String urls="127.0.0.1";
               byte[] url1 = urls.getBytes("utf-8");
               if(url1.length>200){
                   System.out.println("门锁服务器url大于200个字节，无法分配对应服务器");
                   return;
               }
               byte[] url2=new byte[200-url1.length];
               byte[] url = new byte[200];
               System.arraycopy(url1, 0, url, 0, url1.length);
               System.arraycopy(url2, 0, url, url1.length , url2.length);
//                   port
               byte[] port={0x59,0x53};
               byte[] version={0x00,0x02};
               Calendar calendar = Calendar.getInstance();
               byte[] time= CRCUtil.timeToBytes(calendar);


               ByteBuf body=Unpooled.copiedBuffer(orlder,mac,url,port,version,time);
               mBasePackage.setBody(body);
               CustomMsg customMsg=mBasePackage.getCustomMsg();
               byte[] ee=new byte[2];
               customMsg.getEnd().getBytes(0,ee);
               System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
               obj.writeAndFlush(customMsg);
           } catch (UnsupportedEncodingException e) {
               e.printStackTrace();
           }

       }else if(t==9){
           message = "服务器向智能门禁发送远程重启";
           System.out.println("服务器向智能门禁发送远程重启");
           /* api服务器管理后台到mac地址为(mac)的门禁要求其重启
              根据(mac)去数据库搜到对应的服务器serverMAC
               通过SocketChannel obj = ShuntGatewayService.getGatewayChannel(serverMAC)拿到通道obj
               */
           SocketChannel obj = ShuntGatewayService.getGatewayChannel(TempMacManagerService.serverMAC);
           BasePackage mBasePackage=new BasePackage();
           ByteBuf flag=Unpooled.buffer(2);
           flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
           mBasePackage.setFlag(flag);
           mBasePackage.setChannel((byte) 0x01);
           mBasePackage.setProtocolVersion((byte) 0x01);
           byte[] orlder={0x0E};
           byte[] mac=TempMacManagerService.lockMACB;
           Calendar calendar = Calendar.getInstance();
           byte[] time= CRCUtil.timeToBytes(calendar);


           ByteBuf body=Unpooled.copiedBuffer(orlder,mac,time);
           mBasePackage.setBody(body);
           CustomMsg customMsg=mBasePackage.getCustomMsg();
           byte[] ee=new byte[2];
           customMsg.getEnd().getBytes(0,ee);
           System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
           obj.writeAndFlush(customMsg);
       }




        Map<String, Object> result = new HashMap<>();
        result.put("message", message);

        renderJson(result);
    }


}
