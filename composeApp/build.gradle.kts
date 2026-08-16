import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps: Properties? = if (keystorePropsFile.exists()) {
    Properties().apply { keystorePropsFile.inputStream().use { load(it) } }
} else {
    null
}

fun keystoreProperty(key: String): String? =
    keystoreProps?.getProperty(key) ?: System.getenv(key)

plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.playPublisher)
    id("com.android.application")
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(projects.shared)

            // Koin - using explicit versions to avoid deprecated platform()
            implementation("io.insert-koin:koin-core:4.2.0")
            implementation("io.insert-koin:koin-compose:4.2.0")
            implementation("io.insert-koin:koin-compose-viewmodel:4.2.0")

            // Lifecycle
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)

            // Voyager
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tab.navigator)
            implementation(libs.voyager.bottom.sheet.navigator)
            implementation(libs.voyager.koin)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Serialization
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            // Lifecycle for JVM/Desktop
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            // Koin for Desktop
            implementation("io.insert-koin:koin-core:4.2.0")
            implementation("io.insert-koin:koin-compose:4.2.0")
            // SQLDelight for Desktop
            implementation("app.cash.sqldelight:sqlite-driver:2.1.0")
            implementation("app.cash.sqldelight:jdbc-driver:2.1.0")
            // Coroutines Swing for Main dispatcher on desktop
            implementation(libs.kotlinx.coroutines.swing)
        }

        androidMain.dependencies {
            implementation("io.insert-koin:koin-android:4.2.0")
            implementation(libs.sqldelight.driver.android)
            implementation(libs.lifecycle.runtime.ktx)
            implementation(libs.google.play.app.update)
            implementation(libs.google.play.app.update.ktx)
        }
    }
}

android {
    namespace = "com.recipecostcalculator"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.recipecostcalculator"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()
    }

    signingConfigs {
        if (keystoreProps != null) {
            create("release") {
                storeFile = keystoreProperty("RELEASE_STORE_FILE")?.let { file(it) }
                storePassword = keystoreProperty("RELEASE_STORE_PASSWORD")
                keyAlias = keystoreProperty("RELEASE_KEY_ALIAS")
                keyPassword = keystoreProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystoreProps != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.recipecostcalculator.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.recipecostcalculator"
            packageVersion = "1.0.0"
        }
    }
}

sqldelight {
    databases {
        create("PizzeriaDatabase") {
            packageName = "com.recipecostcalculator.db"
        }
    }
}

val localPropsFile = rootProject.file("local.properties")
val playServiceAccount: String = if (localPropsFile.exists()) {
    val props = Properties()
    localPropsFile.inputStream().use { props.load(it) }
    props.getProperty("play.publisher.serviceAccountCredentials")
} else {
    null
} ?: error("play.publisher.serviceAccountCredentials must be set in local.properties")

play {
    serviceAccountCredentials = file(playServiceAccount)
}