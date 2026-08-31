plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.devtools.ksp")
  id("dagger.hilt.android")
  id("kotlin-parcelize")
}

android {
  namespace = "com.bolt.diy"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.bolt.diy"
    minSdk = 26
    targetSdk = 34
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions { jvmTarget = "17" }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
  // Core Android
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.11.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")

  // Compose BOM
  implementation(platform("androidx.compose:compose-bom:2024.01.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("androidx.activity:activity-compose:1.8.2")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

  // Navigation
  implementation("androidx.navigation:navigation-compose:2.7.6")

  // Hilt DI
  implementation("com.google.dagger:hilt-android:2.50")
  ksp("com.google.dagger:hilt-compiler:2.50")
  implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

  // Coroutines
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

  // Ktor Client (HTTP)
  implementation("io.ktor:ktor-client-android:2.3.5")
  implementation("io.ktor:ktor-client-core:2.3.5")
  implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
  implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
  implementation("io.ktor:ktor-client-logging:2.3.5")

  // OkHttp for Ktor
  implementation("com.squareup.okhttp3:okhttp:4.12.0")

  // JSON Serialization
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

  // DataStore (Preferences)
  implementation("androidx.datastore:datastore-preferences:1.0.0")

  // Room Database
  implementation("androidx.room:room-runtime:2.6.1")
  implementation("androidx.room:room-ktx:2.6.1")
  ksp("androidx.room:room-compiler:2.6.1")

  // Paging 3 for chat list
  implementation("androidx.paging:paging-runtime:3.2.1")
  implementation("androidx.paging:paging-compose:3.2.1")

  // Accompanist
  implementation("com.google.accompanist:accompanist-permissions:0.34.0")
  implementation("com.google.accompanist:accompanist-webview:0.34.0")

  // Markdown rendering
  implementation("io.github.kevinnkb:markdown-parser:1.0.12")
  implementation("io.github.qdsf596o:markdown-render:1.0.12")

  // Image loading
  implementation("io.coil-kt:coil-compose:2.5.0")

  // Splash screen
  implementation("androidx.core:core-splashscreen:1.0.1")

  // Work Manager for background tasks
  implementation("androidx.work:work-runtime-ktx:2.9.0")

  // Testing
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
}
