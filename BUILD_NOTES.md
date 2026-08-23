# Android Code Studio compatibility notes

This project does not use C/C++, CMake, or `externalNativeBuild`; **the NDK is not required**.

The project now targets Android API 33 to align with the Android SDK 33.0.1 shown in the IDE configuration screenshots. Keep JDK 17 selected.

If sync still fails, copy the first complete `FAILURE: Build failed with an exception` block from the Build output. The exact Gradle/SDK error is needed to identify the remaining mismatch.
