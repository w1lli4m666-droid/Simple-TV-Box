# Simple TV Box optional Rust core

This optional Android JNI library implements the bundled Wencai request-signing
algorithm. The UI and Media3 player run in the Java Android shell. The APK works
without this library using the Java implementation; no Rust binary is included
by the standard Gradle build. No Windows application is currently provided.

To integrate a native build, compile the cdylib with the Android NDK for
armeabi-v7a and arm64-v8a, then place libmovie8_core.so under the corresponding
app/src/main/jniLibs ABI directories before building the APK. Use Android API 21
as the minimum native platform level. Native compilation is an optional separate
step, not part of the Gradle task.
