package com.edan.www.bluetoothdemo;

import android.app.Application;
import android.os.Build;

/**
 * 创建者     Zhangyu
 * 创建时间   2018/5/22 13:22
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author
 * 更新时间   $Date
 * 更新描述   ${TODO}
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

    }

    public static  int getSSKLevel(){
        return Integer.valueOf(Build.VERSION.SDK);

    }
}
