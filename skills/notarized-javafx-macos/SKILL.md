---
name: notarized-javafx-macos
description: Build, sign, notarize, staple, validate, and troubleshoot macOS DMG releases for Java and JavaFX applications packaged with jlink/jpackage. Use when creating Developer ID signed Java desktop apps, configuring GitHub Actions release workflows, handling Apple notarization, or debugging Gatekeeper, hardened runtime, JVM JIT, JavaFX native library, DMG signing, and macOS launch failures.
license: GPL-3.0
compatibility: Requires macOS for local signing/notarization checks, Apple Developer Program credentials, Xcode command line tools, JDK/jpackage, and GitHub Actions or equivalent CI.
metadata:
  version: "1.0"
---

# Notarized Java/JavaFX macOS app releases

Use this skill when an agent needs to package a Java or JavaFX desktop app for macOS with Developer ID signing, notarization, stapling, and Gatekeeper validation. It is especially useful for apps built with `jlink` and `jpackage` that ship as `.app`, `.dmg`, or `.pkg` artifacts.

## Outcomes

A successful release should produce a macOS artifact that:

1. Contains a signed `.app` bundle.
2. Uses hardened runtime.
3. Has JVM/JavaFX-compatible entitlements when needed.
4. Is packaged into a signed DMG or PKG.
5. Is accepted by Apple notarization.
6. Is stapled successfully.
7. Passes Gatekeeper validation.
8. Launches from Finder after the standard first-run "downloaded from the internet" prompt.

## Required Apple and CI inputs

For GitHub Actions, configure these as repository or environment secrets:

| Secret | Purpose |
|---|---|
| `MACOS_CERTIFICATE_BASE64` | Base64-encoded Developer ID Application `.p12` certificate |
| `MACOS_CERTIFICATE_PASSWORD` | Password for the exported `.p12` |
| `MACOS_SIGNING_KEY_USER_NAME` | Developer ID Application identity, usually `Developer ID Application: Name (TEAMID)` |
| `APPLE_ID` | Apple ID email used for notarization |
| `APPLE_TEAM_ID` | Apple Developer Team ID |
| `APPLE_APP_SPECIFIC_PASSWORD` | App-specific password for notarization |

Export and encode a certificate locally:

```bash
security find-identity -v -p codesigning
base64 -i path/to/developer-id-application.p12 | tr -d '\n'
```

## Recommended release workflow

### 1. Create and unlock a temporary keychain

In CI, import the `.p12` certificate into a temporary keychain and make it usable by `codesign`.

```bash
certificate_path="${RUNNER_TEMP}/developer-id-application.p12"
keychain_path="${RUNNER_TEMP}/app-signing.keychain-db"
keychain_password="$(uuidgen)"

printf '%s' "${MACOS_CERTIFICATE_BASE64}" | base64 --decode > "${certificate_path}"
security create-keychain -p "${keychain_password}" "${keychain_path}"
security set-keychain-settings -lut 21600 "${keychain_path}"
security unlock-keychain -p "${keychain_password}" "${keychain_path}"
security import "${certificate_path}" -P "${MACOS_CERTIFICATE_PASSWORD}" -A -t cert -f pkcs12 -k "${keychain_path}"
security list-keychains -d user -s "${keychain_path}"
security set-key-partition-list -S apple-tool:,apple:,codesign: -s -k "${keychain_password}" "${keychain_path}"
security find-identity -v -p codesigning "${keychain_path}"
```

### 2. Build a runtime image and signed app image

Prefer a two-stage macOS package flow:

1. Build the application and `jlink` runtime.
2. Create a signed `app-image`.
3. Explicitly re-sign the `.app` with known entitlements.
4. Build the DMG from that signed app image.

This avoids silently losing entitlements during direct `jpackage --type dmg` packaging.

Example:

```bash
./mvnw --batch-mode --no-transfer-progress -DskipTests package javafx:jlink

jpackage \
  --type app-image \
  --dest target/app-image \
  --name fx2048 \
  --app-version "${PROJECT_VERSION}" \
  --vendor "Example Vendor" \
  --resource-dir src/jpackage \
  --module fxgame/io.fxgame.game2048.AppLauncher \
  --runtime-image target/fx2048 \
  --java-options -Dfile.encoding=UTF-8 \
  --java-options -Xmx48m \
  --java-options -XX:+UseZGC \
  --mac-package-identifier fx2048 \
  --mac-package-name fx2048 \
  --icon src/main/resources/io/fxgame/game2048/fx2048-logo.icns \
  --mac-sign \
  --mac-signing-keychain "${MACOS_KEYCHAIN_PATH}" \
  --mac-signing-key-user-name "${MACOS_SIGNING_KEY_USER_NAME}" \
  --mac-entitlements src/jpackage/fx2048.entitlements
```

### 3. Use JVM/JavaFX-compatible entitlements

JavaFX and HotSpot may need hardened-runtime exceptions for the JVM JIT and JavaFX native libraries. Use the narrowest entitlements that make the app launch reliably.

Recommended starting point for JavaFX apps:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "https://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.security.cs.allow-jit</key>
    <true/>
    <key>com.apple.security.cs.allow-unsigned-executable-memory</key>
    <true/>
    <key>com.apple.security.cs.disable-library-validation</key>
    <true/>
