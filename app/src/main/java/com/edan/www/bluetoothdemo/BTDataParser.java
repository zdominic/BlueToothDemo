package com.edan.www.bluetoothdemo;


import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class BTDataParser {

    private static final String TAG            = "BTDataParser";
    private              int    isFindedHead   = 0;// 0表示没有找到帧头，1表示找到帧头
    private              int    m_FrameLen     = 127; // 帧的长度
    private              int    m_LastPosition = 0;// 最近一次访问数据的位置
    // private int mHeadPosition = 0;// 最近一次访问头数据的位置
    private              byte[] m_FrameData    = null;
    // TODO 如果使用 定义的 BufQueue 会不会造成很多数据的丢失
//    private LinkedBlockingQueue<FrameBean> beans;
    private BufQueue<FrameBean> beans;
    private FrameHandler        mFrameHandler;
    private List<FrameBean> m_frameBeanList;
    private boolean             firstPackage;


    /**
     */
    public BTDataParser() {
        m_FrameData = new byte[m_FrameLen];
    }

    /**
     */
    public BTDataParser(BufQueue<FrameBean> beans) {
        m_FrameData = new byte[m_FrameLen];
        m_frameBeanList = new ArrayList<>();
        this.beans = beans;
        firstPackage =true;
        this.mFrameHandler = new FrameHandler();
    }

    public void clean() {
        beans.Clear();
        reset();
        firstPackage =true;
    }

    public List<FrameBean> getFrameData() {
        return m_frameBeanList;
    }

    public void clearFrameData() {
        m_frameBeanList.clear();
    }

    public long getBeansLen() {
        if (beans != null) {
            return beans.Size();
        }
        return 0;
    }

    /**
     * 方法名称: process
     * 功能描述: 解析数据.
     * 修改记录:
     * 修改者:   ouyangxingyu
     * 修改日期: 2014-12-3
     * 修改内容: 创建.
     *
     * @param bs
     * @param len 真实数据的长度
     *
     * @throws
     */
    public void process(byte[] bs, int len) {
        byte[] currentBytes = bs;
        if (len > bs.length || len <= 0) {// 表示数据有问题 或者 没有数据
            return;
        }
        // 真实读取的数据长度
        for (int i = 0; i < len; i++) {
            byte value = currentBytes[i];
            switch (isFindedHead) {
                case 0:// 表示没有找到帧头
                    if (value == (byte) 0xfa) {
                        isFindedHead = 1;
                    }
                    break;
                case 1:// 表示找到帧头
                    if (m_LastPosition <= m_FrameLen - 1) {
                        m_FrameData[m_LastPosition++] = value;
                    } else {
                        m_LastPosition++;
                    }
                    // 表示读取一帧数据结束
                    if (m_LastPosition == m_FrameLen + 1) {
                        if (value == (byte) 0xfb) {
                            try {
                                FrameBean bean = mFrameHandler.getFrameBean(m_FrameData);
//                                m_frameBeanList.add(bean);
                                if (bean!=null) {
                                    Log.e("aaa", "process 胎心值   " + bean.getFhr());
                                }
                                if(firstPackage && bean!=null){
                                    EventBus.getDefault().post(new MsgEvent("CONNECT_SUCCESS_SHOW", null));
                                    firstPackage =false;
                                }
                                beans.Push(bean);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // 复位
                        reset();
                    }
                    break;
                default:
                    break;
            }
        }
        currentBytes = null;
    }

    /**
     * 方法名称: reset
     * 功能描述: 复位,重新开始计算.
     * 修改记录:
     * 修改者:   ouyangxingyu
     * 修改日期: 2014-12-3
     * 修改内容: 创建.
     *
     * @throws
     */
    public void reset() {
        m_LastPosition = 0;
        isFindedHead = 0;
    }

}
