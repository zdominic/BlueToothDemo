package com.edan.www.bluetoothdemo;


public class FrameHandler {

    private int bt_data_length = 125;//音频数据长度

    public FrameBean getFrameBean(byte[] src) {
        FrameBean bean = null;


        bean = new IFrameHandler() {
            @Override
            public FrameBean handlerData(byte[] src) {
                FrameBean bean       = new FrameBean();
                int       dataLength = bt_data_length;
                //控制命令
                byte controlComm = src[dataLength + 1];
                int  reserve     = ((controlComm) & 0xC0) >> 6; //取第 7、8 个字节
                int  beat        = ((controlComm) & 0x20) >> 5; //取第 6 个字节
                int  semaphoreQ  = ((controlComm) & 0x18) >> 3; //取第四五个字节
                int  electric    = (controlComm) & 0x07;    //取前三个字节
                //音频数据
                byte[] data = new byte[dataLength];
                System.arraycopy(src, 0, data, 0, dataLength);
                //胎心值  这里和协议给的位置有点偏差 ，注意一下
                int fhr = src[dataLength] & 0xff;
                //备用字节
                bean.setFrameBean(data, electric, semaphoreQ, fhr, reserve,beat);
                return bean;
            }
        }.handlerData(src);
        return bean;
    }


}
