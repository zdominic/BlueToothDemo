package com.edan.www.bluetoothdemo;

import android.bluetooth.BluetoothSocket;

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


    private static BlueToothDataHandler sDataHandler  = new BlueToothDataHandler();

    private BlueToothDataHandler() {
    }

    public static BlueToothDataHandler getDataHandler(){
        return sDataHandler;
    }

    public void openDataSource(Object object) {
        BluetoothSocket bluetoothSocket = (BluetoothSocket) object;

    }
}
