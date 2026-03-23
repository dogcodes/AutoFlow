import java.text.SimpleDateFormat
import java.util.Date
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
        versionName = "0.1.0"
        manifestPlaceholders["JPUSH_APPKEY"] = "8875cd8a215618b05a8e9640"
        manifestPlaceholders["JPUSH_CHANNEL"] = "developer-default"
        manifestPlaceholders["UMENG_CHANNEL"] = "default"

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

    flavorDimensions.add("market")

    productFlavors {
        create("google") {
            dimension = "market"
            manifestPlaceholders["UMENG_CHANNEL"] = "google"
        }
        create("xiaomi") {
            dimension = "market"
            manifestPlaceholders["UMENG_CHANNEL"] = "xiaomi"
        }
        create("huawei") {
            dimension = "market"
            manifestPlaceholders["UMENG_CHANNEL"] = "huawei"
        }
        create("vivo") {
            dimension = "market"
            manifestPlaceholders["UMENG_CHANNEL"] = "vivo"
        }
        create("oppo") {
            dimension = "market"
            manifestPlaceholders["UMENG_CHANNEL"] = "oppo"
        }
        create("samsung") {
            dimension = "market"
            manifestPlaceholders["UMENG_CHANNEL"] = "samsung"
        }
        create("appchina") {
            dimension = "market"
            manifestPlaceholders["UMENG_CHANNEL"] = "appchina"
        }
        create("default") {
            dimension = "market"
            manifestPlaceholders["UMENG_CHANNEL"] = "default"
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
            isMinifyEnabled = true
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

    applicationVariants.configureEach {
        val baseName = "AutoFlow"
        val resolvedVersionName = versionName ?: "0.0.0"
        val dateValue = SimpleDateFormat("yyyyMMdd").format(Date())
        val channel = if (productFlavors.isEmpty()) "default" else productFlavors.joinToString("-") { it.name }
        val buildTypeName = buildType.name
        val normalizedVersion = resolvedVersionName.replace('.', '_')

        outputs.configureEach {
            if (this is com.android.build.gradle.api.ApkVariantOutput) {
                outputFileName = "${baseName}_v${normalizedVersion}_${dateValue}_${channel}_${buildTypeName}.apk"
            }
        }
    }
}

dependencies {
    implementation(project(":license"))
    implementation(project(":platform"))
    implementation(project(":foundation"))
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
