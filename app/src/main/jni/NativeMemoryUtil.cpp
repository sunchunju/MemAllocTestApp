#include <jni.h>
#include <stdlib.h>
#include <time.h>

//extern "C" JNIEXPORT jobject JNICALL
//Java_com_test_memalloctestapp_NativeMemoryUtil_allocateNativeMemory(JNIEnv *env, jclass clazz, jlong size) {
//    // 分配内存
//    char* buffer = static_cast<char*>(malloc(size));
//
//    // 填充随机数
//    srand(time(nullptr));
//    for (int i = 0; i < size; ++i) {
//        buffer[i] = static_cast<char>(rand() % 256);
//    }
//
//    // 将分配的内存包装为 Java 的 ByteBuffer 类型对象
//    return env->NewDirectByteBuffer(buffer, size);
//}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_test_memalloctestapp_ui_main_NativeMemoryUtil_allocateNativeMemory(JNIEnv *env, jclass clazz, jlong size) {
    // 分配内存
    char* buffer = static_cast<char*>(malloc(size));

    // 填充随机数
    srand(time(nullptr));
    for (int i = 0; i < size; ++i) {
        buffer[i] = static_cast<char>(rand() % 256);
    }

    // 将分配的内存包装为 Java 的 ByteBuffer 类型对象
    return env->NewDirectByteBuffer(buffer, size);
}