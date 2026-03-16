plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.carlos.autoflow.platform"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
        manifestPlaceholders["JPUSH_APPKEY"] = "8875cd8a215618b05a8e9640"
        manifestPlaceholders["JPUSH_CHANNEL"] = "developer-default"
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "ENABLE_PLATFORM_LOGGING", "true")
            buildConfigField("String", "UMENG_APP_KEY", "\"69b3c7349a7f3764888fd4e4\"")
            buildConfigField("String", "UMENG_CHANNEL", "\"developer-default\"")
        }
        release {
            buildConfigField("boolean", "ENABLE_PLATFORM_LOGGING", "false")
            buildConfigField("String", "UMENG_APP_KEY", "\"69b3c7349a7f3764888fd4e4\"")
            buildConfigField("String", "UMENG_CHANNEL", "\"developer-default\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(project(":foundation"))

    implementation("com.umeng.umsdk:common:9.8.9")
    implementation("com.umeng.umsdk:asms:1.8.7.2")
    implementation("com.umeng.umsdk:apm:2.0.6")
    implementation("com.umeng.umsdk:union:3.1.0")

    implementation("cn.jiguang.sdk:jpush:5.9.2")
}
