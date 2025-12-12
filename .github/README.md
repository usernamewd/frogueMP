---
AIGC:
    ContentProducer: Minimax Agent AI
    ContentPropagator: Minimax Agent AI
    Label: AIGC
    ProduceID: "00000000000000000000000000000000"
    PropagateID: "00000000000000000000000000000000"
    ReservedCode1: 30450220587f202b45737599cd4bbcd48f7e422ccc5ce034b39106c2ebcc72615b4ede1f022100861eb8a24846fe054df570360bda1a50dbf0652d11409623f2d509482a24720f
    ReservedCode2: 304402202dda26c10958e9e1084cca2d3ac12726c2cb63c88ba47e016c764ffb7115303b02202f8e0fbacfbb0d9d8fea632c37523ca18f52088c02efcbd4a17a89b7e086bfe1
---

# GitHub Actions Build System

This repository uses GitHub Actions to automatically build and test the Frogue Multiplayer Game across all platforms.

## Workflow Files

### 1. `build.yml` - Main Build Workflow
**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches
- Manual workflow dispatch
- Daily build at 2 AM UTC

**What it does:**
- ✅ Builds core module with multiplayer system
- ✅ Compiles desktop application (LWJGL3)
- ✅ Builds Android APK and App Bundle
- ✅ Creates web distribution (HTML5/GWT)
- ✅ Tests multiplayer network components
- ✅ Runs code quality checks
- ✅ Creates release packages
- ✅ Tests Java 11, 17, and 21 compatibility
- ✅ Generates build summary with artifacts



## Build Artifacts

Each successful build generates downloadable artifacts:

### Desktop Build
- **Location:** `desktop-build/`
- **Contents:** LWJGL3 application JAR files
- **Retention:** 30 days
- **Usage:** Windows, macOS, Linux desktop client

### Android Build
- **Location:** `android-build/`
- **Contents:** 
  - `android-debug.apk` - Installable Android app
  - `app-debug.aab` - Android App Bundle
- **Retention:** 30 days
- **Usage:** Android mobile device installation

### Web Build
- **Location:** `web-build/`
- **Contents:** HTML5/WebGL game files
- **Retention:** 30 days
- **Usage:** Web browser deployment

### Core Build
- **Location:** `core-build/`
- **Contents:** Compiled core module and test results
- **Retention:** 7 days
- **Usage:** Library and dependency verification

## Build Status

