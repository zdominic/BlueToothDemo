package com.edan.www.bluetoothdemo;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;

/*
 * 版权所有: 深圳理邦精密仪器股份有限公司
 * 项目名称: SD1
 * 项目编号: 
 * 文件名称: BluetoothSendHandler
 * 功能说明: TODO 
 * 其它说明: 无
 * 
 * 修改记录:
 *     修改日期: 2017/4/20
 *     修 改 者: majiahao
 *     修改内容: 创建
 */
public class BluetoothSendHandler {

    private static BluetoothSendHandler mBleSendHandler = new BluetoothSendHandler();
    private boolean        mIsSendTaskStart;
    private DataSendThread mSendThread;
    private boolean        mIsForceByOthers;  // 其他影响条件有影响到发送数据的时候，只需要改变这个值就好了

    /**
     * 用于数据回传的任务
     **/
    private BluetoothSendHandler() {
    }

    public static BluetoothSendHandler getBleSendHandler() {
        return mBleSendHandler;
    }


    /**
     * 打开数据通道，进行数据的回传
     * <p/>
     *
     * @param object 蓝牙的socket 数据接口
     */
    public void openDataSource(Object object) throws Exception {
        if (!mIsSendTaskStart) {
            BluetoothSocket bluetoothSocket = (BluetoothSocket) object;
            if (bluetoothSocket == null) {
                throw new Exception("bluetoothSocket is empty when send data back");
            }
            if (mSendThread != null) {
                mSendThread.cancelLoop();
                mSendThread = null;
            }
            mSendThread = new DataSendThread(bluetoothSocket);
            mSendThread.start();
            mIsSendTaskStart = true;
        }
    }

    /**
     * 关闭数据发送的任务
     */
    public void closeDataSource() {
        if (mIsSendTaskStart) {
            if (mSendThread != null) {
                mSendThread.cancelLoop();
                mSendThread = null;
            }
            mIsSendTaskStart = false;
        }
    }

    /**
     * 发送关闭设备声音的指令
     *
     * @param playByPhone
     */
    public void playByPhone(boolean playByPhone) {
        mIsForceByOthers = playByPhone;
    }

    /**
     * 数据发送的任务
     */
    class DataSendThread extends LoopThread {

        byte[] mData = new byte[3];
        OutputStream mOutputStream;

        public DataSendThread(BluetoothSocket socket) {
            try {
                if (socket != null) {
                    mOutputStream = socket.getOutputStream();
                }
                mData[0] = (byte) 0xfa;
                mData[2] = (byte) 0xfb;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void loopTask() throws Exception {
            try {
                sleep(500);
                if (mIsForceByOthers) { //&& mIsForceByOthers
                    mData[1] = (byte) 0x00;
                } else {
                    mData[1] = (byte) 0x01;
                }
                mOutputStream.write(mData, 0, mData.length);
                sleep(500);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        @Override
        public void doWithCancel() {
            try {
                if (mOutputStream != null) {
                    mOutputStream.close();
                    mOutputStream = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
