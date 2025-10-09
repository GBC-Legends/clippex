use std::thread;
use std::time::Duration;
use jni::objects::JClass;
use jni::sys::jint;
use jni::JNIEnv;

#[link(name = "log")]
unsafe extern "C" {
    fn __android_log_print(prio: i32, tag: *const u8, fmt: *const u8, ...) -> i32;
}

const ANDROID_LOG_INFO: i32 = 4;

fn log(tag: &str, message: &str) {
    use std::ffi::CString;
    unsafe {
        let tag = CString::new(tag).unwrap();
        let msg = CString::new(message).unwrap();
        __android_log_print(ANDROID_LOG_INFO, tag.as_ptr() as *mut u8, msg.as_ptr() as *mut u8);
    }
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_clippex_MainActivity_add(
    _env: JNIEnv,
    _class: JClass,
    a: jint,
    b: jint,
) -> jint {
    log("RustJNI", &format!("Hello from Rust! {} + {} = {}", a, b, a + b));
    a + b
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_example_clippex_MainActivity_runThreads(
    _env: JNIEnv,
    _class: JClass,
) {
    log("RustJNI", "Starting two threads...");

    thread::spawn(|| {
        log("RustJNI", "Thread 1 started (sleeping 3s)");
        thread::sleep(Duration::from_secs(3));
        log("RustJNI", "Thread 1 finished");
    });

    thread::spawn(|| {
        log("RustJNI", "Thread 2 started (sleeping 5s)");
        thread::sleep(Duration::from_secs(5));
        log("RustJNI", "Thread 2 finished");
    });

    log("RustJNI", "Threads launched");
}