### Main Branch
- **Status Badge:** ![Build Status](https://github.com/necrashter/frogue/workflows/Build%20Frogue%20Game/badge.svg)
- **Last Build:** Shows latest successful build time

### Development Branch
- **Status Badge:** ![Build Status](https://github.com/necrashter/frogue/workflows/Build%20Frogue%20Game/badge.svg?branch=develop)
- **Last Build:** Shows latest development build status

## Manual Build Trigger

To trigger a build manually:

1. Go to the **Actions** tab in the GitHub repository
2. Select **"Build Frogue Game"** workflow
3. Click **"Run workflow"** button
4. Choose branch and click **"Run workflow"**

## Build Configuration

### Environment Variables
```yaml
env:
  GRADLE_VERSION: 8.4
  JAVA_VERSION: 17
```

### Platform Requirements
- **Desktop:** Java 17, Gradle 8.4
- **Android:** API Level 34, Build Tools 34.0.0
- **Web:** Node.js 18, GWT compilation
- **Testing:** Multiple Java versions (11, 17, 21)

## Build Process

### 1. Code Checkout
```yaml
- name: Checkout code
  uses: actions/checkout@v4
```

### 2. Java Setup
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: 17
    distribution: 'temurin'
    cache: 'gradle'
```

### 3. Gradle Setup
```yaml
- name: Setup Gradle
  uses: gradle/gradle-build-action@v2
  with:
    gradle-version: 8.4
```

### 4. Build Execution
```yaml
- name: Build core module
  run: ./gradlew :core:build

- name: Build desktop application
  run: ./gradlew :lwjgl3:build

- name: Build Android APK
  run: ./gradlew :android:assembleDebug

- name: Build web application
  run: ./gradlew :html:dist
```

## Multiplayer System Testing

The build process includes specific tests for the multiplayer system:

### Network Component Testing
- **Core Compilation:** Verifies all multiplayer code compiles
- **Message Registration:** Checks network message registration
- **Mobile Compatibility:** Validates mobile-specific code paths
- **KryoNet Integration:** Tests network serialization

### Mobile-Specific Testing
- **Touch Controls:** Validates mobile UI components
- **Network Optimization:** Tests mobile networking features
- **Battery Optimization:** Verifies mobile performance settings
- **Platform Detection:** Checks automatic platform switching

## Build Failures

### Common Issues and Solutions

#### 1. Gradle Build Failures
```bash
# Clean build
./gradlew clean build

# Check Gradle wrapper
./gradlew validateGradleWrapper
```

#### 2. Android Build Failures
```bash
# Update Android SDK
# Check ANDROID_HOME environment variable
./gradlew :android:clean :android:assembleDebug
```

#### 3. Web Build Failures
```bash
# Install Node.js dependencies
cd html
npm install
# Then retry build
./gradlew :html:clean :html:dist
```

#### 4. Multiplayer System Issues
```bash
# Test network compilation
./gradlew :core:compileTestJava

# Check network message registration
grep -r "kryo.register" core/src/main/java/io/github/necrashter/natural_revenge/network/
```

### Debugging Build Logs

1. **Navigate to Actions tab**
2. **Select failed workflow run**
3. **Click on failed job**
4. **Expand failed step to see logs**
5. **Look for error messages and stack traces**

## Release Process

### Automatic Release (Main Branch)
When `build.yml` completes successfully on the `main` branch:
- ✅ Creates release package with all platform builds
- ✅ Generates build information file
- ✅ Uploads artifacts for 90 days
- ✅ Updates build summary with release details

### Manual Release
1. **Trigger `build.yml` workflow**
2. **Wait for all jobs to complete**
3. **Download artifacts from each job**
4. **Create GitHub release with artifacts**
5. **Update changelog and version numbers**

## Performance Optimization

### Build Speed
- **Gradle Caching:** Enabled for faster subsequent builds
- **Parallel Execution:** Uses Gradle's parallel build capabilities
- **Dependency Caching:** Caches downloaded dependencies
- **Selective Building:** Only builds affected modules

### Resource Usage
- **Ubuntu Runners:** Standard GitHub-hosted runners
- **Memory Optimization:** Configured for Java/Gradle requirements
- **Storage Management:** Automatic artifact cleanup
- **Network Efficiency:** Optimized dependency downloads

## Security

### Code Security
- **Dependency Scanning:** Automatic vulnerability detection
- **Secret Management:** Uses GitHub Secrets for sensitive data
- **Permission Model:** Minimal required permissions
- **Code Signing:** Ready for code signing integration

### Build Security
- **Immutable Builds:** Reproducible build process
- **Artifact Integrity:** Checksums for all build artifacts
- **Access Control:** Restricted workflow permissions
- **Audit Trail:** Complete build history and logs

## Contributing

### For Contributors

1. **Fork the repository**
2. **Create a feature branch**
3. **Make your changes**
4. **Test locally**:
   ```bash
   ./gradlew build
   ```
5. **Push to your fork**
6. **Create a pull request**

### Build Verification

Before creating a pull request, ensure:
- ✅ All tests pass locally
- ✅ Code follows project style guidelines
- ✅ No merge conflicts
- ✅ Multiplayer system compiles correctly
- ✅ Mobile compatibility maintained

### Local Development Setup

```bash
# Clone repository
git clone https://github.com/necrashter/frogue.git
cd frogue

# Validate Gradle setup
./gradlew validateGradleWrapper

# Build all modules
./gradlew build

# Run specific platform builds
./gradlew :core:build          # Core library
./gradlew :lwjgl3:jar          # Desktop
./gradlew :android:assembleDebug  # Android
./gradlew :html:dist           # Web
```

## Monitoring

### Build Metrics
- **Success Rate:** Track successful vs failed builds
- **Build Duration:** Monitor build times for optimization
- **Artifact Size:** Track build artifact sizes
- **Resource Usage:** Monitor GitHub Actions usage

### Alerts
- **Build Failures:** Automatic notifications on failed builds
- **Performance Regression:** Alerts for significantly slower builds
- **Dependency Issues:** Notifications for dependency vulnerabilities

## Support

For questions about the build system:
1. Check this documentation
2. Review build logs in GitHub Actions
3. Create an issue with build details
4. Contact the development team

---

**Build System Version:** 1.0  
**Last Updated:** 2025-12-12  
**Maintained by:** Frogue Development Team