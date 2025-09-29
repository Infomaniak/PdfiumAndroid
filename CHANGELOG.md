## What's new in 1.9.9
* Update project
* Force the use of libc++_shared.so from the NDK instead of the local copy

## What's new in 1.9.8
* Update project
* Update libraries to 16kb pages

## What's new in 1.9.6
 * Edit jitpack.yml file to fix a Jitpack CI issue

## What's new in 1.9.5
 * Upgrade build tools to 8.1.3
 * Upgrade gradle to 8.0
 * Delete package from AndroidManifest.xml and add namespace to build.gradle
 * Upgrade androidx.collection to 1.3.0

## What's new in 1.9.4
 * Jumping from 1.9.2 to 1.9.4 because jitpack has an issue to refresh an existing artifact with the same name
 * Bump APP_PLATFORM from 14 to 21
 * Functionally the same as 1.9.2
 * Fix an issue when generating aar library
 * Add the possibility to generate local aar library using Maven Local (./gradlew publishToMavenLocal)
