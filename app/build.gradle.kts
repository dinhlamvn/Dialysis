plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.dialysis.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.dialysis.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 2
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("keys/keys.jks")
            storePassword = "123123@Dialysis"
            keyAlias = "key0"
            keyPassword = "123123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    flavorDimensions("default")
    productFlavors {
        create("dev") {
            buildConfigField("String", "SERVER_URL", "\"https://dialysis-intake.io.vn/\"")
        }

        create("prod") {
            buildConfigField("String", "SERVER_URL", "\"https://dialysis-intake-app.io.vn/\"")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.compose.foundation)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter)
    implementation(libs.http.logging)
    implementation(libs.koin.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
