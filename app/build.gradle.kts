plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "pt.gaya.nutriai"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "pt.gaya.nutriai"
        minSdk = 26
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
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Dependências do CameraX para controlo do hardware (Sintaxe Otimizada)
    val cameraxVersion = "1.3.3"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion") // Mantém o camera-camera2, mas usamos aspas duplas limpas
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Dependência para gerir permissões de forma simples no Compose (Versão Estabilizada)
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")

    // Retrofit para chamadas HTTP API
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Conversor automático de JSON para Objetos Kotlin (Gson)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // Extensão para conseguir usar o "viewModel()" dentro dos ecrãs em Jetpack Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
}