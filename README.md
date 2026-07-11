# CallController

[![CI](https://github.com/LeBaptouBaptiste/CallController/actions/workflows/ci.yml/badge.svg)](https://github.com/LeBaptouBaptiste/CallController/actions/workflows/ci.yml)

Application Android **open-source** qui rejette automatiquement les appels de **démarchage téléphonique** selon des **règles de motif (préfixe / pattern)**, avec des **presets partagés par la communauté**. Pensée **vie privée d'abord** : traitement local, zéro télémétrie, jamais de revente de données.

> 🇫🇷 Cible prioritaire : la France. Le preset par défaut bloque les tranches de numéros réservées au démarchage par l'ARCEP (Numéros Polyvalents Vérifiés).

## Documentation

- [`SPEC.md`](./SPEC.md) — vision, périmètre, contexte réglementaire, roadmap.
- [`CLAUDE.md`](./CLAUDE.md) — architecture détaillée et règles de contribution / collaboration.
- [`presets/`](./presets) — presets communautaires (JSON). Source de vérité, copiée dans l'app à la compilation.

## Stack

Kotlin · Jetpack Compose · `CallScreeningService` (API 29+) · Room · DataStore · RE2 (regex anti-ReDoS) · `minSdk 29` / `targetSdk 35`.

## Build & run

Le wrapper Gradle est inclus : un clone frais se build sans installer Gradle (un JDK 17 et le SDK Android suffisent).

**Avec Android Studio** (recommandé — embarque JDK, SDK et émulateur) :

1. Installer [Android Studio](https://developer.android.com/studio).
2. *File → Open* → sélectionner ce dossier, laisser la synchronisation Gradle se faire.
3. Lancer sur un appareil/émulateur **Android 10+** (API 29+) via *Run ▶*.
4. Dans l'app, activer le filtrage (accorde le rôle « filtrage des appels »).

**En ligne de commande :**

```bash
./gradlew assembleDebug   # APK debug
./gradlew installDebug    # installe sur un appareil connecté
./gradlew test            # tests unitaires du moteur (sans appareil)
./gradlew lintDebug       # lint Android
```

## Soutenir le projet

CallController est gratuit, sans pub et sans revente de données. Pour soutenir son développement : [ko-fi.com/baptistevidal](https://ko-fi.com/baptistevidal). **Dons uniquement, jamais de contenu payant.**

## Licence

Sous [**GPLv3**](./LICENSE) (copyleft, anti-récupération commerciale).
