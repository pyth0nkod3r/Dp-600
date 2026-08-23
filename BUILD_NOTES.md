# Android Code Studio compatibility notes

This project does not use C/C++, CMake, or `externalNativeBuild`; **the NDK is not required**.

The app supports Android 8.0 and newer because `minSdk = 26`. `compileSdk` is only the Android API used while compiling; the current dependencies require `compileSdk = 35`. Install Android SDK Platform 35 and its Build Tools in Android Code Studio, then keep JDK 17 selected.

`targetSdk = 35` does not raise the minimum Android version. It opts the app into modern Android behavior while remaining installable on Android 8.0+.

If sync still fails, copy the first complete `FAILURE: Build failed with an exception` block from the Build output. The exact Gradle/SDK error is needed to identify the remaining mismatch.
