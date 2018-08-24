package com.edan.www.bluetoothdemo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 创建者     Zhangyu
 * 创建时间   2018/5/22 10:34
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author
 * 更新时间   $Date
 * 更新描述   ${TODO}
 */

public class BleConnectService extends Service implements Runnable {

    //蓝牙是否连接
    public boolean mIsBleConnected;
    private Thread mThread;
    private BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ExecutorService mExecutorService;
    private MonitorThread mMonitorThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);                   //搜索蓝牙设备的广播监听
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);    //蓝牙设备配对的监听
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);        //连接中断的监听
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);   //蓝牙搜索任务
        registerReceiver(mBlueToothReceiver, intentFilter);

        mExecutorService = Executors.newFixedThreadPool(1);
        mMonitorThread = new MonitorThread();
        mMonitorThread.start();
        mExecutorService.execute(this);
        return START_STICKY;
    }

    @Override
    public void run() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter != null) {
            if (mAdapter.isEnabled()) {
                BluetoothDevice bluetoothDevice = BlueToothUtils.checkBondDevices(mAdapter);
                if (bluetoothDevice != null) {
                    connectDevice(bluetoothDevice);
                } else {
                    //需要重新查找设备进行配对,开启蓝牙设备可被发现
                    startDiscovery();
                }
            } else {
                //蓝牙关闭
            }
        }
    }

    //TODO 当接收到这个消息的时候如何处理，线程模式是否正确
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgEvent(MsgEvent event) {
        if (event != null) {
            switch (event.getMsg()) {
                case "CONNECT_HALF_INTERRUPT":
                    try {
                        BluetoothSocket socket = (BluetoothSocket) event.getObject();
                        if (socket != null) {
                            socket.close();
                        }
                        mIsBleConnected = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("aaa", " 开启搜素");
                    startDiscovery();
                    break;
            }
        }
    }


    /**
     * 蓝牙连接的广播监听
     */
    private BroadcastReceiver mBlueToothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device;
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:           //搜索蓝牙设备的广播监听
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.e("aaa", "device = " + device.getName());
                    //找到设备 并且蓝牙未连接
                    if (!mIsBleConnected && BlueToothUtils.checkFoundDevice(device)) {
                        BlueToothUtils.createBond(BluetoothDevice.class, device);
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:     //配对情况的更改
                    //设备配对
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_NONE:
                            //配对失败
                            Log.e("aaa", "配对失败 ");
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            Log.e("aaa", "配对中 ");
                            //配对中
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            //配对成功
                            Log.e("aaa", "配对成功 ");
                            connectDevice(device);
                            break;
                    }
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {   //蓝牙关闭了
                        mIsBleConnected = false;
                        //关掉控制线程
                        if (mMonitorThread != null) {
                            mMonitorThread.cancelLoop();
                            mMonitorThread = null;
                        }
                        cancelDiscovery();    //关闭搜索
                        Log.e("aaa", "蓝牙关闭了 ");
                    } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {   //蓝牙打开了
                        //用户手动开启蓝牙,则执行搜索任务
                        Log.e("aaa", "蓝牙打开了 ");
                        mAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (!mIsBleConnected) {
                            BluetoothDevice bluetoothDevice = BlueToothUtils.checkBondDevices(mAdapter);
                            if (bluetoothDevice != null) {
                                connectDevice(bluetoothDevice);   //去连接
                            } else {
                                startDiscovery();           //去搜索
                            }
                        }
                        if (mMonitorThread != null) {
                            mMonitorThread.cancelLoop();
                            mMonitorThread = null;
                        }
                        mMonitorThread = new MonitorThread();
                        mMonitorThread.start();

                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:        //搜索失败
                    cancelDiscovery();
                    Log.e("aaa", "ACTION_DISCOVERY_FINISHED ");
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 连接蓝牙设置的任务
     * <p>
     * 多次执行蓝牙连接任务的时候要保证连接蓝牙的线程不会有多个存活
     *
     * @param device
     */
    private void connectDevice(BluetoothDevice device) {
        if (mThread != null && mThread.isAlive()) {
            return;
        }
        if (mConnectThread != null) {
            mConnectThread = null;
        }
        if (!mIsBleConnected) {     //没有连接
            synchronized (BleConnectService.class) {
                mConnectThread = new ConnectThread(device);
                mThread = new Thread(mConnectThread);
                mThread.start();
            }
        }

    }


    /**
     * 开始连接的线程
     */
    class ConnectThread implements Runnable {

        BluetoothDevice mDevice;
        BluetoothSocket mSocket;


        public ConnectThread(BluetoothDevice device) {
            mDevice = device;
        }

        @Override
        public void run() {
            cancelDiscovery();
            try {
                mSocket = BlueToothUtils.getConnectBlueToothSocket(mDevice);
                if (mSocket != null) {
                    mSocket.connect();
                    mIsBleConnected = true;
                Log.e("aaa", "000  mSocket  的范德萨发 ");
                } else {
                    mIsBleConnected = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    mIsBleConnected = false;
                }
            } finally {
                Log.e("aaa", "mIsBleConnected   " + mIsBleConnected);
                if (!mIsBleConnected) {   //没有连接
                    startDiscovery();
                } else {
                    EventBus.getDefault().post(new MsgEvent("CONNECT_SUCCESS", mSocket));
                }
            }
        }
    }

    class MonitorThread extends LoopThread {

        int i = 0;
        int sleep = 15000;
        BluetoothDevice mDevice = null;

        @Override
        public void loopTask() throws Exception {
            sleep(sleep);
            if (!mIsBleConnected) {      //休眠30秒后蓝牙仍然未连接上
                //移除所有配对过的设备
                Set<BluetoothDevice> bondedDevices =
                        mAdapter.getBondedDevices();
                for (BluetoothDevice device : bondedDevices) {
                    if (device.getName().equalsIgnoreCase(BlueToothUtils.TRAGET_DEVICE)) {
                        mDevice = device;
                    }
                }
                if (mDevice != null) {
                    ++i;
                    if ((i % 2) != 0) {
                        connectDevice(mDevice);
                    } else {
                        BlueToothUtils.removeBond(mDevice);
                        i = 0;
                    }
                    mDevice = null;
                } else {
                    startDiscovery();
                }
            } else {
                i = 0;
            }
        }

        @Override
        public void doWithCancel() {

        }
    }

    /**
     * 开始查找设置
     */
    private void startDiscovery() {
        if (mAdapter != null && !mIsBleConnected) {
            //先关闭掉搜索
            BlueToothUtils.cancelDiscovery(mAdapter);
            SystemClock.sleep(10);
            //开启搜素
            BlueToothUtils.startDiscovery(mAdapter);
        }
    }

    /**
     * 取消搜索任务
     */
    private synchronized void cancelDiscovery() {
        if (mAdapter != null && mIsBleConnected) {     //已经连接了
            BlueToothUtils.cancelDiscovery(mAdapter);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mThread != null) {
            mThread = null;
        }
        if (mBlueToothReceiver != null) {
            unregisterReceiver(mBlueToothReceiver);
            mBlueToothReceiver = null;
        }
        if (mMonitorThread != null) {
            mMonitorThread.cancelLoop();
            mMonitorThread = null;
        }
        stopSelf();
    }
}