</dict>
</plist>
```

Why these matter:

- `allow-jit`: permits the JVM JIT to toggle JIT write protection.
- `allow-unsigned-executable-memory`: helps JVM-generated executable memory under hardened runtime.
- `disable-library-validation`: permits JavaFX native libraries extracted to user cache locations, such as `~/.openjfx/cache`, to load even when signed by a different team.

### 4. Re-sign and verify the app image explicitly

After `jpackage --type app-image`, explicitly re-sign the `.app` bundle with hardened runtime and entitlements. Then verify that the entitlement keys are actually present.

```bash
app_image="target/app-image/fx2048.app"

codesign --force --timestamp --options runtime \
  --entitlements src/jpackage/fx2048.entitlements \
  --keychain "${MACOS_KEYCHAIN_PATH}" \
  --sign "${MACOS_SIGNING_KEY_USER_NAME}" \
  "${app_image}"

codesign --verify --deep --strict --verbose=4 "${app_image}"
codesign -d --entitlements - "${app_image}" 2>&1 | tee /tmp/app-entitlements.plist
grep -q 'com.apple.security.cs.allow-jit' /tmp/app-entitlements.plist
grep -q 'com.apple.security.cs.disable-library-validation' /tmp/app-entitlements.plist
```

Do not assume `jpackage --mac-entitlements` worked just because the build succeeded. Always inspect the installed or generated `.app` entitlements.

### 5. Build the DMG from the signed app image

```bash
jpackage \
  --type dmg \
  --dest target/dist \
  --name fx2048 \
  --app-version "${PROJECT_VERSION}" \
  --vendor "Example Vendor" \
  --resource-dir src/jpackage \
  --app-image "${app_image}" \
  --mac-package-identifier fx2048 \
  --mac-package-name fx2048
```

### 6. Sign the DMG

Some DMGs notarize successfully but still fail Gatekeeper with `source=no usable signature` unless the disk image itself has a usable Developer ID signature.

```bash
packages=( target/dist/*.dmg )
if [ "${#packages[@]}" -ne 1 ]; then
  printf 'Expected exactly one DMG, found %s.\n' "${#packages[@]}"
  printf '%s\n' "${packages[@]}"
  exit 1
fi

codesign --force --timestamp \
  --keychain "${MACOS_KEYCHAIN_PATH}" \
  --sign "${MACOS_SIGNING_KEY_USER_NAME}" \
  "${packages[0]}"

codesign --verify --verbose=4 "${packages[0]}"
```

### 7. Notarize, staple, and validate

```bash
xcrun notarytool submit "${packages[0]}" \
  --apple-id "${APPLE_ID}" \
  --team-id "${APPLE_TEAM_ID}" \
  --password "${APPLE_APP_SPECIFIC_PASSWORD}" \
  --wait

xcrun stapler staple "${packages[0]}"
xcrun stapler validate "${packages[0]}"
spctl -a -t open --context context:primary-signature -v "${packages[0]}"
```

## Diagnostics and common failures

### Normal first-launch prompt

It is normal for a signed and notarized app downloaded from GitHub to show a first-run warning like "downloaded from the internet." It should identify the developer and allow the app to open. It should not say "cannot verify developer," "app is damaged," or "cannot be opened."

### Check an installed app

```bash
defaults read /Applications/AppName.app/Contents/Info CFBundleShortVersionString
codesign --verify --deep --strict --verbose=4 /Applications/AppName.app
codesign -d --entitlements - /Applications/AppName.app 2>&1
/Applications/AppName.app/Contents/MacOS/AppName
```

Launching the executable from Terminal is often the fastest way to see Java or JavaFX errors that Finder hides.

### `pthread_jit_write_protect_np` crash during JVM startup

Symptom:

```text
Thread crashed: pthread_jit_write_protect_np
Threads::create_vm
JNI_CreateJavaVM
```

Cause: hardened runtime blocked JVM JIT behavior.

Fix: ensure the signed `.app` has `com.apple.security.cs.allow-jit` and often `com.apple.security.cs.allow-unsigned-executable-memory`. Re-sign and verify the app image before creating the DMG.

### JavaFX fails with `different Team IDs`

Symptom:

```text
Loading library prism_mtl from resource failed
not valid for use in process: mapping process and mapped file (non-platform) have different Team IDs
Graphics Device initialization failed
No toolkit found
```

Cause: JavaFX extracted native libraries to `~/.openjfx/cache`, and hardened runtime library validation rejected them.

Fix: add `com.apple.security.cs.disable-library-validation` to the signed app entitlements and verify it is present on the installed app.

### `source=no usable signature` after notarization

Symptom:

```text
target/dist/app.dmg: rejected
source=no usable signature
```

Cause: the DMG itself is not signed with a usable primary signature, even if the app inside it was signed and Apple accepted notarization.

Fix: `codesign` the DMG before notarization, then staple and validate it.

### Notarization succeeds but the app still does not launch

Notarization confirms Apple accepted the submitted artifact. It does not prove the app can initialize the JVM or JavaFX. Always smoke test the installed `.app` after downloading the release artifact:

```bash
/Applications/AppName.app/Contents/MacOS/AppName
```

## Pull request checklist

Before declaring the macOS release fix complete:

- The entitlements plist passes `plutil -lint`.
- The workflow YAML parses.
- The `.app` signature is verified with `codesign --verify --deep --strict`.
- `codesign -d --entitlements - App.app` shows required entitlement keys.
- The DMG is signed.
- `notarytool submit --wait` returns `Accepted`.
- `stapler staple` and `stapler validate` pass.
- `spctl -a -t open --context context:primary-signature -v App.dmg` passes.
- The app launches from Terminal and Finder after installation.
