# Contributing to JuggleIM Android SDK

Thank you for your interest in JuggleIM Android SDK. Issues and pull requests are welcome.

## Before You Start

- Search existing issues and pull requests before opening a new one.
- For integration questions, include the SDK version, Android version, device model, and the module you are using.
- Do not include production `appKey`, user tokens, push credentials, private server URLs, keystore files, or customer data in issues or pull requests.

## Development Setup

1. Install Android Studio.
2. Use JDK 17.
3. Open the repository root in Android Studio.
4. Sync Gradle and run the `demo` or `app` module.
5. Configure your own JuggleIM app key, server URL, push vendor credentials, and call vendor credentials when testing vendor-specific features.

## Pull Request Guidelines

- Keep pull requests focused on one change.
- Explain the problem, the solution, and the test coverage.
- Update `README.md` and `README.en.md` when behavior, dependencies, or integration steps change.
- Avoid committing generated build outputs, APKs, local IDE files, credentials, or logs.
- For bug fixes, include a reproduction description or a regression test when practical.

## Commit Message Style

Prefer concise conventional-style messages:

- `fix: handle reconnect after token refresh`
- `feat: add push plugin option`
- `docs: update Android quick start`
- `test: cover message decrypt failure`

## Reporting Bugs

Please include:

- SDK version
- Android version and device model
- Module or plugin involved
- Steps to reproduce
- Expected behavior
- Actual behavior
- Relevant logs with secrets removed

## Requesting Features

Please describe:

- The product scenario
- The API or behavior you expect
- Whether the feature should belong to the core SDK, UI Kit, push plugin, or call plugin
- Any compatibility requirements
