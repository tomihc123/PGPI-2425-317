plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.villactiva"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.villactiva"
        minSdk = 30
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.google.android.material:material:1.9.0")
    testImplementation("org.mockito:mockito-core:4.6.1") // Mockito para crear mocks
    testImplementation("org.mockito:mockito-inline:4.6.1") // Mockito inline para mocks avanzados
    testImplementation("org.robolectric:robolectric:4.9") // Robolectric para pruebas con simulación de Android
    testImplementation("androidx.test.ext:junit:1.1.5") // JUnit para pruebas unitarias en Android
    // Dependencias para pruebas instrumentadas
    androidTestImplementation("androidx.test.ext:junit:1.2.1") // JUnit para pruebas instrumentadas
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1") // Espresso para pruebas de UI
    androidTestImplementation("androidx.test:core-ktx:1.6.1") // Core para pruebas con AndroidX


}