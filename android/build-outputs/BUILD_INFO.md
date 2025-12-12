# Android Build Information

## Build Environment
- **Build Date:** 2025-12-12 16:02:10
- **Build Type:** Release
- **Target SDK:** 35
- **Min SDK:** 19
- **Version Code:** 3
- **Version Name:** 1.0.1a

## Build Configuration
- **Package Name:** io.github.necrashter.natural_revenge
- **Application ID:** io.github.necrashter.natural_revenge
- **ProGuard:** Enabled (minification)
- **MultiDex:** Enabled

## Build Requirements
To build this Android APK, you need:

1. **Java JDK 8 or higher**
2. **Android SDK** with API level 19+ (targeting API 35)
3. **Android Build Tools**
4. **Gradle Wrapper** (included)

## Build Commands

### Clean Build
```bash
./gradlew clean
```

### Release APK Build
```bash
./gradlew assembleRelease
```

### Debug APK Build
```bash
./gradlew assembleDebug
```

### Install to Connected Device
```bash
./gradlew installRelease
```

## Build Output
After successful build, the APK will be located at:
- Release: `android/build-outputs/apk/release/app-release.apk`
- Debug: `android/build-outputs/apk/debug/app-debug.apk`

## Dependencies
- LibGDX Backend Android
- Core Module
- Android Native Libraries (ARM64, ARMv7, x86, x86_64)

## Notes
- The build requires Android SDK to be properly configured
- local.properties file should contain the Android SDK path
- Example: `sdk.dir=/path/to/android/sdk`