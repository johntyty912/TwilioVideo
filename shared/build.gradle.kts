import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

// Function to read .env.local file
fun readEnvFile(): Properties {
    val props = Properties()
    val envFile = File(rootDir, ".env.local")
    if (envFile.exists()) {
        FileInputStream(envFile).use { fis ->
            props.load(fis)
        }
    }
    return props
}

// Read environment variables
val envProps = readEnvFile()

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    jvm()
    
    val iosX64 = iosX64()
    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()
    
    listOf(
        iosX64,
        iosArm64,
        iosSimulatorArm64
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines for Flow and async operations
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                // Serialization for data models
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                // DateTime handling
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                // HTTP client for API calls
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
        
        val androidMain by getting {
            dependencies {
                // Twilio Video Android SDK
                implementation("com.twilio:video-android:7.6.0")
                // Android-specific coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
                // AndroidX libraries for permissions and lifecycle
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
                // Android-specific HTTP client engine (platform implementation)
                implementation("io.ktor:ktor-client-android:2.3.7")
                // Android-specific logging (optional)
                implementation("io.ktor:ktor-client-logging:2.3.7")
            }
        }
        
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                // iOS-specific coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
                implementation("org.mockito:mockito-core:5.7.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
    }
}

android {
    namespace = "com.johnlai.twiliovideo.shared"
    compileSdk = 34

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    defaultConfig {
        minSdk = 24
        
        // Inject environment variables into BuildConfig
        buildConfigField("String", "TWILIO_TOKEN_URL", "\"${envProps.getProperty("TWILIO_TOKEN_URL", "https://your-api-endpoint.com/twilio/video_token")}\"")
        buildConfigField("String", "TEST_USER_IDENTITY", "\"${envProps.getProperty("TEST_USER_IDENTITY", "user")}\"")
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
}
