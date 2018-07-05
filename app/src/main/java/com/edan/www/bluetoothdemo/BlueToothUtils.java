package com.edan.www.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

/**
 * 创建者     Zhangyu
 * 创建时间   2018/5/22 11:03
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author
 * 更新时间   $Date
 * 更新描述   ${TODO}
 */

public class BlueToothUtils {

    /**
     * 对设备进行配对
     *
     * @param btClass
     * @param bluetoothDevice
     * @return
     */
    public static boolean createBond(Class btClass, BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {         //蓝牙已经连接了
            return true;
        }
        boolean isCreateSuccess = false;
        try {
            Method method = btClass.getMethod("createBond");
            isCreateSuccess = (Boolean) method.invoke(bluetoothDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isCreateSuccess;

    }

    /**
     * 取消搜索任务
     *
     * @param adapter
     */
    public static void cancelDiscovery(BluetoothAdapter adapter) {
        if (adapter != null) {
            adapter.cancelDiscovery();
        }
    }

    /**
     * 开始搜索
     * @param adapter
     */
    public static void startDiscovery(BluetoothAdapter adapter) {
        if (adapter!=null){
            adapter.startDiscovery();
        }
    }



    /**
     * 检查已配对的设备
     *
     * @param adapter
     * @return
     */
    public static BluetoothDevice checkBondDevices(BluetoothAdapter adapter) {
        BluetoothDevice device = null;
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        for (BluetoothDevice bluetoothDevice : bondedDevices) {
            if (bluetoothDevice != null && bluetoothDevice.getName().equalsIgnoreCase(TRAGET_DEVICE)) {
                device = bluetoothDevice;
                break;
            }
        }
        return device;
    }

    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static final String TRAGET_DEVICE = "EDAN-SD";


    /**
     * 连接设置
     * @param device
     * @return
     * @throws IOException
     */
    public static BluetoothSocket getConnectBlueToothSocket(BluetoothDevice device) throws IOException {
        UUID uuid = UUID.fromString(SPP_UUID);
        BluetoothSocket socket;
        if (App.getSSKLevel() >= 10){
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        }else {
            socket = device.createRfcommSocketToServiceRecord(uuid);
        }
        return socket;
    }

    public static boolean removeBond(BluetoothDevice device) {
        boolean isRemoveSuccess = false;
        try {
            Method method = BluetoothDevice.class.getMethod("removeBond");
            boolean flag = (Boolean)method.invoke(device);
            isRemoveSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isRemoveSuccess;
    }
}
