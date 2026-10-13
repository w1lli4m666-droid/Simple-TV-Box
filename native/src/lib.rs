use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use md5::{Digest, Md5};
use sha1::Sha1;

fn hex(bytes: &[u8]) -> String {
    let mut result = String::with_capacity(bytes.len() * 2);
    for value in bytes {
        use core::fmt::Write;
        let _ = write!(result, "{value:02x}");
    }
    result
}

#[no_mangle]
pub extern "system" fn Java_win_movie8_tv_NativeBridge_sign(
    mut env: JNIEnv,
    _class: JClass,
    canonical: JString,
    key: JString,
    timestamp: JString,
) -> jstring {
    let fallback = || env.new_string("").map(|value| value.into_raw()).unwrap_or(core::ptr::null_mut());
    let canonical: String = match env.get_string(&canonical) { Ok(value) => value.into(), Err(_) => return fallback() };
    let key: String = match env.get_string(&key) { Ok(value) => value.into(), Err(_) => return fallback() };
    let timestamp: String = match env.get_string(&timestamp) { Ok(value) => value.into(), Err(_) => return fallback() };
    let separator = if canonical.is_empty() { "" } else { "&" };
    let mut md5 = Md5::new();
    md5.update(format!("{canonical}{separator}key={key}&t={timestamp}").as_bytes());
    let md5_text = hex(&md5.finalize());
    let mut sha1 = Sha1::new();
    sha1.update(md5_text.as_bytes());
    match env.new_string(hex(&sha1.finalize())) { Ok(value) => value.into_raw(), Err(_) => core::ptr::null_mut() }
}
