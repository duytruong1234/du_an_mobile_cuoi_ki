plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "vn.duytruong.appbandienthoai"
    compileSdk = 36

    defaultConfig {
        applicationId = "vn.duytruong.appbandienthoai"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
        viewBinding =   true
        dataBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }

}

dependencies {
    // Multidex support
    implementation("androidx.multidex:multidex:2.0.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // SwipeRefreshLayout for pull-to-refresh lists
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(fileTree("D:\\Zalopay") {
        include("*.aar", "*.jar")
    })
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:5.0.5")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.akarnokd:rxjava3-retrofit-adapter:3.0.0")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")

    // OkHttp logging interceptor để debug API
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.nex3z:notification-badge:1.0.4")

    // EventBus cho Android
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("io.github.pilgr:paperdb:2.7.2")

    implementation("com.google.code.gson:gson:2.8.9")

    // Volley (HTTP client lightweight)
    implementation("com.android.volley:volley:1.2.1")

    // Firebase Cloud Messaging for push notifications
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    implementation("com.squareup.okhttp3:okhttp:4.6.0")
    implementation("commons-codec:commons-codec:1.14")
    implementation("com.google.android.material:material:1.12.0")

    implementation("com.github.fornewid:neumorphism:0.3.2")
    // MPAndroidChart - use the jitpack coordinate (v3.1.0) which is known to resolve correctly.
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") {
        exclude(group = "com.android.support")
    }


}