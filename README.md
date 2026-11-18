# Mate ruleset

Reusable Matee KtLint rules for Kotlin call layout and Compose UI layout. The custom ruleset ID is `mate`; rule IDs are `mate:call-layout` and `mate:compose-layout`.

## Build

Use JDK 21 and the included Gradle wrapper:

```sh
./gradlew jar
./gradlew ktlintCheck
```

The output is `build/libs/mate-ruleset-0.1.0.jar`. The JAR contains only the custom rules and their ServiceLoader registration. The consumer supplies KtLint 1.8.0. Standard and Compose Rules 0.6.8 are separate rulesets; they are not bundled in this JAR. Versions are maintained in `gradle/libs.versions.toml`.

## Gradle integration

Copy the versioned JAR into the consuming repository and add it to its KtLint Gradle configuration:

```kotlin
dependencies {
    ktlintRuleset(files("tools/ktlint/mate-ruleset-0.1.0.jar"))
}
```

Enable the ruleset in the consumer's `.editorconfig`:

```editorconfig
[*.{kt,kts}]
ktlint_mate = enabled
ktlint_standard_multiline-expression-wrapping = disabled
ktlint_standard_chain-method-continuation = disabled
ij_kotlin_allow_trailing_comma_on_call_site = true
```

The `.editorconfig` in this project provides the complete compatible configuration. The host project's rule-loading instructions remain responsible for telling agents when to load Compose guidance.

## IntelliJ / Android Studio

Add `build/libs/mate-ruleset-0.1.0.jar` under Settings > Tools > KtLint > Custom/External ruleset JARs. Select KtLint 1.8.0 and load Compose Rules separately for the same checks as Gradle.

## Contract

- In Kotlin and Kotlin scripts, calls with at least two parenthesized arguments are multiline, including nested calls, constructors, and collection factories. Arguments have trailing commas. A trailing lambda is not counted as a parenthesized argument.
- Simple single-argument calls are compact when they fit, except standalone UI calls in multiline Compose blocks, which use multiline argument lists. A tiny slot such as `label = { Text(text = label) }` can stay inline. Comments and multiline argument expressions prevent compaction.
- Short comparisons and `if` conditions stay together, as do a named argument and its short value. A multiline `if` used as a `when` branch starts below the arrow.
- Inside an annotated `@Composable` function or explicitly annotated composable lambda, method chains have one call per line. Annotation import aliases are supported. A single call on `Modifier` or `modifier` also starts below the receiver. Property-only qualification is preserved. An ordinary function beside the composable, or a nested ordinary function, keeps standard chain formatting.
- Within those functions, a blank line separates adjacent multiline UI calls and state/UI pairs. UI calls are identified syntactically by an uppercase callee, matching Kotlin composable naming conventions. Comment-separated statements retain their grouping.
- Named `modifier` arguments come last inside UI call parentheses. This is reported without an autofix because reordering argument evaluation can change behavior.
- Compose Rules checks layout modifiers at every visibility, including private helpers. Adding missing parameters and forwarding them remains an API correction, not a whitespace autofix.

The rules work on Kotlin syntax, not compiler type resolution. They cannot infer an arbitrary variable's Modifier type or whether an external lowercase function emits UI. Use the normal `Modifier`/`modifier` names, `@Composable` annotations, and uppercase UI function names. Logical spacing beyond the structural checks remains part of review.
