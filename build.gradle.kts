plugins {
    `maven-publish`
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

publishing {
    publications {
        create<MavenPublication>("ruleset") {
            artifact(tasks.jar)
            pom {
                name.set("Matee ruleset")
                description.set("KtLint rules for Kotlin and Compose")
                url.set("https://github.com/MateeDevs/matee-ruleset")
            }
        }
    }
    repositories {
        maven {
            name = "Pages"
            url = layout.buildDirectory.dir("maven-repository").get().asFile.toURI()
        }
    }
}
