# SharedSecrets
A small Android app for verifying a password between two people.

### How it works

1.  Enter a secret string or password.
2.  Click 'OK'
    This will take the current time - rounded down, so the minutes are a multiple of five - as a salt and generate a SHA-256 hash from it. This hash will then be displayed as a QR Code on your screen.
3.  Press 'CHECK SECRET' to check the QR Code of the other person with the integrated QR Code scanner. 
    If they have entered the same secret string as you have at roughly the same time, then the SHA-256 hashes will be the same and the app will let you know that you both know the same secret.
