# Simple TV Box

[English](README.md) | [简体中文](README.zh-CN.md)

[![Release](https://img.shields.io/github/v/release/w1lli4m666-droid/Simple-TV-Box?style=flat-square&color=cb3837)](https://github.com/w1lli4m666-droid/Simple-TV-Box/releases/latest)
[![Android](https://img.shields.io/badge/Android-5.0%2B-339933?style=flat-square&logo=android&logoColor=white)](https://github.com/w1lli4m666-droid/Simple-TV-Box/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-f4c430?style=flat-square)](LICENSE)

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="96" height="96" alt="Simple TV Box icon">

A lightweight Android video player for TV remotes and touchscreens, previously
named PureBox. The current source adapter supports Wencai from the default TVBox
subscription `http://www.饭太硬.net/tv`. This is an Android app, not an npm package;
Node.js is not required. A Windows application is not included.
This project distributes sideloaded APKs; it is not a Google Play release.

## Download

Download the APK from [Releases](https://github.com/w1lli4m666-droid/Simple-TV-Box/releases/latest).
Requires Android 5.0 (API 21) or newer; the APK includes ARMv7 and ARM64 support.
Enable installation from your file manager or browser when Android requests it.
The package ID remains `win.movie8.tv` so existing PureBox installations can be
updated without losing watch history when the signing certificate matches.
The initial release reuses the existing local development signing certificate
for upgrade compatibility; its private key is not distributed.

### Version 1.1.2

[Download the signed ARMv7 + ARM64 APK](https://github.com/w1lli4m666-droid/Simple-TV-Box/releases/download/v1.1.2/Simple-TV-Box-v1.1.2-android5-arm.apk)
or read the [release notes](docs/releases/v1.1.2.md).

## Features

- Recently updated, movies, TV shows, variety and anime tabs; five-column covers.
- Search, episode selection in an eight-column grid, and ascending/descending order.
- Local watch history with episode and playback-position restoration.
- D-pad navigation, remote playback controls and touchscreen seeking.
- Focus a top navigation tab to open it automatically, including Search and
  Watch history. Navigation stays available on those pages.
- Keep the screen awake during active playback.
- Cached first-page previews for all five categories, followed by a background refresh.
- Search suggestions after a short typing pause, a loading spinner, and local
  search-history cards with long-press deletion and a clear-all control.
- Dynamic subscription/plugin checking at startup with a bundled API fallback.
- English by default; Simplified Chinese devices display Chinese UI and the
  launcher name **简单电视**. Other devices display **Simple TV Box**.

Titles, descriptions and source messages come from the provider and are not
automatically translated. Changing the device language and reopening the app
updates the interface; some launchers may need time to refresh the displayed name.

## Controls

| Action | TV remote | Touchscreen |
| --- | --- | --- |
| Navigate | D-pad | Scroll and tap |
| Switch category | Focus a category tab | Tap a category tab |
| Open Search / Watch history | Focus the corresponding top tab | Tap the corresponding top tab |
| Refresh category | Confirm on the current tab | Tap the current tab |
| Enter fullscreen | Confirm on the video preview | Tap the video preview |
| Play / pause in fullscreen | Confirm | Center playback button |
| Seek in fullscreen | Left / right when controls are hidden | Swipe horizontally |
| Show / hide bottom controls | Settings, Menu or Info | Tap an empty video area |
| Delete a history entry | Long-press Confirm | Long-press the entry |
| Select a search suggestion | Down, then Confirm to search immediately | Tap the suggestion |
| Reuse / delete a saved search | Confirm / long-press Confirm | Tap / long-press the card |
| Return from Search / Watch history content | Back focuses the current top tab and resets list scroll | Back gesture |
| Return from deep results | Back returns to the first row and navigation | Back gesture |
| Exit from the library top navigation | Back twice within 1.8 seconds | Back twice within 1.8 seconds |

## Search and startup behavior

After a 400 ms typing pause, the app requests one page from Wencai and shows up
to five distinct matching titles below the input. Selecting a title immediately
starts a full search; an orange spinner stays visible while that search runs.
Stale responses cannot replace a newer query. If the device keyboard captures
directional keys, close it with Back before moving Down to the suggestions.

When the input is empty, saved searches appear as compact multi-column cards
(up to five columns on TV). One entry does not stretch across the entire row.
The trash icon remains at the upper-right edge, above the last card slot,
regardless of the number of entries. Long-press a card to delete one entry, or
use the trash icon to clear them all; both actions ask for confirmation.
Left / right stays within the current row of search-history cards and search
results. At either edge, focus stays on the current card instead of jumping
to an unrelated top navigation tab.

On Search and Watch history, pressing Back from content first returns focus to
that page's own top tab and brings lists back to the first row. It does not
immediately leave the page for the previously selected category. With focus
already in the top navigation, Back retains the normal page-return behavior.
An open keyboard or confirmation dialog handles Back first.

The first-page cache is retained for up to 24 hours. All five categories can
display cached lists while fresh data loads in the background. The four category
responses already downloaded for Recently updated are also cached; no extra
prefetch requests are needed. A first launch without cache, an expired cache or
plugin initialization after an APK update can still require several seconds.
Caching improves time to visible content; it does not eliminate plugin
initialization or network latency.

## Build

Install JDK 17+ and Android SDK Platform 35. Set `ANDROID_HOME` to your SDK folder,
or add an untracked `local.properties` containing `sdk.dir=...`.

```sh
./gradlew assembleDebug
```

On Windows, use `gradlew.bat assembleDebug`. The output is
`app/build/outputs/apk/debug/app-debug.apk`. The Java fallback means Rust is not
required to build the APK. Optional JNI signing code lives in [`native/`](native/).

To build a signed release, supply your own signing environment variables:
`ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` and
`ANDROID_KEY_PASSWORD`, then run `./gradlew assembleRelease`. Without signing
variables, the release build is unsigned. Signing keys are never committed.

## Source and privacy

The app checks the TVBox subscription and downloads its executable plugin to
private app storage. **Only use sources you trust:** these third-party plugins
execute inside the app and are not covered by this repository's license. The
subscription currently uses HTTP, so its transport is not encrypted. MD5 checks
detect mismatched downloads but do not establish the provider's authenticity.

Watch history and search history stay in the device's local app storage. The
app does not upload them to this repository or a project-operated server. The provider and IP-lookup
services can still see network requests and the device's public IP. Source
availability, content access and rights depend on the provider. No videos,
third-party plugin binaries, captured account data or signing keys are bundled
in this repository. There is no diagnostic screen or LAN log server.

## License

Project source code is released under [MIT](LICENSE). See [NOTICE](NOTICE.md) for
third-party dependencies and launcher artwork notices.

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=w1lli4m666-droid/Simple-TV-Box&type=Date)](https://www.star-history.com/#w1lli4m666-droid/Simple-TV-Box&Date)
