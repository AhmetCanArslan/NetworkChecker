# Network Checker

A minimal Android application designed to test firewall behavior. Its primary purpose is to verify if a firewall application correctly applies rules based on the app's current lifecycle state (foreground vs background).

## How it works

The app runs a persistent background service (Foreground Service) that attempts to make an HTTP request every 3 seconds to test actual network connectivity. 

For each network check, the app records:
1. The timestamp.
2. The current lifecycle state of the app (Foreground or Background).
3. The result of the network request (Connected or Blocked).

This combination allows developers to easily test firewall logic. For example, if a firewall policy is set to block access when an app is in the background and allow access when it is in the foreground, this app will log the exact moments the state transitions and verify if the network requests were correctly allowed or blocked.

## Features

- **Continuous Network Testing**: Runs an ongoing connectivity check every 3 seconds.
- **Persistent Logging**: Logs are saved locally and persist across app closures and device restarts.
- **Lifecycle Tracking**: Accurately labels network requests based on whether the app was visible to the user or running in the background.
- **Manual Control**: Includes simple controls to start the service, stop the service, and clear the saved logs.

## Usage Guide

1. Open the application.
2. Tap "Start Service" to begin networking checks. Grant the notification permission if prompted.
3. Observe the logs appearing in the list. They will show "Foreground" and "Connected" assuming network access is currently allowed.
4. Put the application into the background (e.g., press the home button). Wait a few seconds to let the firewall apply its background policy.
5. Bring the application back to the foreground.
6. Review the newly generated logs. You should see entries marked as "Background" during the time the app was hidden, and their connection status will reflect whether the firewall successfully blocked those background requests.
