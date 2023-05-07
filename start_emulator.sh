sdkmanager 'system-images;android-33;google_apis;arm64-v8a'
avdmanager delete avd -n test
avdmanager -v create avd -n test -d "pixel_xl" -k  'system-images;android-33;google_apis;arm64-v8a'
~/Library/Android/sdk/emulator/emulator -avd test -gpu swiftshader_indirect