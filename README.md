Private Period Tracker
======================

This app is a period tracker with a strong focus on privacy and security.

- There is no advertising. No external tracking or logging. No network backup or storage.
- There is a "duress" feature, where if setup, will allow the user to use an alternative password,
  which will show completely randomised data.
  The randomised data can be interacted with as if it were real, and there is no way to tell the
  difference.
- All data is encrypted with AES-GCM, with 256bit keys.
- Encryption keys are generated from the password using Argon2 (Signal Foundation library)
- IV's and Salts are generated each time a file is saved.
