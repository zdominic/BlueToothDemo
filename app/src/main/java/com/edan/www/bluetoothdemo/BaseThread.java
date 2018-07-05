package com.edan.www.bluetoothdemo;

/**
 * 创建者     Zhangyu
 * 创建时间   2018/5/22 13:55
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author
 * 更新时间   $Date
 * 更新描述   ${TODO}
 */

public abstract class BaseThread extends Thread {

    @Override
    public void run() {
        super.run();
        task();
    }

    public abstract void task();
}
