# CLAUDE.md — Épure EPUB

> Version de travail, 10/11/2026. Document rédigé par déclinaison de `CLAUDE.md` d'Épure PDF.
> Ce dossier n'est pas encore un dépôt : il sera copié à la racine du futur dépôt `epure-epub` lors du jalon 0.

Consignes pour l'assistant de code. À lire en entier au début de chaque session.

## 1. Avant toute tâche

1. Lire `docs/ROADMAP.md` : identifier le jalon et la session en cours.
2. Lire dans `docs/SPEC.md` les exigences de cette session.
3. Lire `docs/DECISIONS.md` (décisions prises) et `docs/CHARTE.md` (règles visuelles, d'interaction et de sécurité).
4. Ne travailler **que** sur le jalon et la session en cours. Ne pas anticiper les jalons suivants.

## 2. Projet

Lecteur EPUB Android minimaliste, entièrement hors ligne, dont le contenu est traité comme **non fiable**. Lire sans distraction, sans réseau, sans collecte de données, sans exécution de code venu du livre.

- Nom affiché « Épure EPUB » ; nom technique `EpureEpub` ; `applicationId` `com.alain.epure.epub` (proposition, à consigner dans `DECISIONS.md` avant le premier build).
- Licence GPL v3. Dépôt public `epure-epub`.
- Application distincte d'Épure PDF et du launcher Épure : aucune dépendance de code entre elles (DEC-0037, DEC-0002).
- Le porteur du projet n'est pas développeur Android de métier : expliquer clairement, sans jargon inutile, ce qui a été fait et pourquoi.

## 3. Choix techniques figés

- Kotlin uniquement ; aucun code natif ajouté (pas de NDK, pas de Rust).
- Jetpack Compose, Material 3, thème sombre personnalisé (trois jetons : `Black`, `TextPrimary`, `TextSecondary`).
- `minSdk` 31 ; `targetSdk` / `compileSdk` 36.
- Une seule activité ; `ViewModel` + `StateFlow` ; navigation par état simple (pas de `navigation-compose`) ; pas de framework d'injection (`AppContainer`).
- Paquets : `ui/` (écrans), `data/` (persistance), `system/` (API Android, moteur de rendu EPUB, SAF, intents), `domain/` (logique pure, sans dépendance Android, testée).
- Le moteur de rendu est isolé derrière une interface dans `system/`.
- `allowBackup="false"`.
- Edge-to-edge géré par `WindowInsets` ; retour par `BackHandler`, jamais `onBackPressed()`.
- Le choix du moteur de rendu (WebView ou autre) est **[À DÉCIDER]** : voir `SPEC.md` §6. Aucun moteur n'est ajouté avant la décision consignée dans `DECISIONS.md`.

## 4. Règles absolues

### Aucune permission réseau (DEC-0005, reprise)

- `INTERNET` et `ACCESS_NETWORK_STATE` absentes du manifeste source **et** du manifeste fusionné.
- Après tout ajout ou mise à jour de dépendance, contrôler :
  ```
  ./gradlew assembleDebug
  $ANDROID_HOME/build-tools/<version>/aapt2 dump permissions app/build/outputs/apk/debug/app-debug.apk
  ```
  Résultat attendu : aucune permission, sauf celles du tableau `SPEC.md` §8.
- Permission injectée par une bibliothèque : la signaler, puis la retirer avec `tools:node="remove"` ou écarter la bibliothèque, avec entrée dans `DECISIONS.md`.

### Fichiers et permissions

- Accès aux fichiers uniquement par le sélecteur système (`ACTION_OPEN_DOCUMENT`) et les intents `VIEW` / `SEND`.
- Interdits : `MANAGE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, `READ_MEDIA_*`, `QUERY_ALL_PACKAGES`, `SYSTEM_ALERT_WINDOW`, `POST_NOTIFICATIONS`, `WAKE_LOCK`.
- Aucun `Service`, aucun `WorkManager`, aucune notification.
- Aucune bibliothèque d'analytics, de suivi de plantage ou de publicité.
- Le contenu du livre n'est pas copié durablement. Seuls sont stockés : URI, titre, métadonnées des récents, position de lecture, marque-pages, réglages.

### Sécurité du contenu (voir `SPEC.md` §5, T-13)

Un EPUB est une archive de pages HTML, CSS et parfois JavaScript, fournie par un tiers. Il est traité comme un fichier hostile :

- **Aucun JavaScript** exécuté. Ne jamais activer l'exécution de scripts sans décision consignée.
- **Aucune ressource ni requête hors de l'archive** : pas de chargement d'image, de police ou de feuille de style depuis une URL.
- **Aucun accès direct au système de fichiers** depuis le moteur de rendu.
- **Aucune navigation vers l'extérieur** : les liens externes ne sont pas ouverts.
- **Archive validée avant lecture** : chemins contrôlés (pas de sortie de l'archive), tailles et nombre d'entrées bornés, XML sans entités externes ni DTD.
- **Aucune donnée du livre dans un cache système** : pas de cache, cookies ni stockage web de la WebView (si elle est retenue).
- Toute modification de ces règles exige une entrée dans `DECISIONS.md`, argumentée.

### Dépendances

- Toute nouvelle bibliothèque est **proposée** puis justifiée dans `DECISIONS.md` (contexte, alternative, licence, compatibilité GPL v3, effet sur le manifeste fusionné) **avant** d'être ajoutée. Ne jamais l'ajouter de sa propre initiative.
- Solution simple d'abord ; API Android intégrée de préférence à une bibliothèque (une WebView système plutôt qu'un moteur tiers, si la sécurité le permet).

## 5. Méthode

- Un point `[À DÉCIDER]` du `SPEC.md` n'est **jamais** implémenté comme s'il était tranché. Le signaler, proposer les options, attendre la décision consignée dans `DECISIONS.md`.
- Un point `[À VÉRIFIER]` se teste sur l'appareil ; proposer le protocole de test, ne pas présumer du résultat.
- Ne pas modifier `SPEC.md` pour l'aligner sur le code : signaler l'écart.
- Ne pas modifier `CHARTE.md` ; toute dérogation passe par `DECISIONS.md`.
- Ne pas cocher une tâche de `ROADMAP.md` : elle n'est cochée qu'après validation sur l'appareil par le porteur du projet.
- Signaler clairement les limites des API Android (notamment celles de la WebView selon la version d'Android et du composant système mis à jour par Google), les particularités de One UI et les contraintes du Play Store.
- Les fichiers de test (EPUB, y compris malveillants) restent **hors du dépôt**.

## 6. Interface (résumé de `CHARTE.md`)

- Fond `Black` partout ; texte `TextPrimary` ou `TextSecondary` ; aucune couleur d'accent ; aucune couleur en dur dans un écran.
- Texte du livre affiché avec les couleurs du thème de lecture (`Black` / `TextPrimary` en mode nuit ou en mode par défaut) ; les couleurs du livre sont ignorées par défaut.
- Texte seul : pas d'icône, pas de pictogramme, sauf décision consignée.
- Aucune animation décorative ; apparitions et disparitions immédiates ; pas d'indicateur de chargement animé.
- Tailles de texte de l'interface en `sp` ; cibles tactiles ≥ 48 dp ; libellé TalkBack pour tout élément interactif.
- Composants types de la charte (action en liste, fenêtre de menu, boîte de saisie, liste de réglages) réutilisés, pas réinventés.
- Toutes les chaînes dans `res/values/strings.xml` ; français, tutoiement, ton neutre ; libellés d'action à l'infinitif.
- Aucun plantage : toute erreur (fichier absent, droit retiré, EPUB mal formé, livre protégé, mémoire, archive suspecte) affiche un message en texte.

## 7. Code

- Identifiants et commentaires en **anglais**.
- Logique pure dans `domain/`, avec tests unitaires (position de lecture, progression, aplatissement de la table des matières, validation des chemins d'archive, limites de taille, politique des URL, réglages de taille du texte).
- Rendu des chapitres hors du fil principal ; un seul chapitre chargé dans le moteur à la fois, sauf décision contraire.
- Cache de chapitres ou d'images borné en octets ; libérer ce qui n'est pas visible.
- Aucun parseur XML ou HTML maison sans décision : préférer les API Android et des bibliothèques justifiées.

## 8. Commandes

```
./gradlew assembleDebug          # build
./gradlew testDebugUnitTest      # unit tests
./gradlew lint                   # lint
./gradlew installDebug           # install on connected device
```
Sous Windows : `gradlew.bat` au lieu de `./gradlew`.

## 9. Fin de tâche

1. Build, tests et lint sans erreur (pas de nouvelle erreur de lint).
2. Contrôle `aapt2 dump permissions` des permissions.
3. Contrôle de sécurité : relire la liste des règles de la section 4 (sécurité du contenu) et la section T-13 de `SPEC.md`.
4. Donner au porteur du projet la liste de contrôle à passer sur l'appareil (reprise de `ROADMAP.md`).
5. Proposer un message de commit, sans committer avant validation sur l'appareil.

## 10. Git et dépôt public

- Jamais versionnés : `*.jks`, `*.keystore` et leurs mots de passe, `local.properties`, informations personnelles, livres personnels ou sous droits d'auteur, EPUB de test non libres, captures de produits tiers.
- Avant chaque commit : `git status` et contenu indexé relus.
- Commit seulement après validation sur l'appareil ; adresse e-mail « noreply » de GitHub.
- Messages de commit en anglais, courts, à l'impératif (ex. `Add chapter pagination`).
- Pas de `git push`, pas de réécriture d'historique, pas de `--force` sans demande explicite.
- Ressources tierces seulement sous licence compatible (Google Fonts, Material Symbols, Apache 2.0, BSD…) ; origine et avis de copyright conservés.
