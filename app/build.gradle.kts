plugins {
    alias(libs.plugins.androidApplication)
    id("androidx.navigation.safeargs") version "2.7.7"
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")


}

android {
    namespace = "com.example.easydoc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.easydoc"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

}
secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}

dependencies {


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    // implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.materialdatetimepicker)
    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.messaging)
    //  implementation(libs.google.services)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.maps)


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries


    //implementation(libs.firebase.ui.auth)

    // Required only if Facebook login support is required
    // Find the latest Facebook SDK releases here: https://goo.gl/Ce5L94
//    implementation 'com.facebook.android:facebook-android-sdk:8.x'

    // implementation(libs.firebase.database)
    //  implementation(libs.firebase.ui.auth)
    //    implementation("com.wdullaer:materialdatetimepicker:4.2.3")
    // implementation(libs.gms.play.services.auth)
    //  implementation(libs.firebase.messaging)
    //implementation(libs.gms.play.services.auth)
    implementation(libs.lottie)
    //implementation(libs.navigation)
    //implementation(libs.navigation.v530)


}
