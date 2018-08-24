package com.edan.www.bluetoothdemo;

import java.util.Arrays;

/**
 * 类名称     : BtFrameBean
 * 功能描述: 蓝牙数据帧.
 * 修改记录:
 * 修改者:   ouyangxingyu
 * 修改日期: 2015-4-10
 * 修改内容: 创建.
 * 修改者:   majiahao
 * 修改日期: 2016-12-8
 * 修改内容: 协议更改
 * <p/>
 * 版本信息: V1.0
 */
public class FrameBean {

    private int dataLength = 125;

    private byte[] data;//音频数据
    private int    fhr;//胎心值
    private int    reserve;//备用字节
    private int    electric;//电量
    private int    semaphoreQ;//信号质量
    private int    beat;//心跳节拍


    public FrameBean() {

    }


    public void setFrameBean(byte[] data, int electric,
                             int semaphoreQ, int fhr, int reserve, int beat) {
        this.data = data;
        this.electric = electric;
        this.semaphoreQ = semaphoreQ;
        this.fhr = fhr;
        this.reserve = reserve;
        this.beat =beat;
    }

    public int getDataLength() {
        return dataLength;
    }

    public int getBeat() {
        return beat;
    }

    public void setBeat(int p_beat) {
        beat = p_beat;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getElectric() {
        return electric;
    }

    public void setElectric(int off) {
        this.electric = off;
    }

    public int getSemaphoreQ() {
        return semaphoreQ;
    }

    public void setSemaphoreQ(int semaphoreQ) {
        this.semaphoreQ = semaphoreQ;
    }


    public int getFhr() {
        return fhr;
    }

    public void setFhr(int fhr) {
        this.fhr = fhr;
    }

    public int getReserve() {
        return reserve;
    }

    public void setReserve(int reserve) {
        this.reserve = reserve;
    }


    @Override
    public String toString() {
        return "FrameBean{" +
                "dataLength=" + dataLength +
                ", data=" + Arrays.toString(data) +
                ", fhr=" + fhr +
                ", reserve=" + reserve +
                ", electric=" + electric +
                ", semaphoreQ=" + semaphoreQ +
                ", beat=" + beat +
                '}';
    }
}
