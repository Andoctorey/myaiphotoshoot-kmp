import java.io.FileInputStream
import java.util.Properties
import java.util.concurrent.TimeUnit
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val gitVersionCode =
    "git rev-list --all --count --full-history --no-max-parents HEAD".runCommand().toInt()

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.perf)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = "ai.create.photo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.myaiphotoshoot"
        minSdk = 26
        targetSdk = 36
        versionCode = gitVersionCode
        versionName = "1.4.$gitVersionCode"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    val keystorePropertiesFile = rootProject.file("signing.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
            versionNameSuffix = "-DEV"
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            isJniDebuggable = false
            manifestPlaceholders["enableCrashReporting"] = "false"
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            manifestPlaceholders["enableCrashReporting"] = "true"
            proguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(compose.foundation)
    implementation(compose.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.filekit.compose)
    implementation(project.dependencies.platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.perf)

    debugImplementation(compose.uiTooling)
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
