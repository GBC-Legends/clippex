plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.clippex"
    compileSdk = 36
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = "com.example.clippex"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// --- Rust prebuild integration ---
// Builds Rust JNI libraries for Android ABIs before the APK build and places them under src/main/jniLibs
val rustDir = layout.projectDirectory.dir("src/main/rust")
val jniLibsDir = layout.projectDirectory.dir("src/main/jniLibs")

// ABIs to build; adjust as needed
val rustAbis = listOf("arm64-v8a", "armeabi-v7a", "x86_64")

val rustBuildTasks = rustAbis.map { abi ->
    tasks.register("cargoNdkBuildRelease_$abi", Exec::class) {
        group = "build"
        description = "Build Rust JNI library for $abi using cargo-ndk"
        workingDir = rustDir.asFile
        // Requires cargo-ndk to be installed: cargo install cargo-ndk
        commandLine(
            "cargo", "ndk",
            "-t", abi,
            "-o", jniLibsDir.asFile.absolutePath,
            "build", "--release"
        )
    }
}


// Aggregate task
tasks.register("cargoNdkBuildRelease") {
    group = "build"
    description = "Build Rust JNI libraries for all configured ABIs"
    dependsOn(rustBuildTasks)
}

// Ensure Rust is built before the Android build proceeds
tasks.named("preBuild") {
    dependsOn("cargoNdkBuildRelease")
}

// Clean task to remove generated JNI libraries
tasks.register("cleanRust") {
    group = "build"
    description = "Clean generated Rust JNI libraries under src/main/jniLibs"
    doLast {
        delete(jniLibsDir.asFile)
    }
}

tasks.named("clean") {
    dependsOn("cleanRust")
}