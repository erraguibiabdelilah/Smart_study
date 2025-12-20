plugins {
    // Le plugin "application" doit être appliqué directement dans le module app
    alias(libs.plugins.android.application)
    // Ajout du plugin Kotlin, indispensable pour un projet Android moderne
    alias(libs.plugins.kotlin.android)
}
// Configuration des tâches de compilation Kotlin (manière moderne)
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

android {
    namespace = "com.example.smart_study"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smart_study"
        minSdk = 26 // Azure nécessite au moins API 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Le bloc kotlinOptions a été déplacé à l'extérieur (voir en haut)

    packaging {
        resources {
            // Correction de la syntaxe : "excludes.add" est incorrect, il faut utiliser "exclude"
            exclude("META-INF/{AL2.0,LGPL2.1}")
            exclude("META-INF/LICENSE*")
            exclude("META-INF/NOTICE*")
            exclude("META-INF/INDEX.LIST")
            exclude("META-INF/DEPENDENCIES")
            exclude("META-INF/io.netty.versions.properties")
            exclude("**/*.kotlin_module")
        }
    }
}

dependencies {
    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    
    // Gson pour converters
    implementation("com.google.code.gson:gson:2.10.1")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.0")



    implementation("org.json:json:20230227")

    // Azure AI
    implementation("com.azure:azure-ai-inference:1.0.0-beta.1")
    implementation("com.azure:azure-core-http-okhttp:1.11.8")

    // La dépendance OkHttp est retirée car elle est déjà incluse par azure-core-http-okhttp
    // implementation("com.squareup.okhttp3:okhttp:4.11.0")

    //pour l'extraction de text :
    implementation("com.itextpdf:itextg:5.5.10")
}
