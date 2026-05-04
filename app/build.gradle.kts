import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

val secureLocalProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun secureConfig(name: String, defaultValue: String): String =
    providers.environmentVariable(name).orNull
        ?: providers.gradleProperty(name).orNull
        ?: secureLocalProperties.getProperty(name)
        ?: defaultValue

fun buildConfigString(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.erivaldogelson.remedios"
    compileSdk = 36
    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "com.erivaldogelson.remedios"
        minSdk = 26
        targetSdk = 36
        versionCode = 16
        versionName = "0.1.15"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField(
                "String",
                "API_BASE_URL",
                buildConfigString(secureConfig("RELEASE_API_BASE_URL", "https://api.example.com/")),
            )
            buildConfigField(
                "String",
                "API_HOST",
                buildConfigString(secureConfig("RELEASE_API_HOST", "api.example.com")),
            )
            buildConfigField(
                "String",
                "CERTIFICATE_PINS",
                buildConfigString(secureConfig("RELEASE_CERTIFICATE_PINS", "")),
            )
            buildConfigField(
                "String",
                "EXPECTED_SIGNING_CERT_SHA256",
                buildConfigString(secureConfig("RELEASE_SIGNING_CERT_SHA256", "")),
            )
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField(
                "String",
                "API_BASE_URL",
                buildConfigString(secureConfig("DEBUG_API_BASE_URL", "https://debug-api.example.com/")),
            )
            buildConfigField(
                "String",
                "API_HOST",
                buildConfigString(secureConfig("DEBUG_API_HOST", "debug-api.example.com")),
            )
            buildConfigField(
                "String",
                "CERTIFICATE_PINS",
                buildConfigString(secureConfig("DEBUG_CERTIFICATE_PINS", "")),
            )
            buildConfigField(
                "String",
                "EXPECTED_SIGNING_CERT_SHA256",
                buildConfigString(secureConfig("DEBUG_SIGNING_CERT_SHA256", "")),
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.mlkit.text.recognition)
    implementation(libs.coil.compose)
    implementation(libs.google.material)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    debugImplementation(libs.androidx.compose.ui.tooling)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}
