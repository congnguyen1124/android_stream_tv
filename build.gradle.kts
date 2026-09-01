import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import com.diffplug.spotless.LineEnding
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.hilt) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.detekt) apply false
}

subprojects.filter { it.buildFile.exists() }.forEach { subproject ->
  subproject.apply<io.gitlab.arturbosch.detekt.DetektPlugin>()
  subproject.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
    parallel = true
    source.from(files("src/"))
    config.from(files("${subproject.rootDir}/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = true
  }
  subproject.dependencies {
    "detektPlugins"(rootProject.libs.compose.rules.detekt)
  }
  // The Gradle daemon runs on a JDK newer than detekt 1.23.x can parse as a --jvm-target,
  // so pin it to the same bytecode level the app compiles against.
  subproject.tasks.withType<Detekt>().configureEach {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }
  subproject.tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }
}

listOf(rootProject).plus(subprojects.filter { it.buildFile.exists() }).forEach { projectToFormat ->
  projectToFormat.apply<SpotlessPlugin>()
  projectToFormat.configure<SpotlessExtension> {
    val ktlintVersion = rootProject.libs.versions.ktlint.get()
    lineEndings = LineEnding.UNIX

    if (projectToFormat == rootProject) {
      kotlinGradle {
        target("*.gradle.kts")
        targetExclude("**/build/**/*.kts")

        ktlint(ktlintVersion)
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
      }
    } else {
      kotlin {
        target("src/**/*.kt")
        targetExclude(
          "**/Res.kt", // Compose Multiplatform Res class
          "**/build/**/*.kt", // Kotlin generated files
        )

        ktlint(ktlintVersion)
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
      }

      format("xml") {
        target("src/**/res/**/*.xml")
        targetExclude("**/build/**/*.xml")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
      }

      kotlinGradle {
        target("*.gradle.kts")
        targetExclude("**/build/**/*.kts")

        ktlint(ktlintVersion)
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
      }
    }
  }
}
