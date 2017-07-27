package orz.xuchao.server.apiserverclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.SocketChannel;
import orz.xuchao.server.bean.BasePackage;
import orz.xuchao.server.bean.CustomMsg;
import orz.xuchao.server.utils.CRCUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Administrator on 2017/7/5 0005.
 */
public class ShuntServerUI extends JFrame{

    private  JButton sendB7;
    private  JButton sendB4;
    private  JButton sendB5;
    private  JButton sendB6;
    private JTextArea receivedTF;
    private JButton sendB,sendB1,sendB2,sendB3;
    private JLabel jl1;

    private int port;


    public ShuntServerUI(int port) {
        this.port=port;
        this.setTitle("api服务器端");
        this.setBounds(0, 0, 600, 600);
        // 窗体大小不能改变
        this.setResizable(true);
        // 居中显示
        this.setLocationRelativeTo(null);
        // 窗体可见
        this.setVisible(true);

        // 创建一个容器
        Container con = this.getContentPane();



        // 按钮设定
        sendB = new JButton("从智能门禁读取一条UID指令 0x03");
        sendB.setBounds(50, 40, 500, 20);  // 按钮设定

        sendB1 = new JButton("写一条UID和有效期到智能门禁指令 0x04");
        sendB1.setBounds(50, 70, 500, 20);

        sendB2 = new JButton("从智能门禁删除一条UID指令 0x05");
        sendB2.setBounds(50, 100, 500, 20);

        sendB3 = new JButton("开门指令 0x06");
        sendB3.setBounds(50, 130, 500, 20);

        sendB4 = new JButton("清空智能门禁所有UID和有效期指令 0x09");
        sendB4.setBounds(50, 160, 500, 20);

        sendB5 = new JButton(" 读取给定时间范围内开门日志 0x0C");
        sendB5.setBounds(50, 190, 500, 20);

        sendB6 = new JButton("服务器向智能门禁发送远程升级指令 0x0D");
        sendB6.setBounds(50, 220, 500, 20);

        sendB7 = new JButton("服务器向智能门禁发送远程重启指令 0x0E");
        sendB7.setBounds(50, 250, 500, 20);

         receivedTF= new JTextArea();
        //在文本框上添加滚动条
        JScrollPane jsp = new JScrollPane(receivedTF);
        //设置矩形大小.参数依次为(矩形左上角横坐标x,矩形左上角纵坐标y，矩形长度，矩形宽度)
        jsp.setBounds(50, 280, 800, 500);
        //默认的设置是超过文本框才会显示滚动条，以下设置让滚动条一直显示
        jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        jl1 = new JLabel();
        jl1.setBounds(0, 0, 355, 265);

        // 给按钮添加1个事件
        sendB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                    try {

                        System.out.println("从通道列表去出"+TempMacManagerService.serverMAC+"的通道");
                        System.out.println(ShuntGatewayService.getChannels().size());



                            SocketChannel obj = ShuntGatewayService.getGatewayChannel(TempMacManagerService.serverMAC);
                            BasePackage mBasePackage=new BasePackage();
                            ByteBuf flag=Unpooled.buffer(2);
                            flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                            mBasePackage.setFlag(flag);
                            mBasePackage.setChannel((byte) 0x01);
                            mBasePackage.setProtocolVersion((byte) 0x01);
//                            byte[] bbody={0x26
//                                    ,0x01,0x02,0x03,0x04,0x05,0x06
//                                    ,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08
//                                    ,0x01
//                                    ,0x01,0x02,0x03,0x04};
//                            ByteBuf body=Unpooled.buffer(bbody.length);
//                            body.writeBytes(bbody);

                            byte[] orlder={0x26};
                            byte[] result=TempMacManagerService.lockMACB;
                            byte[] id={0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x07};
                            byte[] type={0x01};
                            Calendar calendar = Calendar.getInstance();
                            byte[] time=CRCUtil.timeToBytes(calendar);
                            ByteBuf body=Unpooled.copiedBuffer(orlder,result,id,type,time);
                            mBasePackage.setBody(body);
                            CustomMsg customMsg=mBasePackage.getCustomMsg();
                            byte[] ee=new byte[2];
                            customMsg.getEnd().getBytes(0,ee);
                            System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                            obj.writeAndFlush(customMsg);

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

            }
        });
        // 给按钮添加1个事件
        sendB1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {





                    Map<String, SocketChannel> map = TempTestChannelManagerService.getChannels();
                    Iterator<String> it = map.keySet().iterator();
//                        无筛选向所有客户端发消息
                    while (it.hasNext()) {
                        String key = it.next();
                        SocketChannel obj = map.get(key);
                        System.out.println("服务器写一条UID和有效期到编号为 "+key+" 的智能门禁指令 0x04");


                        BasePackage mBasePackage=new BasePackage();
                        ByteBuf flag=Unpooled.buffer(2);
                        flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                        mBasePackage.setFlag(flag);
                        mBasePackage.setChannel((byte) 0x01);
                        mBasePackage.setProtocolVersion((byte) 0x01);
                        byte[] bbody={0x04
                                ,0x01,0x02,0x03,0x04,0x05,0x06
                                ,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08
                                ,0x01,0x02,0x03,0x04
                                ,0x01,0x02,0x03,0x04};
                        ByteBuf body=Unpooled.buffer(bbody.length);
                        body.writeBytes(bbody);
                        mBasePackage.setBody(body);
                        CustomMsg customMsg=mBasePackage.getCustomMsg();
                        byte[] ee=new byte[2];
                        customMsg.getEnd().getBytes(0,ee);
                        System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                        obj.writeAndFlush(customMsg);


                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });
        // 给按钮添加1个事件
        sendB2.addActionListener(new ActionListener() {


            public void actionPerformed(ActionEvent e) {
                try {
                    Map<String, SocketChannel> map = TempTestChannelManagerService.getChannels();
                    Iterator<String> it = map.keySet().iterator();
//                        无筛选向所有客户端发消息
                    while (it.hasNext()) {
                        String key = it.next();
                        SocketChannel obj = map.get(key);
                        System.out.println("服务器从编号为 "+key+" 的智能门禁删除一条UID 0x05");
                        BasePackage mBasePackage=new BasePackage();
                        ByteBuf flag=Unpooled.buffer(2);
                        flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                        mBasePackage.setFlag(flag);
                        mBasePackage.setChannel((byte) 0x01);
                        mBasePackage.setProtocolVersion((byte) 0x01);
                        byte[] bbody={0x05
                                ,0x01,0x02,0x03,0x04,0x05,0x06
                                ,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08
                                ,0x01,0x02,0x03,0x04};
                        ByteBuf body=Unpooled.buffer(bbody.length);
                        body.writeBytes(bbody);
                        mBasePackage.setBody(body);
                        CustomMsg customMsg=mBasePackage.getCustomMsg();
                        byte[] ee=new byte[2];
                        customMsg.getEnd().getBytes(0,ee);
                        System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                        obj.writeAndFlush(customMsg);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });
        // 给按钮添加1个事件
        sendB3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    Map<String, SocketChannel> map = TempTestChannelManagerService.getChannels();
                    Iterator<String> it = map.keySet().iterator();
//                        无筛选向所有客户端发消息
                    while (it.hasNext()) {
                        String key = it.next();
                        SocketChannel obj = map.get(key);
                        System.out.println("服务器给编号为"+key+"的门禁发送开门指令 0x06");

                        BasePackage mBasePackage=new BasePackage();
                        ByteBuf flag=Unpooled.buffer(2);
                        flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                        mBasePackage.setFlag(flag);
                        mBasePackage.setChannel((byte) 0x01);
                        mBasePackage.setProtocolVersion((byte) 0x01);
                        byte[] bbody={0x06
                                ,0x01,0x02,0x03,0x04,0x05,0x06
                                ,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08
                                ,0x01
                                ,0x01,0x02,0x03,0x04};
                        ByteBuf body=Unpooled.buffer(bbody.length);
                        body.writeBytes(bbody);
                        mBasePackage.setBody(body);
                        CustomMsg customMsg=mBasePackage.getCustomMsg();
                        byte[] ee=new byte[2];
                        customMsg.getEnd().getBytes(0,ee);
                        System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                        obj.writeAndFlush(customMsg);


                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });
        // 给按钮添加1个事件
        sendB4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Map<String, SocketChannel> map = TempTestChannelManagerService.getChannels();
                    Iterator<String> it = map.keySet().iterator();
//                        无筛选向所有客户端发消息
                    while (it.hasNext()) {
                        String key = it.next();
                        SocketChannel obj = map.get(key);
                        System.out.println(" 服务器清空编号为"+key+"智能门禁所有UID和有效期指令0x09");



                        BasePackage mBasePackage=new BasePackage();
                        ByteBuf flag=Unpooled.buffer(2);
                        flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                        mBasePackage.setFlag(flag);
                        mBasePackage.setChannel((byte) 0x01);
                        mBasePackage.setProtocolVersion((byte) 0x01);
                        byte[] bbody={  0x09
                                ,0x01,0x02,0x03,0x04,0x05,0x06
                                ,0x01,0x02,0x03,0x04};
                        ByteBuf body=Unpooled.buffer(bbody.length);
                        body.writeBytes(bbody);
                        mBasePackage.setBody(body);
                        CustomMsg customMsg=mBasePackage.getCustomMsg();
                        byte[] ee=new byte[2];
                        customMsg.getEnd().getBytes(0,ee);
                        System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                        obj.writeAndFlush(customMsg);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });
        // 给按钮添加1个事件
        sendB5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Map<String, SocketChannel> map = TempTestChannelManagerService.getChannels();
                    Iterator<String> it = map.keySet().iterator();
//                        无筛选向所有客户端发消息
                    while (it.hasNext()) {
                        String key = it.next();
                        SocketChannel obj = map.get(key);
                        System.out.println("服务器读取编号为"+key+"给定时间范围内开门日志 0x0C");


                        BasePackage mBasePackage=new BasePackage();
                        ByteBuf flag=Unpooled.buffer(2);
                        flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                        mBasePackage.setFlag(flag);
                        mBasePackage.setChannel((byte) 0x01);
                        mBasePackage.setProtocolVersion((byte) 0x01);
                        byte[] bbody={  0x0C,
                                0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
                                0x01, 0x02, 0x03, 0x04,
                                0x01, 0x02, 0x03, 0x04,
                                0x01, 0x02,
                                0x01, 0x02, 0x03, 0x04};
                        ByteBuf body=Unpooled.buffer(bbody.length);
                        body.writeBytes(bbody);
                        mBasePackage.setBody(body);
                        CustomMsg customMsg=mBasePackage.getCustomMsg();
                        byte[] ee=new byte[2];
                        customMsg.getEnd().getBytes(0,ee);
                        System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                        obj.writeAndFlush(customMsg);

                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });
        // 给按钮添加1个事件
        sendB6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Map<String, SocketChannel> map = TempTestChannelManagerService.getChannels();
                    Iterator<String> it = map.keySet().iterator();
