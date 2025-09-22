# Globe Android

A composable providing a 3D globe, where you can place markers. Use the compose [animations API](https://developer.android.com/develop/ui/compose/animation/choose-api) to create smooth and performant interactions.

> **🚧: This library is still work in-progress, API may still change at any point.**

The globe asset files are created using [globe-mesh-builder](https://github.com/mullvad/globe-mesh-builder), which in-turn consumes that from [Natural Earth](https://www.naturalearthdata.com/)

## Examples

You find examples in the [`GlobePreview.kt`](./globe/src/main/java/net/mullvad/mullvadvpn/globe/GlobePreview.kt) or by launching the sample app.

## Usage

1. Add [jitpack](https://jitpack.io/) as a repository.
```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = URI("https://jitpack.io") }
    }
}
```

2. Add globe-android as a dependency.
```
    implementation("com.github.mullvad:globe-android:main-SNAPSHOT")
```
