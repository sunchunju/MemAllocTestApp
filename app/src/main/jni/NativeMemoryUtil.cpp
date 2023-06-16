#include <jni.h>
#include <stdarg.h>
#include <stdlib.h>
#include <time.h>
#include <errno.h>
#include <fcntl.h>
#include <android/log.h>

#include <sys/stat.h>
#include <sys/mman.h>
#include <sys/shm.h>
#include <string.h>

#define PRE_CREATED_FILE_BLOCKS 256
#define PRE_CREATED_FILE_SIZE   (PRE_CREATED_FILE_BLOCKS * 1024) /* kb unit */

static void LOG(const char *fmt, ...)
{
    va_list vl;
    va_start(vl, fmt);
    __android_log_vprint(ANDROID_LOG_INFO, "MemoryAllocTest", fmt, vl);
    va_end(vl);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_test_memalloctestapp_ui_main_NativeMemoryUtil_mmapFileMemoryTest(JNIEnv *env, jclass clazz, jstring filename)
{
    caddr_t m;
    jbyteArray jb;
    jboolean isCopy;
    struct stat finfo;

    const char *filePath = (*env).GetStringUTFChars(filename, &isCopy);
    int fd = open(filePath, O_RDONLY);

    if (fd < 0) {
        LOG("Could not open file at path:%s", filePath);
        return;
    }

    lstat(filePath, &finfo);
    m = static_cast<caddr_t>(mmap((caddr_t) 0, finfo.st_size,
                                  PROT_READ, MAP_PRIVATE, fd, 0));

    if ((caddr_t)-1 == m) {
        LOG("mmap file failed with errno:%d", errno);
        return;
    }
    /* now we touch 1K area on every 1MB segment */
    uint8_t *buffer = static_cast<uint8_t *>(malloc(1024));
    int i;
    for (i = 0; i < PRE_CREATED_FILE_BLOCKS; i++) {
        memcpy(buffer, m, 1024);
        m += 1024 * 1024;
    }
    LOG("touch file mmapped address %d times, every area range is 1K", i);
    free(buffer);
    close(fd);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_test_memalloctestapp_ui_main_NativeMemoryUtil_createFileIfNotExist(JNIEnv *env, jclass clazz, jstring filename)
{
    jboolean isCopy;
    const char *filePath = (*env).GetStringUTFChars(filename, &isCopy);
    struct stat st = {0};

    if (stat(filePath, &st) == 0 && (st.st_size / 1024) == PRE_CREATED_FILE_SIZE) {
        // looks the file is already exist
        LOG("file:%s is already exist, size:%d\n", filePath, st.st_size);

    }
    else {
        if (st.st_size != 0)
        {
            LOG("exist file size %d does not match to purpose: %d", st.st_size, PRE_CREATED_FILE_SIZE);
            /* remove current file */
            unlink(filePath);
            LOG("%s is removed", filePath);
        }
        FILE *fp = fopen(filePath, "wb+");
        if (fp == nullptr) {
            LOG("can not create a file at path:%s", filePath);
            return;
        }

        srand(time(nullptr));
        uint8_t *buffer = static_cast<uint8_t *>(malloc(1024));
        if (buffer == nullptr) {
            LOG("out of memory to allocate 1024 Bytes");
            return;
        }

        for (int i = 0; i < PRE_CREATED_FILE_SIZE; i++) {
            /* fill up all the 1024B RAM */
            for (int j = 0; j < 1024; j++) {
                buffer[j] = static_cast<char>(rand() % 256);
            }

            size_t size = fwrite(buffer, 1, 1024, fp);
            if (size != 1024) {
                LOG("disk full? at No. %d cycles, written size:%d", i, size);
                break;
            }
        }

        fclose(fp);
        free(buffer);
        LOG("file:%s is created successfully with size:%d KB", filePath, PRE_CREATED_FILE_SIZE);
    }

}

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