pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "stream_tv"
include(":app")

// The player engine lives in its own repository so it can be reused by other apps. A composite build
// substitutes the local checkout for the `com.congnguyencn:stream-player` coordinate, so no publish
// step is needed while developing both side by side.
// A clone without the sibling checkout must instead run `./gradlew :stream-player:publishToMavenLocal`
// in stream_player and add `mavenLocal()` above — see docs/player-integration/player-integration.md.
val streamPlayerDir = file("../stream_player")
require(streamPlayerDir.isDirectory) {
  "Expected the stream_player project at ${streamPlayerDir.absolutePath}. " +
    "Clone it next to this repository, or switch to the mavenLocal route documented in " +
    "docs/player-integration/player-integration.md."
}
includeBuild(streamPlayerDir)
