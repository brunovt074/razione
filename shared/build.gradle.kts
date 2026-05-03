plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvm()

    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // SQLDelight
            implementation(platform(libs.koin.bom))
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Lifecycle (for ViewModel)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.runtime.ktx)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
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