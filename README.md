# Matee ruleset

KtLint rules for Kotlin and Compose. See [Rules](RULES.md) for descriptions and examples.

## Gradle

Requires KtLint 1.8.0.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven("https://mateedevs.github.io/matee-ruleset")
    }
}
```

```kotlin
// build.gradle.kts
dependencies {
    ktlintRuleset("cz.matee:matee-ruleset:0.1.0")
}
```

```editorconfig
[*.{kt,kts}]
ktlint_matee = enabled
ktlint_standard_multiline-expression-wrapping = disabled
ktlint_standard_chain-method-continuation = disabled
```

## IDE

Download the JAR from [Releases](https://github.com/MateeDevs/matee-ruleset/releases) and add it in **Settings > Tools > KtLint > Custom ruleset JARs**.

## Build

JDK 21:

```sh
./gradlew jar ktlintCheck
```

Push a `v<version>` tag matching `gradle/libs.versions.toml` to publish.
