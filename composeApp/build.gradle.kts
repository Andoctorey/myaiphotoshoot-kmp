import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.serialization)
    alias(libs.plugins.versions)
}

kotlin {
    android {
        namespace = "ai.create.photo.shared"
        compileSdk = 36
        minSdk = 26
        androidResources {
            enable = true
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-P",
                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=ai.create.photo.platform.Parcelize"
            )
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            freeCompilerArgs += listOf("-Xbinary=bundleId=com.myaiphotoshoot")
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = project.name + "-wasm"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    // Serve sources to debug inside browser.
                    static(rootDirPath)
                    static(projectDirPath)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.multidex)
            implementation(libs.ktor.client.cio)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.crashlytics.ktx)
            implementation(libs.billing.ktx)

            // third-party
            implementation(libs.kermit.crashlytics)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)

            // third-party
            implementation(libs.kermit.crashlytics)
        }

        // ./gradlew kotlinUpgradeYarnLock
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.cio)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive.navigation.suite)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.components.ui.tooling.preview)
            implementation(libs.adaptive)
            implementation(libs.adaptive.layout)
            implementation(libs.adaptive.navigation)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.navigation.compose)

            implementation(libs.material.navigation)

            implementation(libs.supabase.postgrest.kt)
            implementation(libs.supabase.auth.kt)
            implementation(libs.supabase.realtime.kt)
            implementation(libs.supabase.storage.kt)
            implementation(libs.supabase.functions.kt)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // third-party
            implementation(libs.filekit.compose)
            implementation(libs.kermit)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

compose.desktop {
    application {
        mainClass = "ai.create.photo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ai.create.photo"
            packageVersion = "1.0.0"
        }
    }
}

fun String.isNonStable(): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.isNonStable() && !currentVersion.isNonStable()
    }

    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}

val updateHtmlTimestamp by tasks.registering {
    val indexHtml = file("src/wasmJsMain/resources/index.html")
    inputs.file(indexHtml)
    outputs.file(indexHtml)
    doLast {
        val versionCode = project.readGitVersionCodeOrDefault()
        val originalContent = indexHtml.readText()
        val updatedContent = originalContent
            .replace(Regex("""composeApp\.js\?v=\d+"""), "composeApp.js?v=$versionCode")

        if (updatedContent != originalContent) {
            indexHtml.writeText(updatedContent)
        }
    }
}

tasks.named("wasmJsProcessResources").configure {
    dependsOn(updateHtmlTimestamp)
}

fun Project.readGitVersionCodeOrDefault(defaultValue: String = "0"): String =
    runCatching {
        providers.exec {
            commandLine("git", "rev-list", "--all", "--count", "--full-history", "--no-max-parents", "HEAD")
        }.standardOutput.asText.get().trim().ifBlank { defaultValue }
    }.getOrDefault(defaultValue)
