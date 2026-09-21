import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

val releaseVersionProperties = Properties().apply {
  val versionFile = rootProject.file("release-version.properties")
  if (versionFile.isFile) {
    versionFile.inputStream().use { load(it) }
  }
}

android {
  namespace = "com.remmi.browser"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.remmi.browser"
    minSdk = 26
    targetSdk = 36
    
    val configuredVersionCode = providers.gradleProperty("versionCode")
      .map { it.toInt() }
      .orElse(
        releaseVersionProperties.getProperty("versionCode", "1").toInt()
      )

    val configuredVersionName = providers.gradleProperty("versionName")
      .orElse(
        releaseVersionProperties.getProperty("versionName", "1.0.0")
      )

    versionCode = configuredVersionCode.get()
    versionName = configuredVersionName.get()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    ndk {
      abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
    }
  }

  val releaseTaskRequested = gradle.startParameter.taskNames.any { task ->
    !task.startsWith("-") && (task.contains("assembleRelease", ignoreCase = true) ||
     task.contains("bundleRelease", ignoreCase = true) ||
     task.contains("packageRelease", ignoreCase = true) ||
     task.contains("publishRelease", ignoreCase = true) ||
     (task.endsWith("Release", ignoreCase = true) && !task.contains("lint", ignoreCase = true) && !task.contains("test", ignoreCase = true)))
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH")
      val storePassword = System.getenv("STORE_PASSWORD")
      val keyAlias = System.getenv("KEY_ALIAS")
      val keyPassword = System.getenv("KEY_PASSWORD")

      val releaseRequested = gradle.startParameter.taskNames.any {
        it.contains("Release", ignoreCase = true) &&
        !it.contains("lint", ignoreCase = true) &&
        !it.contains("test", ignoreCase = true)
      }

      if (releaseRequested) {
        require(!keystorePath.isNullOrBlank()) {
          "KEYSTORE_PATH is required for release builds"
        }
        require(!storePassword.isNullOrBlank()) {
          "STORE_PASSWORD is required for release builds"
        }
        require(!keyAlias.isNullOrBlank()) {
          "KEY_ALIAS is required for release builds"
        }
        require(!keyPassword.isNullOrBlank()) {
          "KEY_PASSWORD is required for release builds"
        }
        val ksFile = file(keystorePath)
        require(ksFile.isFile) {
          "Release keystore does not exist: $keystorePath"
        }
        storeFile = ksFile
        this.storePassword = storePassword
        this.keyAlias = keyAlias
        this.keyPassword = keyPassword
      }
    }
  }

  buildTypes {
    debug {
      // Use AGP's standard debug signing.
    }
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
  }

  val isBuildingBundle = gradle.startParameter.taskNames.any { it.contains("bundle", ignoreCase = true) }
  splits {
    abi {
      isEnable = releaseTaskRequested && !isBuildingBundle
      reset()
      include("arm64-v8a", "armeabi-v7a", "x86_64")
      isUniversalApk = true
    }
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    jniLibs {
      useLegacyPackaging = true
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlin {
    compilerOptions {
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      all {
        it.maxHeapSize = "2g"
        it.systemProperty("robolectric.dependency.repo.url", "https://repo1.maven.org/maven2")
        it.systemProperty("robolectric.dependency.repo.id", "Central")
        it.systemProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        it.systemProperty("java.awt.headless", "true")
        it.testLogging {
          events("passed", "skipped", "failed", "standardError")
          showExceptions = true
          showCauses = true
          showStackTraces = true
        }
      }
    }
  }
  lint {
    checkReleaseBuilds = true
    abortOnError = true
    disable += listOf("InvalidFragmentVersionForActivityResult", "UnsafeOptInUsageError")
  }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

configurations.all {
  resolutionStrategy.dependencySubstitution {
    substitute(module("org.mozilla.geckoview:geckoview:156.0.20260909172920"))
      .using(module("org.mozilla.geckoview:geckoview-omni:156.0.20260909172920"))
  }
}

// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.fragment.ktx)
  // implementation(libs.accompanist.permissions)
  // GeckoView & Tor dependencies (Pinned deterministic versions)
  implementation("org.mozilla.geckoview:geckoview-omni:156.0.20260909172920")
  implementation("info.guardianproject:tor-android:0.4.9.12")
  implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
  implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.animation:animation")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.biometric)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.bouncycastle)
  implementation(libs.sqlcipher)
  implementation(libs.androidx.sqlite.ktx)
  implementation(libs.coil.compose)
  implementation("io.coil-kt.coil3:coil-network-okhttp:${libs.versions.coilCompose.get()}")
  implementation(libs.jsoup)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation("info.guardianproject:jtorctl:0.4.5.7")
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.matching { it.name.contains("AarMetadata") }.configureEach {
  enabled = false
}
