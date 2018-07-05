package com.edan.www.bluetoothdemo;

/**
 * 创建者     Zhangyu
 * 创建时间   2018/5/22 15:44
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author
 * 更新时间   $Date
 * 更新描述   ${TODO}
 */

public class MsgEvent {

    private final String msg;
    private final Object mObject;

    public MsgEvent(String msg, Object mObject) {
        this.msg = msg;
        this.mObject = mObject;
    }

    public String getMsg() {
        return msg;
    }

    public Object getObject() {
        return mObject;
    }
}
