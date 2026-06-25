# CallController — Spécification

> Document de cadrage produit : la **vision**, le **périmètre**, le **contexte réglementaire** et la **roadmap**.
> Les règles de travail et les conventions techniques sont dans [`CLAUDE.md`](./CLAUDE.md).

## 1. Problème & vision

Le démarchage téléphonique et le spam vocal sont une nuisance quotidienne. Les solutions existantes sont soit **payantes**, soit **opaques sur la vie privée** (Truecaller revend les données, exige l'accès au carnet d'adresses, etc.).

**Vision** : une app Android qui **rejette automatiquement les appels indésirables** selon des **règles de motif (préfixe / pattern)**, alimentée par des **presets partagés par la communauté**, **respectueuse de la vie privée** (traitement local, zéro télémétrie) et **open-source**.

## 2. Cible & contexte réglementaire (France)

La cible prioritaire est la **France**, parce que la réglementation y rend le blocage par préfixe redoutablement efficace.

Depuis le **1er janvier 2023**, l'ARCEP impose aux appels de démarchage **automatisé** d'utiliser des **Numéros Polyvalents Vérifiés (NPV)** dans des tranches dédiées. Aucun appelant légitime n'utilise ces préfixes → les bloquer élimine l'essentiel du démarchage légal.

**Préfixes NPV alloués (vérifiés en juin 2026) :**

| Zone | Préfixes |
|---|---|
| Métropole | `0162` `0163` · `0270` `0271` · `0377` `0378` · `0424` `0425` · `0568` `0569` · `0948` `0949` |
| Outre-mer | `09475` → `09479` |

