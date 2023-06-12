LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := native-lib
LOCAL_SRC_FILES := NativeMemoryUtil.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)