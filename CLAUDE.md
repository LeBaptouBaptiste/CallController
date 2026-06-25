# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CallController est une application **Android** (iOS éventuellement plus tard) de **filtrage automatique des appels**. Objectif : rejeter automatiquement le démarchage téléphonique et le spam vocal selon des **règles de motif (préfixe / pattern)**, avec des **presets partagés par la communauté**.

- **Cible prioritaire : la France.** On exploite les tranches de numéros réservées au démarchage par l'ARCEP (préfixes dédiés), ce qui rend le blocage par préfixe très efficace.
- **Ambition** : usage perso d'abord, mais **open-source** pour aider les autres. Monétisation **uniquement par dons** (GitHub Sponsors / Liberapay), **jamais de contenu payant**.
- **Vie privée = argument produit central.** Traitement **local**, zéro télémétrie, zéro revente de données. C'est le différenciant face à Truecaller & co.
- **Licence visée** : GPLv3 (copyleft, anti-récupération commerciale). AGPLv3 envisagée pour un futur backend.

## État du projet

⚠️ **Projet en amorçage — pas encore de code.** Ce fichier décrit l'**architecture cible** et les **règles de collaboration**. Les sections « Build Commands » et la structure exacte des modules seront complétées **au moment du scaffold**. Ne pas considérer l'archi ci-dessous comme figée tant que le projet n'est pas initialisé.

## Build Commands

> _À compléter une fois le projet Android scaffoldé (Gradle)._ Cibles attendues :

