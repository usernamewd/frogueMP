#!/bin/bash
# Android Build Script for Frogue
# This script demonstrates the build process for the Frogue Android APK

echo "=== FROGUE ANDROID BUILD SCRIPT ==="
echo "Starting build process..."

# Set environment variables
export ANDROID_HOME=${ANDROID_HOME:-"/path/to/android/sdk"}
export JAVA_HOME=${JAVA_HOME:-"/path/to/java/home"}

echo "Android SDK: $ANDROID_HOME"
echo "Java Home: $JAVA_HOME"

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java JDK 8 or higher"
    exit 1
fi

# Check if Android SDK is available
if [ ! -d "$ANDROID_HOME" ]; then
    echo "ERROR: Android SDK not found at $ANDROID_HOME"
    echo "Please install Android SDK and set ANDROID_HOME"
    exit 1
fi

echo "Java version:"
java -version

echo ""
echo "Starting Gradle build..."

# Make gradlew executable
chmod +x gradlew

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build release APK
echo "Building release APK..."
./gradlew assembleRelease

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ BUILD SUCCESSFUL!"
    echo "Release APK location: android/build-outputs/apk/release/app-release.apk"
    
    # Show APK info
    if command -v aapt &> /dev/null; then
        echo ""
        echo "APK Information:"
        aapt dump badging android/build-outputs/apk/release/app-release.apk | head -5
    fi
    
    echo ""
    echo "Build artifacts:"
    find android/build -name "*.apk" -exec ls -lh {} \;
    
else
    echo ""
    echo "❌ BUILD FAILED!"
    echo "Please check the error messages above"
    exit 1
fi

echo ""
echo "Build completed at: $(date)"