> ⚠️ L'ARCEP **élargit ces allocations au fil du temps**. Le preset par défaut ([`presets/fr-demarchage-arcep.json`](./presets/fr-demarchage-arcep.json)) est **versionné** et doit être mis à jour quand de nouvelles tranches sont allouées. Les bandes complètes (ex. `0162`–`0168`) étant *réservées* à cet usage, on pourra envisager de bloquer la bande entière pour être à l'épreuve du futur (à reconfirmer avant de l'inscrire en dur).

**Concurrents / positionnement** : Truecaller, Hiya, Orange Téléphone, Should I Answer, Calls Blacklist. **Différenciant** : local-first, vie privée, presets communautaires open-source. Référence FOSS à étudier : *Yet Another Call Blocker* (F-Droid).

## 3. Contrainte technique fondatrice

| Plateforme | Capacité |
|---|---|
| **Android** | `CallScreeningService` + rôle `ROLE_CALL_SCREENING` (API 29+) : le système fournit le **numéro avant la sonnerie**, l'app décide accepter/rejeter/silencieux. **Le matching par pattern est possible.** ✅ |
| **iOS** | `CallDirectory Extension` : on ne fournit qu'une **liste de numéros exacts**, **pas de logique par appel**. Le blocage par préfixe est **impossible** nativement → architecture différente, **reportée**. ⚠️ |

→ **Android-only au départ.** Stack : **Kotlin + Jetpack Compose** (le cœur étant une API Android non portable, le cross-platform n'apporterait quasiment rien — voir `CLAUDE.md`).

## 4. Périmètre

### MVP (v1) — dans le périmètre
- Demande et gestion du rôle Call Screening.
- Règles locales : blocage par **préfixe** et **pattern**.
- **Preset France « anti-démarchage »** intégré par défaut (données ARCEP ci-dessus).
- **Liste blanche** (numéros / contacts jamais bloqués).
- **Journal** des appels bloqués.
- Aucun backend.

### Hors périmètre MVP (plus tard)
- Synchronisation de presets communautaires (phase 2).
- Plages « Ne pas déranger », stats, recherche inversée (phase 3).
- Votes / signalements / modération + backend Rust (phase 4).
- iOS (Call Directory).
- **Répondeur custom** : un vrai répondeur (jouer un message à l'appelant, enregistrer) est **techniquement très coûteux** sur Android sans devenir l'app Téléphone par défaut, et la messagerie visuelle dépend de l'opérateur (VVM). **Projet à part entière, repoussé loin.** Version réaliste à terme : laisser filer vers la messagerie opérateur + gérer/consulter l'existant.

## 5. Architecture cible (résumé)

Détail et conventions dans [`CLAUDE.md`](./CLAUDE.md). En bref :

- **Screening** : `CallScreeningService` + moteur de décision. Hot path **rapide et hors-ligne** (budget temps système strict).
- **Matching** : logique **pure** (préfixes via structure efficace, regex **bornées anti-ReDoS**), testable en unitaire JVM. Cœur métier.
- **Normalisation** : tout numéro entrant est normalisé en forme canonique (gérer `0X…` **et** `+33X…`) **avant** comparaison.
- **Presets** : modèle + stockage local des règles actives ; synchro asynchrone séparée de l'évaluation.
- **UI Compose** : réglages, liste blanche, abonnement aux presets, journal.

## 6. Format des presets

Presets = fichiers **JSON hébergés sur un repo GitHub** ; l'app s'y abonne et se met à jour. Contributions par Pull Request. Exemple de référence : [`presets/fr-demarchage-arcep.json`](./presets/fr-demarchage-arcep.json).

```json
{
  "id": "fr-demarchage-arcep",
  "name": "Anti-démarchage France (ARCEP)",
  "description": "…",
  "version": 1,
  "author": "communauté",
  "source": "https://www.arcep.fr/…",
  "rules": [
    { "type": "prefix", "value": "0162", "label": "Métropole" },
    { "type": "regex",  "value": "^0(48|49)\\d{6}$" }
  ]
}
```

**Validation obligatoire** de tout preset avant usage (schéma, types, bornes ; regex bornées contre le ReDoS) — input communautaire = non fiable.

## 7. Roadmap

| Phase | Contenu | Backend |
|------|---------|---------|
| **0 — Setup** | Repo, licence GPLv3, `SPEC.md` + `CLAUDE.md`, scaffold Android | — |
| **1 — MVP** | Screening, règles préfixe/pattern, liste blanche, preset ARCEP, journal | Aucun |
| **2 — Presets communautaires** | Abonnement aux presets JSON GitHub, maj auto | GitHub seul |
| **3 — Confort** | Ne pas déranger, blocage numéros masqués, stats, recherche inversée | Aucun |
| **4 — Communauté+** | Votes, signalements, modération | **API Rust (Axum)** |
| **+ tard** | iOS (Call Directory), pistes répondeur | Réécriture iOS |

## 8. Vie privée, licence & dons

- **Vie privée** : traitement **local**, **zéro télémétrie tierce**, **zéro revente**. Permissions minimales (justifiables pour la policy Google Play). Engagement produit central.
- **Licence** : **GPLv3** pour l'app (copyleft, anti-récupération commerciale). **AGPLv3** envisagée pour un futur backend. Note : conflit connu GPL ↔ Apple App Store (cas VLC) — exception « App Store » à prévoir si iOS un jour.
- **Dons** : **GitHub Sponsors** + **Liberapay** (européen, sans commission). **Jamais de contenu payant.**

## 9. Risques & points ouverts

- **Policy Google Play** sur les permissions d'appels : à anticiper **avant** la 1ʳᵉ soumission (formulaire de justification).
- **Évolution des tranches ARCEP** : preset à maintenir.
- **Points à trancher au scaffold** : `minSdk/targetSdk`, stockage (Room vs DataStore), DI (Hilt), confirmation GPLv3, découpage exact des modules.

## 10. Sources

- ARCEP — Plan de numérotation pour les professionnels (fiche pratique) : https://www.arcep.fr/mes-demarches-et-services/entreprises/fiches-pratiques/plan-numerotation-professionnels.html
- ARCEP — La numérotation (dossier) : https://www.arcep.fr/la-regulation/grands-dossiers-thematiques-transverses/la-numerotation.html
- En-Contact — Nouvelles tranches NPV pour le démarchage : https://en-contact.com/la-foire-aux-numeros-larcep-alloue-des-nouvelles-tranches-de-numeros-obligatoires-pour-le-demarchage-telephonique