//                        无筛选向所有客户端发消息
                    while (it.hasNext()) {
                        String key = it.next();
                        SocketChannel obj = map.get(key);
                        System.out.println(" 服务器向编号为"+key+"智能门禁发送远程升级指令 0x0D");
                        BasePackage mBasePackage=new BasePackage();
                        ByteBuf flag=Unpooled.buffer(2);
                        flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                        mBasePackage.setFlag(flag);
                        mBasePackage.setChannel((byte) 0x01);
                        mBasePackage.setProtocolVersion((byte) 0x01);
                        byte[] byte1 = {
                                0x0D,
                                0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
//                    域名
                        byte[] url = new byte[200];
//                    端口号
                        byte[] byte2 = {
                                0x01, 0x02,
                                0x01, 0x02,
                                0x01, 0x02, 0x03,0x04};
                        byte[] data = new byte[byte1.length + url.length + byte2.length];
                        System.arraycopy(byte1, 0, data, 0, byte1.length);
                        System.arraycopy(url, 0, data, byte1.length, url.length);
                        System.arraycopy(byte2, 0, data, byte1.length + url.length, byte2.length);
                        ByteBuf body = Unpooled.buffer(data.length);
                        body.writeBytes(data);
                        mBasePackage.setBody(body);
                        CustomMsg customMsg=mBasePackage.getCustomMsg();
                        byte[] ee=new byte[2];
                        customMsg.getEnd().getBytes(0,ee);
                        System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                        obj.writeAndFlush(customMsg);

                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });
        // 给按钮添加1个事件
        sendB7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Map<String, SocketChannel> map = TempTestChannelManagerService.getChannels();
                    Iterator<String> it = map.keySet().iterator();
