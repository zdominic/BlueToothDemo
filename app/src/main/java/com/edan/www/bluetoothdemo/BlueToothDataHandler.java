package com.edan.www.bluetoothdemo;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;

/**
 * 创建者     Zhangyu
 * 创建时间   2018/5/22 15:54
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author
 * 更新时间   $Date
 * 更新描述   ${TODO}
 */

public class BlueToothDataHandler {


    private static BlueToothDataHandler sDataHandler = new BlueToothDataHandler();
    private final BufQueue<FrameBean> m_Beans;
    private final BTDataParser m_BtDataParser;
    private float m_bleStateCount = 1;
    private boolean mIsStartProcess = true;
    private boolean m_processDataException = false;
    private boolean m_isSavingData = false;
    private boolean isFileCreated;
    private boolean m_isDataSourceOpen; //防止重复操作，是否打开了数据通道。
    private VerifyConnectionTask m_verifyTask;
    private ReadDataTask mReadDataTask;

    /**
     * 用于数据处理
     **/
    private BlueToothDataHandler() {
        // m_Beans = new LinkedBlockingQueue<FrameBean>();
        m_Beans = new BufQueue<FrameBean>(4);//以防数据积压太多，设置了4包数据
        m_BtDataParser = new BTDataParser(m_Beans);
    }

    public static BlueToothDataHandler getDataHandler() {
        return sDataHandler;
    }

    public void openDataSource(Object object) throws Exception {
        // 清空缓存数据
        m_Beans.Clear();
        m_BtDataParser.clean();
        if (!m_isDataSourceOpen) {
            m_isDataSourceOpen = true;
            m_processDataException = false;
            BluetoothSocket bluetoothSocket = (BluetoothSocket) object;
            if (mReadDataTask != null) {
                mReadDataTask.cancel();
                mReadDataTask = null;
            }
            mReadDataTask = new ReadDataTask(bluetoothSocket);
            mReadDataTask.start();

            //校验数据的接收
            if (m_verifyTask != null) {
                m_verifyTask.cancelLoop();
                m_verifyTask = null;
            }
            m_verifyTask = new VerifyConnectionTask(bluetoothSocket);
            m_verifyTask.start();
        }

    }

    /**
     * 关闭 蓝牙数据的接收
     */
    public void closeDataSource() {
        if (m_isDataSourceOpen) {
            m_isDataSourceOpen = false;
            if (mReadDataTask != null) {
                mReadDataTask.cancel();
                mReadDataTask = null;
            }
            if (m_verifyTask != null) {
                m_verifyTask.cancelLoop();
                m_verifyTask = null;
            }
            //清除数据，恢复初始状态
            m_Beans.Clear();
            m_BtDataParser.clean();
        }
    }


    private class ReadDataTask extends Thread {
        private BluetoothSocket mBluetoothSocket;
        private InputStream mInputStream;
        private boolean isExitReadDataTask = false;
        private int len = 0;
        private int readMaxLen = 4 * 129; // 四包数据
        private byte[] bData = new byte[readMaxLen];
        private int readLen = bData.length;

        public ReadDataTask(BluetoothSocket bluetoothSocket) {

            mBluetoothSocket = bluetoothSocket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (!Thread.interrupted() && !isExitReadDataTask && mInputStream != null) {
                try {
                    if (len >= readLen) {
                        len = 0;
                    }
                    while (len < readLen) {
                        // 当蓝牙断开的时候该代码会进入一定时间的阻塞状态，断开呢？
                        len += mInputStream.read(bData, len, readLen - len);

                    }
                    //每次在指定时间内成功获取数据都给其重新赋值
                    m_bleStateCount = 1.0f;
                    if (mIsStartProcess) {
                        if (len > 0) {
                            m_processDataException = false;
                            m_BtDataParser.process(bData, bData.length);
                        } else {
                            m_processDataException = true;
                        }
                    }
                } catch (Exception e) {
                    Log.e("aaa", " Exception  Exception " + e.getMessage());
                    cancel();
                }
            }
        }

        /**
         * 结束任务，释放资源
         */

        public void cancel() {
            isExitReadDataTask = true;
            try {
                if (mBluetoothSocket != null) {
                    mBluetoothSocket.close();
                    Log.e("aaa", "cancel ");
                }
                bData = null;
                len = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 从 保存的 帧数据的解析成的类的储存器中 拿到数据
     *
     * @return FrameBean
     * @throws Exception
     */
    public FrameBean readFrameBean() throws Exception {
        FrameBean bean = null;
        try {
            bean = m_Beans.Pop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (m_processDataException) {
            throw new Exception("获取数据异常");
        }
        return bean;
    }

    /**
     * 解析数据的任务，与蓝牙数据接收的任务处于并行状态，减少数据阻塞的可能性
     * <p/>
     * 或者做成监视蓝牙数据是否出现断开的任务
     */
    private class VerifyConnectionTask extends LoopThread {

        private BluetoothSocket m_bluetoothSocket;

        public VerifyConnectionTask(BluetoothSocket bluetoothSocket) {
            m_bluetoothSocket = bluetoothSocket;
        }

        @Override
        public void loopTask() throws Exception {
            //如果一秒钟之后还没有对其重新赋值，则表示数据断开
            if (m_bleStateCount > 0) {
                m_bleStateCount = m_bleStateCount - 0.2f;
            } else {
                EventBus.getDefault().post(new MsgEvent("CONNECT_HALF_INTERRUPT", m_bluetoothSocket));
                cancelLoop();
                m_bleStateCount = 1;
            }
            sleep(200);
        }

        @Override
        public void doWithCancel() {

        }
    }
}


