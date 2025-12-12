# Frogue Android Build

This directory contains the Android build configuration and output for the Frogue game.

## Quick Start

To build the Android APK:

1. **Prerequisites:**
   - Java JDK 8 or higher
   - Android SDK (API 19+)
   - Android Build Tools

2. **Build Commands:**
   ```bash
   # Make script executable
   chmod +x android/build-apk.sh
   
   # Run build script
   ./android/build-apk.sh
   ```

3. **Alternative Manual Build:**
   ```bash
   cd android
   ./gradlew assembleRelease
   ```

## Build Output

After successful build:
- **Release APK:** `android/build-outputs/apk/release/app-release.apk`
- **Debug APK:** `android/build-outputs/apk/debug/app-debug.apk`

## Build Files

- `build-apk.sh` - Automated build script
- `BUILD_INFO.md` - Detailed build information
- `RELEASE_NOTES.md` - Version release notes
- `build-outputs/` - Build output directory
- `build/` - Gradle build cache and intermediates

## Configuration

- **Package:** io.github.necrashter.natural_revenge
- **Min SDK:** 19 (Android 4.4)
- **Target SDK:** 35 (Android 15)
- **Version:** 1.0.1a (Build 3)

## Architecture Support

The APK includes native libraries for:
- ARM64 (64-bit ARM)
- ARMv7 (32-bit ARM)
- x86 (32-bit Intel/AMD)
- x86_64 (64-bit Intel/AMD)

## Notes

This is a LibGDX-based Android application with full multiplayer support and mobile-optimized controls.