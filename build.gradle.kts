plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

group = "cz.matee"
version = libs.versions.ruleset.get()

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

dependencies {
    compileOnly(libs.ktlint.engineCore)
    compileOnly(libs.ktlint.rulesetCore)
    add(
        "ktlintRuleset",
        files(tasks.jar),
    )
    add(
        "ktlintRuleset",
        libs.ktlint.composeRules,
    )
}

ktlint {
    version.set(libs.versions.ktlint)
}
