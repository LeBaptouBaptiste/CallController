plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.voyager3.callcontroller"
    compileSdk = 35

    defaultConfig {
        applicationId = "fr.voyager3.callcontroller"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Renseigné uniquement en CI via des variables d'environnement issues de
            // secrets (voir .github/workflows/release.yml). En local sans keystore, le
            // build release reste non signé sans échouer. Zéro secret dans le dépôt.
            System.getenv("KEYSTORE_FILE")?.let { chemin ->
                storeFile = file(chemin)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
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
        compose = true
    }
}

// DRY : le preset canonique vit dans /presets (racine, source de vérité, publié
// sur GitHub). On le copie dans les assets embarqués à la compilation plutôt
// que de le dupliquer à la main.
val synchroniserPresets by tasks.registering(Copy::class) {
    from(rootProject.file("presets"))
    into(layout.projectDirectory.dir("src/main/assets/presets"))
}
tasks.named("preBuild") { dependsOn(synchroniserPresets) }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Moteur regex à temps linéaire : neutralise le ReDoS des presets communautaires.
    implementation(libs.re2j)

    testImplementation(libs.junit)
}
