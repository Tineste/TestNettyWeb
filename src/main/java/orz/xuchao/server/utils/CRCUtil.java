package orz.xuchao.server.utils;


import java.util.Calendar;
import java.util.Date;

public class CRCUtil {

    public static int A_VALUE=0xFFFF;
    public static int DIV_VALUE=0x4821;

    public static byte[] getTimestampBytes(long timestamp) {
        byte[] b = new byte[4];
        b[0] = (byte) ((timestamp >> 24) & 0xFF);
        b[1] = (byte) ((timestamp >> 16) & 0xFF);
        b[2] = (byte) ((timestamp >> 8) & 0xFF);
        b[3] = (byte) (timestamp & 0xFF);
        return b;
    }

    public static byte[] short2Bytes(short s) {
        byte[] b = new byte[2];
        b[0] = (byte) ((s >> 8) & 0xFF);
        b[1] = (byte) (s & 0xFF);
        return b;
    }

    public static byte[] long2Bytes(long l) {
        byte[] b = new byte[8];
        b[0] = (byte) ((l >> 56) & 0xFF);
        b[1] = (byte) ((l >> 48) & 0xFF);
        b[2] = (byte) ((l >> 40) & 0xFF);
        b[3] = (byte) ((l >> 32) & 0xFF);
        b[4] = (byte) ((l >> 24) & 0xFF);
        b[5] = (byte) ((l >> 16) & 0xFF);
        b[6] = (byte) ((l >> 8) & 0xFF);
        b[7] = (byte) (l & 0xFF);
        return b;
    }

    public static byte[] int2Bytes(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) ((i >> 24) & 0xFF);
        b[1] = (byte) ((i >> 16) & 0xFF);
        b[2] = (byte) ((i >> 8) & 0xFF);
        b[3] = (byte) (i & 0xFF);
        return b;
    }

    public static byte[] crc16Check(byte[] data) {
        int high;
        int flag;

        // 16位寄存器，所有数位均为1
        int wcrc = 0xffff;
        for (int i = 0; i < data.length; i++) {
            // 16 位寄存器的高位字节
            high = wcrc >> 8;
            // 取被校验串的一个字节与 16 位寄存器的高位字节进行“异或”运算
            wcrc = high ^ data[i];

            for (int j = 0; j < 8; j++) {
                flag = wcrc & 0x0001;
                // 把这个 16 寄存器向右移一位
                wcrc = wcrc >> 1;
                // 若向右(标记位)移出的数位是 1,则生成多项式 1010 0000 0000 0001 和这个寄存器进行“异或”运算
                if (flag == 1)
                    wcrc ^= 0x4821;
            }
        }
        return CRCUtil.short2Bytes((short) wcrc);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static int bytesToInt(byte[] src) {
        int value;
        value = (int) (((src[0] & 0xFF) << 24)
                | ((src[1] & 0xFF) << 16)
                | ((src[2] & 0xFF) << 8)
                | (src[3] & 0xFF));
        return value;
    }

    public static short bytesToShort(byte[] src) {
        short value;
        value = (short) (((src[0] & 0xFF) << 8)
                | (src[1] & 0xFF));
        return value;
    }



    public static int MyCrc16Check(byte[] data){
        int i, j;
        int Crc_Value = A_VALUE;
        int My_Check;
        for (i = 0; i < data.length; i++) {
            //System.out.println(String.format("%x", data[i]));
            // System.out.println(Integer.toBinaryString(data[i] & 0xFF));
            Crc_Value = Crc_Value ^ (data[i] & 0xFF);
            for (j = 0; j < 8; j++) {
                My_Check = Crc_Value & 0x0001;
                Crc_Value = Crc_Value >>> 1;
                if (My_Check == 1) {
                    Crc_Value = Crc_Value ^ 0x4821;
                }
            }
        }
        return Crc_Value;
    }


    public static  byte[] timeToBytes(Calendar calendar) {

        return CRCUtil.getTimestampBytes(calendar.getTimeInMillis() / 1000);
    }
    public static Date bytesToTime(byte[] time){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(((long) CRCUtil.bytesToInt(time))*1000);
        return calendar.getTime();
    }

}
