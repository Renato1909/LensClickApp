import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val stagingApiBaseUrl = providers.gradleProperty("lensclick.stagingApiBaseUrl")
    .orElse(providers.environmentVariable("LENSCLICK_STAGING_API_BASE_URL"))
    .orElse("https://staging.invalid")
    .get()
    .trim()

require(stagingApiBaseUrl.startsWith("https://")) {
    "The staging API base URL must use HTTPS."
}

val releaseKeystorePath = providers.gradleProperty("lensclick.releaseKeystorePath")
    .orElse(providers.environmentVariable("LENSCLICK_RELEASE_KEYSTORE_PATH"))
    .orNull
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
val releaseKeystoreBase64 = providers.gradleProperty("lensclick.releaseKeystoreBase64")
    .orElse(providers.environmentVariable("LENSCLICK_RELEASE_KEYSTORE_BASE64"))
    .orNull
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
val releaseStorePassword = providers.gradleProperty("lensclick.releaseStorePassword")
    .orElse(providers.environmentVariable("LENSCLICK_RELEASE_STORE_PASSWORD"))
    .orNull
val releaseKeyAlias = providers.gradleProperty("lensclick.releaseKeyAlias")
    .orElse(providers.environmentVariable("LENSCLICK_RELEASE_KEY_ALIAS"))
    .orNull
val releaseKeyPassword = providers.gradleProperty("lensclick.releaseKeyPassword")
    .orElse(providers.environmentVariable("LENSCLICK_RELEASE_KEY_PASSWORD"))
    .orNull

val decodedReleaseKeystore = if (releaseKeystorePath == null && releaseKeystoreBase64 != null) {
    layout.buildDirectory.file("signing/release.keystore").get().asFile.apply {
        parentFile.mkdirs()
        writeBytes(Base64.getDecoder().decode(releaseKeystoreBase64))
    }
} else {
    null
}

val releaseKeystoreFile = releaseKeystorePath?.let { file(it) } ?: decodedReleaseKeystore
val hasReleaseSigning = releaseKeystoreFile != null &&
    !releaseStorePassword.isNullOrEmpty() &&
    !releaseKeyAlias.isNullOrEmpty() &&
    !releaseKeyPassword.isNullOrEmpty()

android {
    namespace = "com.lensclick.app"

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.lensclick.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8787\"")
            buildConfigField("String", "API_ENVIRONMENT", "\"debug\"")
        }
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "API_BASE_URL", "\"$stagingApiBaseUrl\"")
            buildConfigField("String", "API_ENVIRONMENT", "\"staging\"")
        }
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"https://lensclick.renato-aparecido-business.workers.dev\""
            )
            buildConfigField("String", "API_ENVIRONMENT", "\"release\"")
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
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("org.json:json:20240303")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
