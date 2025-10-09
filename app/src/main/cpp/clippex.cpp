#include <jni.h>


extern "C"
JNIEXPORT jint JNICALL
Java_com_example_clippex_MainActivity_00024Companion_add(JNIEnv *env, jobject thiz, jint a,
                                                         jint b) {
    return a+b;
}