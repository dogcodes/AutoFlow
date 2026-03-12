import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    val signingProperties = Properties()
    val defaultPropertiesFile = rootProject.file("buildsystem/default.properties")
    if (defaultPropertiesFile.exists()) {
        defaultPropertiesFile.inputStream().use { signingProperties.load(it) }
    }
    val keystorePropertiesFile = rootProject.file("buildsystem/keystore.properties")
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { signingProperties.load(it) }
    }

    val hasSigningProperties = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
        .all { signingProperties.getProperty(it)?.isNotBlank() == true }

    namespace = "com.carlos.autoflow"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.carlos.autoflow"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasSigningProperties) {
            create("release") {
                storeFile = rootProject.file(signingProperties["storeFile"] as String)
                storePassword = signingProperties["storePassword"] as String
                keyAlias = signingProperties["keyAlias"] as String
                keyPassword = signingProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            if (hasSigningProperties) {
                signingConfig = signingConfigs.getByName("release")
            }
            buildConfigField("boolean", "FORCE_PREMIUM", "false")
        }
        release {
            if (hasSigningProperties) {
                signingConfig = signingConfigs.getByName("release")
            }
            buildConfigField("boolean", "FORCE_PREMIUM", "false")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":license"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // 第二阶段新增依赖
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
