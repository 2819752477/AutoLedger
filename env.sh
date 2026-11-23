# Android 构建环境（在 proot Ubuntu / aarch64 上）
export JAVA_HOME=/opt/jdk17
export ANDROID_HOME=/opt/android-sdk
export ANDROID_SDK_ROOT=/opt/android-sdk
export PATH=$JAVA_HOME/bin:/opt/gradle/gradle-8.9/bin:/opt/android-sdk/cmdline-tools/latest/bin:$PATH
# 注意：platform-tools/adb 是 aarch64 ELF，在 proot 下无法直接执行（Exec format error），
# 但 Gradle 编译不依赖 adb 本体，装机用手机端或 dsh-go 的 install 即可。
