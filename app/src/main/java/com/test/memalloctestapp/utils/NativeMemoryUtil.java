package com.test.memalloctestapp.utils;

import java.nio.ByteBuffer;

public class NativeMemoryUtil {
    static {
        System.loadLibrary("native-lib");
    }

    public static native ByteBuffer allocateNativeMemory(long size);
}