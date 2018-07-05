package com.edan.www.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_ENABLE = 2018;
    private Button mOpenBtn;
    private static boolean isBleServiceStart = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        initView();
    }

    private void initView() {
        mOpenBtn = (Button) findViewById(R.id.open_button);
        openBlueTooth();
        startBleConnectService(this);
    }

    /**
     * 开启蓝牙开关
     */
    private void openBlueTooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter!=null){
            if (!bluetoothAdapter.isEnabled()){
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivityForResult(intent,RESULT_ENABLE);
            }
        }
    }

    /**
     * 开启蓝牙连接和状态监听的服务
     * @param context  context
     */
    public static synchronized void startBleConnectService(Context context){
        if (!isBleServiceStart){
            synchronized (BleConnectService.class){
                Intent intent = new Intent(context, BleConnectService.class);
                context.startService(intent);
                isBleServiceStart = true;
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public  void onMsgEvent(MsgEvent event){
        if (event!=null){
            if (event.getMsg().equalsIgnoreCase("success")){
                gotoReceiveMsg(event);
            }
        }
    }

    private void gotoReceiveMsg(MsgEvent event) {
        BlueToothDataHandler.getDataHandler().openDataSource(event.getObject());
    }


}
