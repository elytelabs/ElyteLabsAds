plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "com.elytelabs.ads"
    compileSdk = 36

    defaultConfig {
        minSdk = 25
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Retrofit + Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Glide
    implementation(libs.glide)
    implementation(libs.glide.transformations)
    annotationProcessor(libs.glide.compiler)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.elytelabs"
                artifactId = "ads"
                version = "1.0.1"
            }
        }
    }
}
