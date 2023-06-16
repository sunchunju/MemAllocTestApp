package com.test.memalloctestapp.ui.main;

import java.nio.ByteBuffer;

public class NativeMemoryUtil {
    static {
        System.loadLibrary("native-lib");
    }

    public static native ByteBuffer allocateNativeMemory(long size);

    public static native void createFileIfNotExist(String filename);

    public static native void mmapFileMemoryTest(String strPath_externalSD);
}