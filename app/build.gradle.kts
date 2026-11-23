/*
 * app — aplicativo Android HP 12C Mobile.
 *
 * Arquitetura:
 *  - Kotlin + XML Views (AppCompat + Material Components)
 *  - MVVM: CalculatorViewModel expõe LiveData<CalcUiState> observado pela UI
 *  - engine (módulo :engine) fornece o motor financeiro puro-Java
 *  - Persistência em SharedPreferences (serialização JSON do Stack/
 *    FinanceMemory/GeneralMemory/Configuration)
 *  - Orientation: sensorLandscape (preferencial) + suporte a portrait
 *  - Min SDK 24 (Android 7.0+), Target SDK 34
 *
 * Nota: optamos por XML Views em vez de Jetpack Compose para reduzir o
 * tamanho das dependências e tornar o build viável em ambientes com
 * restrição de memória. A UI ainda é fiel à HP 12C física.
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.hp12c.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hp12c.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            // Keystore de debug assina também a release para entrega do APK.
            signingConfig = signingConfigs.getByName("debug")
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        // Não usamos ViewBinding nem Compose — UI em XML Views tradicional.
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // Motor financeiro (Java puro)
    implementation(project(":engine"))

    // AndroidX — conjunto MÍNIMO.
    // Não usamos mais lifecycle/viewmodel/activity-ktx/datastore para evitar
    // bugs de inicialização via ContentProvider em ROMs customizadas.
    implementation("androidx.core:core-ktx:1.13.1")

    // Testes instrumentados
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Testes de unidade (JVM)
    testImplementation("junit:junit:4.13.2")
    testImplementation(project(":engine"))
}
