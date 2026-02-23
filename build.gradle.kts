// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.4.1")
        android.set(true)
        ignoreFailures.set(false)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.SARIF)
        }
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        baseline = project.file("config/detekt/baseline.xml")
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
        source.setFrom(
            "src/main/java",
            "src/main/kotlin",
            "src/test/java",
            "src/test/kotlin"
        )
    }

    dependencies {
        "detektPlugins"("io.nlopez.compose.rules:detekt:0.4.14")
    }
}

tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt in all submodules"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("detekt") })
}