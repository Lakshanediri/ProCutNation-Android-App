            plugins {
    alias(libs.plugins.android.application)

                id("com.google.gms.google-services")
}

android {
    namespace = "com.s22010104.procutnation"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.s22010104.procutnation"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Default libraries
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")

    // CardView for rounded containers
    implementation ("androidx.cardview:cardview:1.0.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    // Firebase Authentication
    implementation ("com.google.firebase:firebase-auth")

    // Firebase Firestore Database
    implementation ("com.google.firebase:firebase-firestore")

    // Google Sign-In authentication
    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    // Google Maps
    implementation ("com.google.android.gms:play-services-maps:18.2.0")

    // Charts Library (MPAndroidChart)
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Add this for CircleImageView (for profile picture)
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // ... other default dependencies like testImplementation

    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Firebase Storage for uploading files
    implementation ("com.google.firebase:firebase-storage")

    // Glide for loading images from a URL
    implementation ("com.github.bumptech.glide:glide:4.16.0")

}