import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// ---------------------------------------------------------------------------
// Release signing is fully environment driven so that no secret ever lives in
// the repository. When the values are absent (local builds, forks, PRs) the
// release build falls back to the debug keystore and still produces an APK.
// ---------------------------------------------------------------------------
val keystorePath: String? = System.getenv("KEYSTORE_FILE")
val keystorePassword: String? = System.getenv("KEYSTORE_PASSWORD")
val keyAliasValue: String? = System.getenv("KEY_ALIAS")
val keyPasswordValue: String? = System.getenv("KEY_PASSWORD")
val hasReleaseKey =
    !keystorePath.isNullOrBlank() &&
        !keystorePassword.isNullOrBlank() &&
        !keyAliasValue.isNullOrBlank() &&
        !keyPasswordValue.isNullOrBlank() &&
        file(keystorePath).exists()

// ABI splits and Android App Bundles are mutually exclusive: the bundle already
// splits per ABI, and AGP fails when both are requested in one invocation.
val buildingAppBundle =
    gradle.startParameter.taskNames.any { it.contains("bundle", ignoreCase = true) }

android {
    namespace = "com.naimpro.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.naimpro.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // Keep the APK small: only the locales we actually ship.
        resourceConfigurations += setOf("tr", "en", "ar")
    }

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(keystorePath!!)
                storePassword = keystorePassword
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
                // v2 + v3 cover every device this app supports (minSdk 26).
                // AGP deliberately skips the v1 (JAR) scheme once minSdk >= 24:
                // v2 already supersedes it, so a v1 signature would only make
                // the APK larger without adding compatibility. Setting
                // enableV1Signing = true here has no effect for that reason.
                enableV1Signing = false
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            isTestCoverageEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig =
                if (hasReleaseKey) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
        }
    }

    splits {
        abi {
            isEnable = !buildingAppBundle
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86_64")
            isUniversalApk = false
        }
    }

    bundle {
        language.enableSplit = false
        density.enableSplit = false
        abi.enableSplit = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = false
    }

    packaging {
        resources {
            excludes +=
                setOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "/META-INF/DEPENDENCIES",
                    "/META-INF/LICENSE*",
                    "/META-INF/NOTICE*",
                    "DebugProbesKt.bin",
                    "kotlin-tooling-metadata.json",
                )
        }
    }

    lint {
        warningsAsErrors = false
        abortOnError = true
        checkDependencies = true
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable")
        baseline = file("lint-baseline.xml")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
                test.useJUnitPlatform()
                test.testLogging {
                    events("passed", "skipped", "failed")
                }
            }
        }
    }

    sourceSets["main"].assets.srcDirs("src/main/assets")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.glance.GlanceExperimentalApi",
        )
        // Compose compiler metrics / reports can be enabled locally with
        // -Pnaim.composeMetrics=true (see docs/BUILD.md).
        if (project.findProperty("naim.composeMetrics") == "true") {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                    layout.buildDirectory.dir("compose-reports").get().asFile.absolutePath,
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                    layout.buildDirectory.dir("compose-metrics").get().asFile.absolutePath,
            )
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.leakcanary)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.espresso.core)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// ---------------------------------------------------------------------------
// Static analysis configuration
// ---------------------------------------------------------------------------
detekt {
    buildUponDefaultConfig = true
    parallel = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    source.setFrom(files("src/main/java", "src/test/java", "src/androidTest/java"))
}

ktlint {
    android.set(true)
    ignoreFailures.set(false)
    filter {
        exclude { it.file.path.contains("build${File.separator}generated") }
    }
    disabledRules.set(setOf("standard:function-naming", "standard:max-line-length"))
}

// Configuration-cache safe: the locations are captured as task inputs/outputs at
// configuration time; nothing touches `project` while the task runs.
abstract class ReportApkSizeTask : DefaultTask() {
    @get:Internal
    abstract val apkDir: DirectoryProperty

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun report() {
        val apks =
            apkDir.get().asFile
                .walkTopDown()
                .filter { it.extension == "apk" }
                .sortedBy { it.name }
                .toList()
        val report = apks.joinToString("\n") { "${it.name}: ${it.length() / 1024} KiB" }
        reportFile.get().asFile.apply { parentFile.mkdirs() }.writeText(report)
        logger.lifecycle(report.ifEmpty { "No release APKs found." })
    }
}

tasks.register<ReportApkSizeTask>("reportApkSize") {
    dependsOn("assembleRelease")
    apkDir.set(layout.buildDirectory.dir("outputs/apk/release"))
    reportFile.set(layout.buildDirectory.file("apk-size.txt"))
}

// Silence the unused properties warning for local builds.
if (!hasReleaseKey) {
    logger.lifecycle("Release signing key not configured - release builds will use the debug key.")
}
@Suppress("UNUSED_EXPRESSION")
Properties()