//                        无筛选向所有客户端发消息
                    while (it.hasNext()) {
                        String key = it.next();
                        SocketChannel obj = map.get(key);
                        System.out.println("服务器向编号为"+key+"智能门禁发送远程重启指令 0x0E");

                        BasePackage mBasePackage=new BasePackage();
                        ByteBuf flag=Unpooled.buffer(2);
                        flag.writeBytes(new byte[]{(byte)0xEF,0x3A});
                        mBasePackage.setFlag(flag);
                        mBasePackage.setChannel((byte) 0x01);
                        mBasePackage.setProtocolVersion((byte) 0x01);
                        byte[] bbody={   0x0E,
                                0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
                                0x01, 0x02, 0x03, 0x04};
                        ByteBuf body=Unpooled.buffer(bbody.length);
                        body.writeBytes(bbody);
                        mBasePackage.setBody(body);
                        CustomMsg customMsg=mBasePackage.getCustomMsg();
                        byte[] ee=new byte[2];
                        customMsg.getEnd().getBytes(0,ee);
                        System.out.println("客户端发出的包，包尾是--->"+ CRCUtil.bytesToHexString(ee));
                        obj.writeAndFlush(customMsg);

                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });

        jl1.add(sendB);
//        jl1.add(sendB1);
//        jl1.add(sendB2);
        jl1.add(sendB3);
//        jl1.add(sendB4);
//        jl1.add(sendB5);
//        jl1.add(sendB6);
//        jl1.add(sendB7);
        jl1.add(jsp);
        con.add(jl1);




        ShuntServer mTimeServer= new ShuntServer();
//                启动客户端
        try {
            mTimeServer.bind(port);
        } catch (Exception e) {
            e.printStackTrace();
        }













    }
    public static void main(String[] args) {
        int port = 8981;
//        try {
////            Properties prop1 = new Properties();
////            ///保存属性到b.properties文件
////            FileOutputStream oFile = new FileOutputStream("b.properties", true);//true表示追加打开
////            prop1.setProperty("port", "8080");
////            prop1.store(oFile, "The New properties file");
////            oFile.close();
//
//            Properties prop2 = new Properties();
//            //读取属性文件a.properties
//            InputStream in = new BufferedInputStream(new FileInputStream("b.properties"));
//            prop2.load(in);     ///加载属性列表
//
//            if(null!=prop2.getProperty("port")){
//                port = Integer.valueOf(prop2.getProperty("port"));
//            }
//
//
//            in.close();
//
//        }catch (Exception e){
//
//        }
        // 实例化对象
        ShuntServerUI qq = new ShuntServerUI(port);
    }


}
