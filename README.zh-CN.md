# 简单电视 / Simple TV Box

[English](README.md) | [简体中文](README.zh-CN.md)

[![Release](https://img.shields.io/github/v/release/w1lli4m666-droid/Simple-TV-Box?style=flat-square&color=cb3837)](https://github.com/w1lli4m666-droid/Simple-TV-Box/releases/latest)
[![Android](https://img.shields.io/badge/Android-5.0%2B-339933?style=flat-square&logo=android&logoColor=white)](https://github.com/w1lli4m666-droid/Simple-TV-Box/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-f4c430?style=flat-square)](LICENSE)

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="96" height="96" alt="简单电视图标">

面向低配置安卓电视盒子和触屏设备的轻量播放器，原名 PureBox。
目前仅实现默认 TVBox 订阅 `http://www.饭太硬.net/tv` 中的文采源。
这是安卓应用，不是 npm 包，不需要 Node.js；当前没有 Windows 应用。
本项目通过 APK 侧载分发，不是 Google Play 上架版本。

## 下载安装

从 [Releases](https://github.com/w1lli4m666-droid/Simple-TV-Box/releases/latest)
下载 APK，支持 Android 5.0（API 21）及以上的 ARMv7、ARM64 设备。
安装时按系统提示允许当前文件管理器或浏览器安装应用。
包名继续使用 `win.movie8.tv`；签名一致时可以覆盖安装旧 PureBox，保留播放记录。
首个发布版沿用原有本地开发签名证书以支持覆盖升级，私钥不对外分发。

### 1.1.1 版本

[下载已签名的 ARMv7 + ARM64 APK](https://github.com/w1lli4m666-droid/Simple-TV-Box/releases/download/v1.1.1/Simple-TV-Box-v1.1.1-android5-arm.apk)，
或查看[版本说明](docs/releases/v1.1.1.md)。

## 功能

- 最近更新、电影、剧集、综艺、动漫；每行五张封面。
- 搜索、每行八集的集数选择、正序与倒序。
- 浏览器无关的本地播放记录，恢复原集数和观看进度。
- 遥控器方向键操作、全屏播放控制、触屏滑动调整进度。
- 顶部栏目获得焦点后自动进入，包括搜索、播放记录；这两个页面同样保留顶部导航。
- 播放期间保持屏幕唤醒。
- 五个分类均支持首屏缓存预览，再后台刷新最新结果；最近更新请求取得的分类列表也会复用。
- 输入停顿后展示片名联想，正式搜索显示转圈动画；本地搜索记录以卡片展示，支持长按删除和全部清空。
- 每次启动检查动态订阅和插件，保留内置接口兜底。
- 默认英文；设备为简体中文时自动显示中文，桌面名称为**简单电视**；
  其他语言设备的名称为 **Simple TV Box**。

影视名称、简介及上游返回的信息由内容源提供，不会自动翻译。
更改设备语言后重新打开应用即可更新界面；部分桌面可能需要等待名称缓存刷新。

## 操作

| 操作 | 遥控器 | 触屏 |
| --- | --- | --- |
| 浏览结果 | 方向键 | 滑动、点击 |
| 切换分类 | 焦点移至分类按钮 | 点击分类按钮 |
| 进入搜索／播放记录 | 焦点移至对应顶部栏目 | 点击对应顶部栏目 |
| 刷新分类 | 确认当前分类按钮 | 点击当前分类按钮 |
| 进入全屏 | 在视频小窗口按确认 | 点击视频小窗口 |
| 全屏暂停／播放 | 确认键 | 中央播放按钮 |
| 全屏调整进度 | 控件隐藏时按左／右 | 横向滑动视频 |
| 显示／隐藏底部栏 | 设置、菜单或信息键 | 点击视频空白区域 |
| 删除单条播放记录 | 长按确认 | 长按记录 |
| 选择搜索联想 | 下移选择，再按确认立即搜索 | 点击候选片名 |
| 复用／删除搜索记录 | 确认／长按确认 | 点击／长按卡片 |
| 搜索／播放记录内容区返回 | 列表归顶，焦点返回当前顶部栏目 | 返回手势 |
| 深层结果返回 | 返回第一行及顶部导航 | 返回手势 |
| 片库顶部导航退出 | 1.8 秒内连续按两次返回 | 1.8 秒内连续返回两次 |

## 搜索与加载说明

输入停顿约 400 毫秒后，应用会从文采请求一页结果，在输入框下方显示最多 5 个去重后的相关片名。
确认候选后立即执行完整搜索；搜索期间显示橙色转圈动画，旧请求不会覆盖新查询的结果。
如果设备输入法占用了方向键，请先按返回关闭输入法，再按下键选择候选。

输入框为空时展示本地搜索记录卡片，电视端每行最多 5 张。
只有一条记录时也保持单个格子的宽度，不会撑满整行。
垃圾桶图标固定在第一行最右侧卡片槽位上方，不随记录数量左移。
长按卡片删除单条，点击垃圾桶清空全部；两种操作均有确认提示。

在搜索、播放记录的内容区按遥控器返回键，先将列表归顶，焦点回到当前顶部的“搜索”或“播放记录”，
不会直接跳回先前的“动漫”等分类。焦点已经位于顶部导航时，再按返回仍沿用原来的页面返回行为。
输入法或确认窗口打开时，返回键会先由它们处理。

首屏缓存最多保留 24 小时。五个分类都可以先显示缓存，再后台获取最新数据；
“最近更新”已经下载的四个分类列表也会缓存复用，不增加额外预加载请求。
首次没有缓存、缓存过期，或 APK 更新后需要初始化插件时，仍可能等待数秒。
缓存优化的是内容出现速度，并不消除插件初始化及网络请求本身的耗时。

## 构建

安装 JDK 17+、Android SDK Platform 35。
通过 `ANDROID_HOME` 指定 SDK 目录，或在不提交的 `local.properties` 中设置 `sdk.dir=...`。

```sh
./gradlew assembleDebug
```

Windows 使用 `gradlew.bat assembleDebug`，产物位于
`app/build/outputs/apk/debug/app-debug.apk`。
应用包含 Java 签名实现，构建 APK 不强制依赖 Rust；可选 JNI 源码位于 [`native/`](native/)。

签名发布版需要配置环境变量：`ANDROID_KEYSTORE_PATH`、`ANDROID_KEYSTORE_PASSWORD`、
`ANDROID_KEY_ALIAS`、`ANDROID_KEY_PASSWORD`，然后执行 `./gradlew assembleRelease`。
没有签名配置时产物为未签名 APK。不要提交签名密钥。

## 内容源与隐私

应用会检查 TVBox 订阅，并将其可执行插件下载至应用私有目录。
**请仅使用可信内容源**：第三方插件在应用内执行，不属于本仓库的 MIT 授权范围。
当前订阅使用 HTTP，传输不加密；MD5 可以检测下载内容是否一致，但不能认证发布者身份。

播放记录和搜索记录保存在设备本地，不上传到本仓库或项目自建服务器。
内容源和公网 IP 查询服务仍可以看到网络请求及公网 IP。
资源可用性、访问条件和内容版权由提供方决定。
仓库不包含影视文件、第三方插件二进制、抓包账户数据或签名密钥。
应用已移除日志页面和局域网日志服务。

## 许可证

项目源码采用 [MIT](LICENSE)；依赖库及启动图标说明见 [NOTICE](NOTICE.md)。

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=w1lli4m666-droid/Simple-TV-Box&type=Date)](https://www.star-history.com/#w1lli4m666-droid/Simple-TV-Box&Date)
