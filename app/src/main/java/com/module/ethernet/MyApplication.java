package com.module.ethernet;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

/**
 * @author Crystal lee
 * @package com.module.ethernet
 * @fileName MyApplication
 * @date on 2018/6/28 0028
 * @describe TODO
 */

public class MyApplication extends MultiDexApplication {
    private static Context mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }


    public static Context getAppContext() {
        return mApp;
    }
}