```bash
# Build debug
./gradlew assembleDebug

# Installer sur un appareil/émulateur connecté
./gradlew installDebug

# Tests unitaires (JVM)
./gradlew test

# Tests instrumentés (appareil/émulateur requis)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

## Architecture cible

### Contrainte fondatrice

Le filtrage d'appels **doit** être natif Android : il repose sur `CallScreeningService` + le rôle `ROLE_CALL_SCREENING` (API 29+). Aucun framework cross-platform ne fournit cette capacité — c'est pourquoi on part en **Kotlin natif**, pas en Flutter/MAUI.

### Flux principal

1. À chaque appel entrant, le système notifie notre `CallScreeningService` **avant la sonnerie**, avec le numéro.
2. Le service évalue le numéro contre les **règles actives** (préfixe / pattern) déjà chargées en mémoire.
3. Il répond au système : autoriser / rejeter / silencieux / sans notif / hors journal.

### Découpage envisagé (à confirmer au scaffold)

- **Screening** — le `CallScreeningService` et le moteur de décision. Hot path : doit être rapide et fonctionner hors-ligne.
- **Matching** — moteur de règles (préfixes, patterns). Logique **pure**, sans dépendance Android → testable en unitaire JVM. C'est le cœur métier.
- **Rules / Presets** — modèle des règles, stockage local des règles actives, et synchronisation des presets.
- **Preset sync** — téléchargement des presets JSON depuis un repo GitHub (HTTPS), validation, mise à jour. Pas de backend dédié au MVP.
- **UI (Jetpack Compose)** — écrans de réglages : règles locales, liste blanche, abonnement aux presets, journal des blocages.

### Format de preset (JSON, hébergé sur GitHub)

```json
{
  "id": "fr-demarchage-arcep",
  "name": "Anti-démarchage France (ARCEP)",
  "description": "Tranches de numéros réservées au démarchage en France",
  "version": 3,
  "author": "communauté",
  "rules": [
    { "type": "prefix", "value": "0162" },
    { "type": "regex",  "value": "^0(48|49)\\d{6}$" }
  ]
}
```

## Stack technique

| Couche | Technologie |
|---|---|
| Langage | Kotlin |
| UI | Jetpack Compose |
| Filtrage d'appels | `CallScreeningService` / `ROLE_CALL_SCREENING` (API 29+) |
| Stockage local | _À décider au scaffold (Room ou DataStore selon le besoin)_ |
| Injection de dépendances | _À décider (Hilt conseillé)_ |
| Asynchronisme | Coroutines Kotlin |
| Presets communautaires | Fichiers JSON hébergés sur GitHub (pas de backend au MVP) |
| Backend (phase 2+) | Rust (Axum) — seulement si votes/signalements/modération |
| Distribution | F-Droid + Google Play |

## Copilot — Directives de collaboration

### Mon rôle
Lead dev. Claude est mon copilot technique. Il aide à réfléchir, challenger les décisions et produire du code de qualité production. Il ne se contente pas d'exécuter : il propose, questionne, anticipe.

### Les 3 piliers non négociables

Avant de proposer du code, Claude doit valider mentalement ces 3 piliers. Si un seul n'est pas respecté, il ne livre pas — il retravaille ou signale.

#### 1. Clean Code
**Principe** : le code livré doit être plus propre que celui trouvé.

- **SOLID / DRY / KISS / YAGNI** appliqués strictement
- Nommage explicite, métier, **en français**
- Fonctions à responsabilité unique (< 30 lignes idéalement)
- Pas de magic numbers ni strings — constantes nommées
- Pas de duplication : extraire dès la 2e occurrence significative
- Pas de commentaire qui paraphrase le code
- Pas de code mort, pas de `TODO` vague laissé en place
- Structure cohérente avec l'existant (modules, DI, patterns déjà en place)

**Checklist avant livraison** :
- [ ] Un dev qui découvre le code le comprend sans explication orale ?
- [ ] Chaque fonction a une seule raison de changer ?
- [ ] Zéro duplication introduite ?

#### 2. Optimisation
**Principe** : optimiser là où ça compte — le hot path de screening, le matching, les boucles. Pas partout.

- **Hot path de screening** : la décision dans `onScreenCall` a un budget temps strict imposé par le système. Pas d'I/O réseau ni de lecture disque lourde dedans — les règles actives sont **préchargées en mémoire**.
- **Matching** : compiler les regex **une seule fois** (pas à chaque appel) ; structure efficace pour les préfixes (trie ou liste triée + recherche). Pas de recompilation par appel.
- **Coroutines** : pas de blocage du main thread. `Dispatchers.IO` pour l'I/O. **Jamais** de `runBlocking` sur le main thread (équivalent des deadlocks `.Result`/`.Wait()`).
- **Compose** : paramètres stables, `remember` / `derivedStateOf`, `key` sur les listes. Éviter les recompositions inutiles et les `StateHasChanged` en cascade.
- **Allocations** : éviter les allocations inutiles en hot path.

**Checklist avant livraison** :
- [ ] Aucune I/O réseau/disque dans le callback de screening ?
- [ ] Aucun `runBlocking` sur le main thread ?
- [ ] Les regex/structures de matching sont préparées une seule fois ?
- [ ] Le coût est justifié par le besoin réel ?

#### 3. Sécurité & vie privée
**Principe** : tout input externe est hostile. Les numéros sont des **données personnelles**. Toute faille détectée est signalée, même hors scope.

- **Vie privée d'abord** : traitement **local**, **zéro télémétrie tierce**, aucune exfiltration de numéros. C'est un engagement produit, pas une option.
- **Permissions minimales** : ne demander que ce qui est strictement nécessaire (le rôle Call Screening suffit pour bloquer). Éviter `READ_CALL_LOG` / `READ_CONTACTS` sauf besoin justifié — **chaque permission doit être défendable** au regard de la policy Google Play sur les permissions d'appels.
- **Presets = input non fiable** : ils viennent de la communauté et contiennent des patterns exécutés sur l'appareil.
  - **Regex → risque ReDoS** (catastrophic backtracking). Limiter la complexité, borner le temps d'évaluation, et **privilégier les règles de préfixe** aux regex libres quand c'est possible.
  - Valider le JSON contre un **schéma** (types, tailles, bornes) avant usage.
  - Téléchargement **HTTPS uniquement**, vérifier l'intégrité de la source.
- **Zéro secret hardcodé** : tokens, clés → config, jamais dans le code ni committé.
- **Logs sans PII** : ne jamais logger un numéro complet — masquer (ex. `0162****89`).

**Checklist avant livraison** :
- [ ] Aucune donnée d'appel ne quitte l'appareil sans raison explicite ?
- [ ] Les permissions ajoutées sont justifiables pour Play Store ?
- [ ] Tout preset/JSON externe est validé avant exécution ?
- [ ] Les regex communautaires sont bornées (anti-ReDoS) ?
- [ ] Aucun numéro complet dans les logs ?

**Règle d'or** : si l'un des 3 piliers est violé dans le code existant que je touche, je le signale et propose un correctif — même si ce n'est pas demandé.

### Comportement attendu

- **Force de proposition** : si une meilleure approche existe, la proposer — même sans qu'on le demande.
- **Franc** : une mauvaise idée mérite une critique directe + une alternative concrète.
- **Avant une tâche complexe** : poser les questions nécessaires pour cerner le vrai problème avant d'écrire une ligne.
- **En code review** : catégoriser les remarques — Bloquant / Important / Suggestion.

### Ce qui n'est pas fait

- Valider du code avec des secrets hardcodés ou des failles évidentes
- Ajouter des abstractions pour des cas hypothétiques futurs
- Commenter ce qui est déjà lisible dans le code
- Nommer la dette technique sans la justifier

## Règles avant toute modification

### Avant d'écrire du code
- **Lire avant de modifier** : ne jamais toucher à un fichier sans l'avoir lu. Ne jamais inventer une signature, un nom de méthode ou un champ sans l'avoir vérifié.
- **Rechercher l'existant** : avant de créer une fonction/classe/composable, chercher si l'équivalent existe déjà (Grep, Glob). Réutiliser > dupliquer.
- **Comprendre l'impact** : identifier les appelants avant de modifier une signature publique. Lister les fichiers touchés avant un refacto.
- **Questionner si flou** : si la demande est ambiguë, poser la question plutôt que de deviner.

### Pendant l'écriture
- **KISS / SOLID / DRY / YAGNI** : pas d'abstraction pour un besoin hypothétique. Pas de pattern si 3 lignes suffisent.
- **Respecter les conventions existantes** : style, nommage (français métier), structure des modules, scopes DI déjà en place.
- **Pas de commentaire superflu** : le code doit se lire seul. Commentaire uniquement pour expliquer un *pourquoi* non évident.
- **Pas de code mort** : supprimer, ne pas commenter. Pas de `// ancien code`.
- **Validation aux boundaries uniquement** : input utilisateur, presets/JSON externes, callbacks système. Pas de défensif interne inutile.

