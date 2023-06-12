package com.test.memalloctestapp.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

public class Utils {
    private static Utils instance = null;
    public Utils() {
    }

    public static Utils getInstance(){
        if (instance == null){
            instance = new Utils();
        }
        return instance;
    }

    public static long getFreeMem(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Activity.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        // 单位Bytes
        return info.availMem;
    }
    public static long getTotalMem(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Activity.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        // 单位Bytes
        return info.totalMem;
    }
}
