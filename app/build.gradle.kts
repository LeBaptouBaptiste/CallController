plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Le tag Git qui déclenche la release (TAG_VERSION = "v1.2.3", voir
// .github/workflows/release.yml) est la seule source de vérité de la version.
// Sans tag (build local), on produit une version de développement.
val numerosVersion: List<Int>? = System.getenv("TAG_VERSION")?.let { tag ->
    val correspondance = requireNotNull(Regex("""v(\d{1,3})\.(\d{1,3})\.(\d{1,3})""").matchEntire(tag)) {
        "TAG_VERSION invalide : « $tag » (attendu : vX.Y.Z, chaque nombre ≤ 999)"
    }
    correspondance.groupValues.drop(1).map(String::toInt)
}

android {
    namespace = "fr.voyager3.callcontroller"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.voyager3.callcontroller"
        // 29 : premier niveau où ROLE_CALL_SCREENING existe.
        minSdk = 29
        // 36 : exigé par Google Play pour toute nouvelle app ou mise à jour depuis le 31/08/2026.
        targetSdk = 36
        // Google Play exige un versionCode strictement croissant : 3 chiffres par
        // composante, v1.2.3 → 1 002 003.
        versionCode = numerosVersion?.let { (majeur, mineur, correctif) ->
            majeur * 1_000_000 + mineur * 1_000 + correctif
        } ?: 1
        versionName = numerosVersion?.joinToString(".") ?: "0.0.0-dev"
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
        // Le suffixe permet d'installer le debug à côté de la release signée, sans
        // la désinstaller (donc sans perdre ses règles ni son journal).
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
    }

    // Nomme l'APK d'après le projet et sa version (ex. CallController-0.1.0.apk) au
    // lieu du "app-release.apk" par défaut — plus parlant sur les Releases GitHub.
    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl)
                .outputFileName = "CallController-$versionName.apk"
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
