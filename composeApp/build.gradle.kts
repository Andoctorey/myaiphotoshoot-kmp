import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

val gitVersionCode =
    "git rev-list --all --count --full-history --no-max-parents HEAD".runCommand().toInt()

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
    androidLibrary {
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
            val versionCode = gitVersionCode
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js?v=$versionCode"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
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

        // https://www.jetbrains.com/help/kotlin-multiplatform-dev/whats-new-compose-170.html#across-platforms
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3)
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
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
    androidRuntimeClasspath(compose.uiTooling)
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
    val versionCode = gitVersionCode

    doFirst {
        val updatedContent = indexHtml.readText()
            .replace(Regex("""composeApp\.js\?v=\d+"""), "composeApp.js?v=$versionCode")
        indexHtml.writeText(updatedContent)
    }
}

tasks.named("wasmJsProcessResources") {
    dependsOn(updateHtmlTimestamp)
}

fun String.runCommand(workingDir: File = file("./")): String {
    val parts = this.split("\\s".toRegex())
    val process = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    process.waitFor(1, TimeUnit.MINUTES)
    return process.inputStream.bufferedReader().readText().trim()
}
