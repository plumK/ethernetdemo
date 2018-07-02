package com.module.ethernet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import org.greenrobot.eventbus.EventBus;

/**
 * @author Crystal lee
 * @package com.module.ethernet.receiver
 * @fileName NetChangeReceiver
 * @date on 2018/6/28 0028
 * @describe TODO
 */

public class NetChangeReceiver extends BroadcastReceiver {
    public static final String NET_CHANGE = "net_change";
    @Override
    public void onReceive(Context context, Intent intent) {
        // 如果相等的话就说明网络状态发生了变化
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            EventBus.getDefault().post(NET_CHANGE);
        }
    }
}
