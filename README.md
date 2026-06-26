# CallController

Application Android **open-source** qui rejette automatiquement les appels de **démarchage téléphonique** selon des **règles de motif (préfixe / pattern)**, avec des **presets partagés par la communauté**. Pensée **vie privée d'abord** : traitement local, zéro télémétrie, jamais de revente de données.

> 🇫🇷 Cible prioritaire : la France. Le preset par défaut bloque les tranches de numéros réservées au démarchage par l'ARCEP (Numéros Polyvalents Vérifiés).

## Documentation

- [`SPEC.md`](./SPEC.md) — vision, périmètre, contexte réglementaire, roadmap.
- [`CLAUDE.md`](./CLAUDE.md) — architecture détaillée et règles de contribution / collaboration.
- [`presets/`](./presets) — presets communautaires (JSON). Source de vérité, copiée dans l'app à la compilation.

## Stack

Kotlin · Jetpack Compose · `CallScreeningService` (API 29+) · Room · `minSdk 29` / `targetSdk 35`.

## Build & run

⚠️ Nécessite **Android Studio** (embarque le JDK, le SDK Android et l'émulateur).

1. Installer [Android Studio](https://developer.android.com/studio).
2. *File → Open* → sélectionner ce dossier. Android Studio génère le wrapper Gradle et synchronise les dépendances au premier ouvrage.
3. Lancer sur un appareil/émulateur **Android 10+** (API 29+) via *Run ▶*.
4. Dans l'app, activer le filtrage (accorde le rôle « filtrage des appels »).

Tests unitaires du moteur (cœur métier, sans appareil) :

```bash
./gradlew test
```

## Licence

GPLv3 (à ajouter via le sélecteur de licence GitHub ou [gnu.org](https://www.gnu.org/licenses/gpl-3.0.txt)).
