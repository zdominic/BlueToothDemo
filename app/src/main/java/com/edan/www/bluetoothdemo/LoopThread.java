package com.edan.www.bluetoothdemo;

/**
 * 创建者     Zhangyu
 * 创建时间   2018/5/22 13:56
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author
 * 更新时间   $Date
 * 更新描述   ${TODO}
 */

public abstract class LoopThread extends BaseThread {

    public boolean mBoolean;

    public LoopThread() {
        mBoolean = true;
    }

    @Override
    public void task() {
        try {
            while (mBoolean) {
                loopTask();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public abstract void loopTask() throws Exception;
    public abstract void doWithCancel();

    public void cancelLoop(){
        if (mBoolean){
            mBoolean = false;
            doWithCancel();
        }
    }
}
