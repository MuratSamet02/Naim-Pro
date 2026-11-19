#!/usr/bin/env bash
# Release APK'yi emülatörde açar ve çökme olup olmadığını kontrol eder.
# android-emulator-runner her satırı ayrı bir kabukta çalıştırdığı için tüm
# adımlar tek dosyada toplandı.

set -o pipefail

APK="$(ls app/build/outputs/apk/release/*x86_64*.apk 2>/dev/null | head -n1)"
if [ -z "$APK" ]; then
  echo "APK bulunamadı"
  exit 1
fi
echo "APK: $APK"

adb install -r "$APK" || exit 1
adb logcat -c || true

adb shell am start -W -n com.naimpro.app/.MainActivity || true
sleep 20

echo "=== odaklanan pencere ==="
adb shell dumpsys window 2>/dev/null | grep -E "mCurrentFocus|mFocusedApp" || true

echo "=== süreç ==="
adb shell pidof com.naimpro.app || echo "PROCESS_DEAD"

echo "=== crash buffer ==="
adb logcat -d -b crash || true

echo "=== uygulama günlüğü ==="
adb logcat -d | grep -E "AndroidRuntime|FATAL|naimpro|Naim" | tail -n 200 || true

if adb logcat -d -b crash | grep -q "com.naimpro.app"; then
  echo "SONUC: CRASH"
  exit 1
fi

if ! adb shell pidof com.naimpro.app > /dev/null; then
  echo "SONUC: SUREC_OLU"
  exit 1
fi

echo "SONUC: OK"