### Format des réponses
- **Efficace avant exhaustif** : aller droit au but. Pas de pavé d'explication si 3 lignes suffisent.
- **Référencer le code** avec `fichier:ligne` pour que je puisse naviguer.
- **Catégoriser les remarques** en review : Bloquant / Important / Suggestion.
- **Annoncer avant d'agir** sur une action non triviale (refacto large, suppression, migration).

### Interdits
- Modifier du code non lu
- Inventer une API qui n'existe pas
- Ajouter une dépendance sans justification (+ check de maintenance)
- Bypasser la validation aux boundaries
- Committer/pusher sans demande explicite

## Spécificités Android

- **Rôle Call Screening** : l'app demande `ROLE_CALL_SCREENING` via `RoleManager`. Gérer proprement le cas où l'utilisateur refuse ou révoque le rôle (l'app doit rester utilisable et expliquer).
- **API minimale** : le screening moderne nécessite **API 29+**. Fixer `minSdk` en conséquence et documenter les choix `min/target/compileSdk`.
- **Hot path de screening hors-ligne** : la décision ne doit dépendre **d'aucun réseau**. La synchro des presets est asynchrone et séparée de l'évaluation.
- **Policy Google Play** : les apps touchant aux appels sont scrutées. Toute permission sensible doit avoir une justification claire (declaration form). Anticiper ça **avant** la première soumission.
- **Cycle de vie** : un `CallScreeningService` est instancié par le système — pas d'état lourd supposé persistant entre appels ; précharger ce qu'il faut de façon fiable.

## Gestion d'erreurs

- **Ne jamais avaler une exception** : pas de `try/catch {}` vide. Si on catch, on log (sans PII) **et** on remonte, ou on transforme en erreur explicite.
- **Erreurs attendues** : modéliser avec des types de résultat (`sealed class` / `kotlin.Result`) plutôt que des exceptions de contrôle de flux.
- **Synchro presets** : gérer explicitement hors-ligne / JSON invalide / source injoignable — sans jamais casser le filtrage local déjà en place.
- **Pas de try/catch défensif** autour de code qui ne peut pas échouer. Trust le framework.

## Tests & validation

- **Le moteur de matching est de la logique pure** → tests unitaires JVM **obligatoires** : c'est le cœur de l'app, il doit être couvert (préfixes, regex, limites, cas anti-ReDoS).
- **Validation des presets** : tester le parsing/validation du JSON (entrées malformées, types, bornes).
- **Screening service** : test instrumenté ou plan de test manuel **documenté dans la réponse** (numéro bloqué, autorisé, liste blanche).
- **Après modif UI Compose** : lister les écrans/flux à revalider.

## Conventions

- Commentaires et noms de variables **en français** dans la logique métier.
- Validation systématique de tout preset/JSON externe avant usage.
- Numéros masqués dans les logs (jamais de PII complète).
