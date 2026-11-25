# Shellforge

Shellforge is an Android Linux development environment built with Kotlin. It aims to provide an Ubuntu ARM64 userspace with a terminal, package management, Git, GitHub CLI, and developer tools.

## Overview

- Native Android application written in Kotlin
- Ubuntu ARM64 userspace planned for execution through PRoot
- Terminal interface and process/session management
- Shared-storage access through Android's supported permission and file-access APIs
- Developer tooling such as `apt`, `git`, and GitHub CLI (`gh`) inside the Linux environment

## Project structure

The Android application lives in `app/`. Linux userspace and native runtime integration will be kept separate from the UI layer as those components are introduced.

## License

License has not been selected yet.
