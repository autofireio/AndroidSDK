![Autofire logo](http://autofire.io/wp-content/themes/autofire/img/logo_ext.png)

#Autofire Java SDKs

---


Java SDKs for Autofire


---

<!-- START TOC -->
**Table of Contents**

- [Requirements](#requirements)
- [Source distribution](#source-distribution)
  - [No-Android flavour](#source-no-android)
  - [Android flavour](#source-android)
  - [Build](#source-build)
- [Binary distribution](#binary-distribution)

<!-- END TOC -->

<a name="requirements">
## Requirements
</a>

- JDK 1.6 or higher

- Android SDK (for the Android flavour only)

<a name="source-distribution">
## Source distribution
</a>

- JavaSE SDK

  * The Autofire JavaSE SDK supports Java 1.6 or higher.

  * The Autofire cached and persisted files are stored in the user's home `.autofire` directory.

- Android SDK

  * The Autofire Android SDK supports Android versions 4.0 (IceCreamSandwich - API 14) or higher.

  * The Autofire cached and persisted files are stored in the host application's private storage space. The Autofire Player Id is stored in the device external storage if available or the host application's private storage space.

  * Host applications should have the following permission in their `AndroidManifest.xml`

      `<uses-permission android:name="android.permission.INTERNET" />`

  - Host applications that want to use the device external storage should have the following permission in their `AndroidManifest.xml`
    
      `<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />`

  - Host applications that want to be accurate on the availability of the internet connection should have the following permission in their `AndroidManifest.xml`
    
      `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`

<a name="source-no-android">
### No-Android flavour
</a>

- Go to the `prj-no-android` directory and follow the steps described in [Build](#source-build).

- To create an Eclipse project, run `./gradlew eclipse` (Linux, Mac OS X) or `gradlew.bat eclipse` (Windows).

- To create an IntelliJ IDEA project, run `./gradlew idea` (Linux, Mac OS X) or `gradlew.bat idea` (Windows).

<a name="source-android">
### Android flavour
</a>

- Go to the `prj-android` directory and follow the steps described in [Build](#source-build).

<a name="source-build">
### Build
</a>

- Build with

  `./gradlew build` (on Linux, Mac OS X)

  `gradlew.bat build` (on Windows)

- Run unit tests with

  `./gradlew test` (on Linux, Mac OS X)

  `gradlew.bat test` (on Windows)

- Publish to local Maven repository with

  `./gradlew install` (on Linux, Mac OS X)

  `gradlew.bat install` (on Windows)

<a name="binary-distribution">
## Binary distribution
</a>

- The Autofire JavaSE SDK `.jar` file is located in the `autofirejavase/build/libs` directory.

- The Autofire Android SDK `.aar` files (debug and release) are located in the `autofireandroid/build/outputs/aar` directory.

  * The affore-mentioned `.aar` files are present if the Autofire SDK was built with Android support (i.e. the Android flavour)

Have fun!

---

Copyright (c) 2017 Autofire - Game Analytics | <http://autofire.io>