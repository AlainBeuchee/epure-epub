# Charte graphique et fonctionnelle — Épure EPUB

> Partie A : version 1.0 du 24 octobre 2026, reprise **sans modification** de la charte du launcher Épure (DEC-0010), telle qu'elle figure dans la charte d'Épure PDF. Le texte est recopié ci-dessous pour que ce projet se suffise à lui-même.
> Partie B : version 0.1 du 10 novembre 2026, propre à Épure EPUB (déclinaison de la Partie B d'Épure PDF).
> Partie C : démarrage d'un nouveau projet.
> Ce document ne remplace pas `SPEC.md`, qui reste le cahier des charges détaillé. En cas d'écart, `SPEC.md` et `DECISIONS.md` font foi ; l'écart est signalé et tranché.

---

## Partie A — Ce qui se reprend dans chaque projet

Ces règles sont **génériques** : elles valent pour tout nouveau projet Android de Alain, sauf décision explicite contraire consignée dans `DECISIONS.md`.

### A.1 Principes

1. **Sobriété visuelle** : fond uni, texte seul, aucune icône décorative, aucune animation superflue.
2. **Sobriété technique** : pas de service permanent sans nécessité ; données lues à la demande ; dépendances réduites au strict minimum.
3. **Confidentialité** : les données restent sur l'appareil. Pas de bibliothèque d'analytics, de suivi de plantage ni de publicité. Pas de permission `INTERNET` si le projet n'en a pas besoin strictement.
4. **Permissions justifiées** : chaque permission sensible correspond à une fonction précise, expliquée à l'utilisateur au moment où elle sert. Si elle est refusée, seule cette fonction est désactivée.
5. **Simplicité d'abord** : la solution simple avant la solution sophistiquée.
6. **Respect du système** : ne pas dupliquer ni contrarier les outils natifs de l'appareil (ici, les outils Samsung / One UI).

### A.2 Identité d'un projet

| Élément | Règle | Exemple (Épure) |
|---|---|---|
| Nom affiché | Avec accents si la langue le permet, dans `strings.xml` (`app_name`) | « Épure » |
| Nom technique | Sans accent ni espace (projet, modules, classes) | `Epure` |
| `applicationId` | Espace de nom inversé, stable dès le premier build | `com.alain.epure` |
| Dépôt Git | Nom court, en minuscules, avec tirets | `epure-launcher` |
| Licence | GPL v3 pour les projets publics | GPL v3 |
| Marques | Ne jamais utiliser le nom d'une marque tierce (nom, dépôt, paquet, fiche de store) ni suggérer une affiliation | — |

### A.3 Pile technique figée

| Domaine | Choix |
|---|---|
| Langage | Kotlin uniquement (pas de code natif, pas de Rust, pas de NDK sauf décision contraire) |
| Interface | Jetpack Compose, Material 3, thème personnalisé |
| `minSdk` | 31 (Android 12) |
| `targetSdk` / `compileSdk` | 36 (Android 16) |
| Architecture | Une seule activité ; écrans en Compose ; `ViewModel` + `StateFlow` |
| Injection | Pas de framework : une classe `AppContainer` instancie les dépendances à la main |
| Navigation | État simple géré à la main (3 à 4 écrans) ; pas de `navigation-compose` tant que ce n'est pas nécessaire |
| Persistance | DataStore (Preferences) ; Room seulement si les données deviennent réellement structurées |
| Logique métier | Dans un paquet `domain/`, **sans dépendance Android**, couverte par des tests unitaires |
| Paquets | `ui/` (écrans), `data/` (dépôts, persistance), `system/` (accès aux API Android), `domain/` (logique pure) |
| Sauvegarde Android | Désactivée (`allowBackup` à `false`) pour un outil qui ne stocke que des préférences locales |

**Règle de dépendance** : toute nouvelle bibliothèque est justifiée dans `DECISIONS.md` (contexte, alternative, raison) **avant** d'être ajoutée au projet.

### A.4 Contraintes Android à anticiper dès le départ

- **Edge-to-edge imposé** (`targetSdk` 36) : gérer les marges avec `WindowInsets`.
- **Retour prédictif** : utiliser `BackHandler` (Compose) ou `OnBackPressedCallback`, jamais `onBackPressed()`.
- **Visibilité des paquets** (Android 11+) : lister les apps impose une déclaration `<queries>` ; `QUERY_ALL_PACKAGES` est interdite.
- **Permissions interdites par défaut** dans le manifeste : `INTERNET`, `SYSTEM_ALERT_WINDOW`, `QUERY_ALL_PACKAGES`. Toute autre permission doit correspondre à une ligne d'un tableau des permissions dans `SPEC.md`.
- **Paramètres restreints** (Android 13+) : une app installée hors Play Store peut voir l'accessibilité ou l'écoute des notifications bloquées ; le noter dans le README dès qu'un service de ce type est prévu.
- **Services d'accessibilité** : `canRetrieveWindowContent="false"` sauf nécessité, événements limités au minimum, aucun enregistrement.

### A.5 Charte visuelle de base

#### Couleurs

Thème **sombre uniquement**, sans couleur dynamique, sans thème clair.

| Jeton | Valeur | Usage |
|---|---|---|
| `Black` | `#000000` | Fond de tous les écrans, fond des fenêtres |
| `TextPrimary` | `#FFFFFF` | Texte principal, éléments actifs, titres, curseur |
| `TextSecondary` | `#999999` | Texte secondaire, libellés, éléments inactifs, filets, liserés (DEC-EE-0031) |

- Aucune couleur d'accent. L'information se transmet par la taille, la graisse et le niveau de gris.
- Éléments inactifs : gris clair (`TextSecondary`), **lisibles** (jamais invisibles).
- Les couleurs sont déclarées une fois, comme jetons dans `ui/theme/Color.kt`, puis reprises dans `darkColorScheme` (`ui/theme/Theme.kt`). Aucune couleur écrite en dur dans un écran.
- Fond : toujours `Black`, y compris dans `res/values/themes.xml` (`android:windowBackground`) pour éviter un flash blanc au démarrage.
- Exception : la page du livre suit le thème de lecture choisi par l'utilisateur (B.4, DEC-EE-0026). L'interface reste sur `Black`.

#### Typographie

- Police système (`FontFamily.Default`). Aucune police tierce par défaut.
- Graisses utilisées : `Light` (très grand affichage uniquement), `Normal`. Pas de gras, sauf nécessité.
- **Toutes les tailles sont en `sp`** : elles suivent le réglage de taille de police du téléphone. Jamais en `dp` pour du texte.
- Échelle utilisée dans Épure (`ui/theme/Type.kt`) :

| Rôle | Taille | Graisse | Style Compose |
|---|---|---|---|
| Heure (accueil) | 120 sp | Light | `ClockStyle` |
| Date (accueil) | 20 sp | Normal | `DateStyle` |
| Nom d'app, titre d'écran, titre de fenêtre, champ de recherche | 28 sp | Normal | `AppNameStyle` |
| Libellé, option secondaire, texte d'aide, bouton texte | 20 sp | Normal | `LabelStyle` |
| Corps de texte (rarement utilisé) | 16 sp | Normal | `Typography.bodyLarge` |

- Interligne généreux pour les textes courants : 1,3 à 1,4 fois la taille (28 sp / 36 sp ; 20 sp / 28 sp). L'heure seule a un interligne égal à sa taille (120 sp).
- Un nom trop long **passe à la ligne**, il n'est pas tronqué.

#### Espacements et géométrie

| Élément | Valeur |
|---|---|
| Marge horizontale des écrans | 24 dp (accueil : 16 dp pour les raccourcis) ; Épure EPUB : 16 dp (Paramètres, accueil) |
| Marge horizontale du texte du livre | 10 dp (Épure EPUB, DEC-EE-0027) |
| Marge verticale du haut d'écran | 24 dp |
| Espace entre un titre et son premier élément | 24 dp |
| Espace entre un bloc et son libellé | 16 dp à 32 dp |
| Hauteur minimale d'une cible tactile | 48 dp |
| Rayon des fenêtres (dialogue, menu) | 16 dp |
| Épaisseur des liserés et filets | 1 dp, couleur `TextSecondary` |

#### Iconographie

- **Par défaut, pas d'icône, pas de pictogramme** : loupe, roue dentée, flèches décoratives sont exclus. Les libellés sont du texte.
- Exception documentée : les icônes de raccourcis de l'accueil (Material Symbols, licence Apache 2.0, licence conservée dans chaque fichier `.xml`). Toute autre exception passe par `DECISIONS.md`.
- Les icônes conservées sont en `TextPrimary`, sans fond ni cadre, avec une zone tactile d'au moins 48 dp.

#### Mouvement

- Aucune animation décorative. Pas de transition d'apparition, pas de rebond.
- Un défilement natif, un changement d'écran immédiat, c'est tout.

### A.6 Composants types

Ces composants se réutilisent d'un projet à l'autre. Leur style doit rester identique.

| Composant | Description | Référence dans Épure |
|---|---|---|
| **Action en liste** | Texte seul, `AppNameStyle`, `TextPrimary`, hauteur minimale 48 dp, cible pleine largeur | `ActionRow.kt` |
| **Action secondaire** | Texte `LabelStyle` en `TextSecondary`, cible ≥ 48 dp | `TextAction` dans `ActionRow.kt` |
| **Champ de recherche** | Texte seul, sans cadre ni loupe ; une ligne soulignée implicite ; curseur `TextPrimary` ; clavier ouvert à l'arrivée sur l'écran | `AppListScreen.kt` |
| **Fenêtre de menu** | Centrée, fond `Black`, liseré 1 dp `TextSecondary`, rayon 16 dp, titre en `TextPrimary`, filet, actions en liste ; fond de l'écran assombri ; fermeture par Retour ou toucher à l'extérieur. Variante Épure EPUB : panneau ancré au-dessus de la barre basse, pleine largeur, sans liseré, texte aligné à gauche, tailles de la page Paramètres (DEC-EE-0023, DEC-EE-0034) | `AppActionsMenu.kt` |
| **Barre basse** | Filet 1 dp `TextSecondary`, commandes texte d'au moins 48 dp réparties sur une ligne, libellé court (symbole ou mot) et nom complet pour les lecteurs d'écran (DEC-EE-0030, reprise d'Épure Notes) | `CommandButton.kt` |
| **Boîte de saisie** | Même fenêtre que le menu, champ de saisie en `AppNameStyle`, deux actions texte | `RenameDialog.kt` |
| **Écran d'autorisation** | Plein écran, texte seul : titre, pourquoi, ce que l'app ne fait pas, consignes numérotées, bouton « Ouvrir le réglage », mention de révocation, bouton « Plus tard » | `PermissionScreen.kt` |
| **Index alphabétique** | Colonne verticale de lettres à droite de la liste, `TextSecondary`, largeur 40 dp, seules les lettres présentes, masquée pendant une recherche | `LetterIndex.kt` |
| **Liste de réglages** | Titre (`titleLarge`, 22 sp), lignes en `bodyLarge` (16 sp), phrase explicative en 16 sp italique `TextSecondary`, défilement vertical (DEC-EE-0031, reprise d'Épure Notes) | `SettingsScreen.kt` |
| **Séparateur** | `HorizontalDivider` de 1 dp en `TextSecondary` | menu et boîte de saisie |

#### États d'un élément

| État | Rendu |
|---|---|
| Actif | `TextPrimary` |
| Inactif ou indisponible | `TextSecondary`, reste visible, explication disponible au toucher |
| Pendant une action | Pas d'indicateur animé ; le résultat apparaît à la fin |
| Vide | Une phrase courte en `TextSecondary`, sans encadré ni illustration (ex. « Aucune app ») |

### A.7 Textes et langue

- **Tous les textes d'interface** sont dans `res/values/strings.xml`. Aucune chaîne en dur dans le code Kotlin.
- Langue par défaut : **français**. L'anglais peut être ajouté en seconde langue.
- **Tutoiement** dans l'interface.
- Ton **factuel et neutre** : pas de promesse chiffrée, pas de formule promotionnelle, pas de culpabilisation.
- Phrases courtes. Libellés à l'infinitif pour les actions (« Renommer », « Ouvrir le réglage »). Majuscule en début de phrase seulement.
- Les libellés des réglages système reprennent **exactement** le texte affiché par Android ou One UI, entre guillemets (ex. « Données d'utilisation » → « Octroi d'autorisation »).
- Les commentaires de code et les identifiants sont en **anglais**.

### A.8 Accessibilité (minimum)

- Tailles en `sp` ; l'interface doit rester utilisable avec la taille de police système agrandie.
- Cibles tactiles ≥ 48 dp.
- Contraste élevé : texte clair sur fond noir.
- Chaque élément interactif a un libellé lisible par TalkBack (`contentDescription` ou texte). Les lettres de l'index sont annoncées comme actions (« Aller à la lettre M »).
- Un écran qui échoue (permission retirée, donnée absente) affiche un message, sans plantage.

### A.9 Gestes et navigation (règles communes)

- **Bouton Retour** : ferme d'abord le premier niveau ouvert (fenêtre, clavier, liste) ; sur l'écran d'accueil d'un launcher, il ne fait rien.
- **Bouton Accueil** (launcher) : ramène toujours à l'état initial de l'écran d'accueil.
- **Geste de navigation** : un balayage clair, avec un seuil de distance (ici 100 dp), pour éviter les déclenchements involontaires lors d'un simple toucher. Un libellé texte est toujours disponible en plus du geste, pour ne pas dépendre du seul geste.
- **Appui court** : action principale (ouvrir, valider). **Appui long** : menu d'actions sur l'élément.
- Pas de fenêtre de confirmation pour une action réversible ; confirmation pour une action irréversible. Exception : la demande d'ajout de marque-page ouverte par « * » dans la barre de commandes (DEC-EE-0040).

### A.10 Permissions et autorisations spéciales

Modèle commun, à réutiliser tel quel :

1. La demande **apparaît au moment où la fonction est utilisée**, jamais dans un assistant au premier lancement.
2. L'écran d'autorisation (A.6) explique : la fonction, pourquoi, ce qu'Épure ne fait pas, les consignes exactes, le bouton d'ouverture du réglage, la mention « Révocable à tout moment », le bouton « Plus tard ».
3. Au retour du réglage système, l'autorisation est **vérifiée automatiquement** ; l'écran se ferme si elle est accordée.
4. Un refus ne désactive que la fonction concernée ; le reste de l'app fonctionne.
5. Aucune illustration ni capture du réglage système (sobriété, et pas de visuels tiers dans le dépôt).

### A.11 Méthode de travail et documents

Chaque projet contient un dossier `docs/` avec :

| Fichier | Contenu |
|---|---|
| `SPEC.md` | Cahier des charges : exigences `F-x.y` (fonctions) et `T-n` (transversales), critères d'acceptation, tableau des permissions, points ouverts `[À DÉCIDER]` |
| `ROADMAP.md` | Jalons et tâches cochables ; une tâche n'est cochée qu'une fois **validée sur l'appareil** |
| `DECISIONS.md` | Journal des décisions `DEC-xxxx` (contexte, options, décision, justification, réversibilité) ; les entrées ne sont jamais effacées |
| `CHARTE.md` | Le présent document |
| `CLAUDE.md` (racine) | Consignes pour l'assistant de code : lire les documents avant toute tâche, ne travailler que sur le jalon en cours |

Règles de méthode :

- **Un point `[À DÉCIDER]` n'est jamais implémenté comme s'il était tranché.** Il est d'abord consigné dans `DECISIONS.md`, puis retiré du `SPEC.md`.
- **Un point `[À VÉRIFIER]` se teste sur l'appareil** ; le résultat est consigné.
- **Ne pas modifier `SPEC.md` pour l'aligner sur le code** : l'écart est signalé et tranché.
- Une session de travail par jalon ou par tâche.
- Ne travailler que sur le jalon en cours : pas d'anticipation des jalons suivants.

### A.12 Dépôt public et Git

- Jamais versionnés : clé de signature (`*.jks`, `*.keystore`) et ses mots de passe, `local.properties`, informations personnelles, captures ou visuels de produits tiers.
- `LICENSE` présent dès le premier commit ; `.gitignore` Android vérifié avant le premier envoi.
- Avant chaque commit : `git status` et contenu indexé relus.
- **Commit seulement après validation sur l'appareil.** Commit avec l'adresse e-mail « noreply » de GitHub.
- Messages de commit en **anglais**, courts, à l'impératif (ex. `Add text-only app list`).
- Pas de `git push`, pas de réécriture d'historique, pas de force sans demande explicite.
- Ressources tierces seulement sous licence compatible avec la redistribution (Google Fonts, Material Symbols, Apache 2.0 ou équivalent). Code repris d'un projet open source : licence du fichier vérifiée, avis de copyright conservés, origine mentionnée en commentaire et dans « À propos ».
- README : mention factuelle si le projet s'inspire d'une autre app (« inspiré de … »), sans logo ni suggestion d'affiliation ; section Confidentialité ; procédure d'activation des permissions spéciales.

### A.13 Contrôles avant de déclarer une tâche terminée

1. `./gradlew assembleDebug` sans erreur.
2. `./gradlew testDebugUnitTest` au vert.
3. `./gradlew lint` sans **nouvelle** erreur.
4. Vérification sur l'appareil de test (Samsung, Android 16 / One UI 8.0) selon une liste de contrôle écrite dans `ROADMAP.md`.
5. Cocher la tâche dans `ROADMAP.md` seulement après validation.

---

## Partie B — Épure EPUB

Cette partie décrit ce qu'Épure EPUB fait et comment la Partie A s'applique à un lecteur de livres au format EPUB. Les exigences détaillées sont dans `SPEC.md` (références entre parenthèses). Tout ce qui est marqué **[À DÉCIDER]** n'est pas tranché.

### B.1 Objet

Lecteur d'EPUB Android minimaliste, entièrement hors ligne, **sûr à ouvrir** : lire sans distraction, sans réseau, sans collecte de données, et sans exécuter de code venu du livre. Usage principal : romans, essais, documents techniques au format EPUB. Usage personnel en priorité ; code public ; publication sur le Play Store éventuelle, non prioritaire.

Un livre EPUB est traité comme **un fichier venu de l'extérieur, potentiellement hostile**. Cette position est le fondement de la Partie B.11 et des exigences S-1 à S-7 de `SPEC.md`.

### B.2 Identité (DEC-EE-0001, à consigner)

| Élément | Valeur |
|---|---|
| Nom affiché | « Épure EPUB » |
| Nom technique | `EpureEpub` |
| `applicationId` | `com.alain.epure.epub` (proposition) |
| Dépôt Git | `epure-epub` |
| Licence | GPL v3 |
| Relation avec Épure PDF | App distincte ; aucune dépendance de code ; cohérence visuelle seulement (DEC-0037) |
| Marques | Aucun nom, logo ou visuel d'une marque tierce ; « EPUB » employé comme nom de format ouvert |

### B.3 Écrans

| Écran | Rôle | Accès | Jalon |
|---|---|---|---|
| Accueil | Titre « Liste », livres récents, barre basse : « Ajout » (au milieu), « Paramètres » (à droite) | Lancement de l'app | 1, 2 |
| Lecteur | Texte du livre, commandes au toucher | Ouverture d'un livre | 1 |
| Table des matières | Liste hiérarchique des titres | Commandes | 4 |
| Marque-pages | Liste des passages marqués | Commandes | 4 |
| Recherche | Champ et navigation entre résultats | Commandes | 4 |
| Paramètres | Liste des livres, latéralité, thème du texte, taille du texte, « À propos » | « Paramètres » sur l'accueil | 2, 3 |

- Accueil : barre basse avec un filet ; « Ajout » au milieu ouvre un fichier, « Paramètres » à droite ouvre les réglages (DEC-EE-0030). Cette barre reprend l'esthétique de la barre de Épure Notes.
- Pas d'écran d'autorisation (A.6, A.10) : l'app ne demande aucune permission (B.10).
- Pas d'écran de mot de passe : les livres protégés ne sont pas ouverts (SPEC §1.3, F-15.1).

### B.4 Application de la charte visuelle au lecteur

#### Texte et fond

| Élément | Règle |
|---|---|
| Fond de la page du livre | Couleur du thème de lecture (DEC-EE-0026) : Jour `#F8F7F4`, Sépia `#F4ECD8`, Nuit (ardoise) `#1E1F22`, Nuit (AMOLED) `#121212`, Nuit (noir) `#000000` (défaut) |
| Texte du livre | Couleur de texte du thème : Jour `#222222`, Sépia `#3B3126`, Nuit (ardoise) `#D4D4D4`, Nuit (AMOLED) `#C4C4C4`, Nuit (noir) `#E6E6E6` (défaut). Imposée au contenu par styles (DEC-EE-0026) |
| Couleurs du livre (fonds, polices, couleurs de texte définis par le livre) | **Ignorées** (proposition **[À DÉCIDER]**, SPEC F-3.3). Elles ne sont pas une « couleur de page » comme dans un PDF : le texte est recomposé par l'app |
| Titres du livre | Même couleur que le texte, graisse normale (pas de gras imposé) |
| Liens dans le texte | `TextPrimary`, soulignés (le soulignement est la seule marque de lien) |
| Images du livre | Affichées telles quelles (proposition **[À DÉCIDER]**, SPEC F-8.3). En mode nuit, aucune inversion. |
| Image indisponible | « Image indisponible » en `TextSecondary`, centré dans le cadre de l'image |
| Chapitre en cours de chargement | Écran `Black` uni, sans indicateur animé (A.6, « Pendant une action ») |
| Chapitre illisible | « Ce chapitre ne peut pas être affiché. » en `TextSecondary`, à sa place |

#### Typographie propre à Épure EPUB

L'échelle de A.5 s'applique à l'interface. Le texte du livre a sa propre règle : sa taille est choisie par l'utilisateur (paliers, SPEC F-3.1) et exprimée en `sp` pour suivre le réglage de police du téléphone.

| Élément | Style |
|---|---|
| Titre de l'accueil « Liste » | 24 sp, `TextPrimary` (DEC-EE-0024) |
| Phrase sous le titre | 16 sp, italique, `TextSecondary` |
| Titre du livre dans les récents | `LabelStyle`, `TextPrimary` (dérogation, mieux adaptée aux titres longs ; à consigner, voir B.4 « Dérogations ») |
| Seconde ligne d'un récent (« 42 % lu ») | `LabelStyle`, `TextSecondary` |
| Titre du livre dans les commandes | `LabelStyle`, `TextPrimary` |
| Progression « 42 % » | `LabelStyle`, `TextSecondary` |
| Actions des commandes | Action secondaire, `TextPrimary` quand disponible, `TextSecondary` quand indisponible |
| Symboles de la barre | « < », « > » : libellés courts, avec nom complet pour les lecteurs d'écran (DEC-EE-0030) |
| Texte du livre | Police système sans empattement (DEC-EE-0011), taille de 12 à 24 sp (défaut 16 sp, DEC-EE-0027), interligne 1,4, largeur de ligne plafonnée à 75 caractères (DEC-EE-0024) |
| Titres de la table des matières | `LabelStyle`, `TextPrimary` ; retrait de 16 dp par niveau |

#### Plein écran

- Lecture en mode immersif : barres d'état et de navigation masquées.
- Les barres système réapparaissent avec les commandes, ou par balayage depuis le bord.

#### Iconographie

Aucune icône. Toutes les actions du lecteur sont des libellés texte (« Table », « Rechercher », « Plus », « Taille du texte », « Thème du texte », « Reste allumé pendant la lecture », « Partager », « Retour au passage »). Exception textuelle : les symboles « < », « > » et « * » de la barre de commandes (DEC-EE-0021, DEC-EE-0040).

#### Dérogations possibles, non tranchées

| Sujet | Problème | Référence |
|---|---|---|
| Titre du livre dans les récents | Titres longs ; `LabelStyle` au lieu de `AppNameStyle` | `SPEC.md` F-4.1 |
| Signalement des résultats de recherche | Négatif de l'occurrence, sans couleur d'accent (comme Épure PDF) | `SPEC.md` F-11.4 |
| Couleurs du livre | Ignorées par défaut : le livre ne décide pas de l'apparence (thèmes de lecture à la place) | `SPEC.md` F-3.3, DEC-EE-0026 |
| Symboles « < », « > » | Icônes textuelles, faute de libellé plus court dans la barre | DEC-EE-0021 |

Toute dérogation retenue est consignée dans `DECISIONS.md` puis reportée ici.

### B.5 Composants propres à Épure EPUB

À créer dans ce projet, en respectant les composants types de A.6 :

| Composant | Description | Exigences |
|---|---|---|
| **Vue de lecture** | Texte recomposé, marges de 10 dp, interligne 1,4, largeur de ligne ≤ 75 caractères ; défilement continu (DEC-EE-0016) | F-2, F-3 (DEC-EE-0024, DEC-EE-0027) |
| **Barre de commandes** | Fond `Black`, filet 1 dp, texte seul ; pour un gaucher (latéralité « gauche », DEC-EE-0032), côté et ordre inversés ; en haut le titre du livre ; en bas, de gauche à droite : « Table » (désactivée jusqu'à la session 4a), progression, « < » et « > » (chapitre précédent et suivant), « * » (demande d'ajout de marque-page), « Plus » ; apparition et disparition immédiates | F-6 (DEC-EE-0021, DEC-EE-0040) |
| **Indicateur de progression** | « 42 % », dans la barre de commandes, non touchable en version 1 (pas de saut à un pourcentage) | F-7 |
| **Ligne de récent** | Titre du livre (passage à la ligne, pas de troncature) et seconde ligne en `TextSecondary` ; appui court = ouvrir, appui long = menu | F-4 |
| **Liste hiérarchique** | Table des matières, retrait par niveau, texte seul | F-12 |
| **Ligne de résultat** | Extrait ou titre de chapitre, compteur, actions « Précédent » et « Suivant » | F-11 |

Composants de A.6 réutilisés tels quels : action en liste, action secondaire, champ de recherche, fenêtre de menu, boîte de saisie (si nécessaire), liste de réglages, séparateur. L'index alphabétique et l'écran d'autorisation ne servent pas.

### B.6 Gestes et navigation

| Geste | Où | Effet |
|---|---|---|
| Appui court | Récent | Ouvrir à la position mémorisée |
| Appui long | Récent | Menu : « Retirer de la liste », « Partager » |
| Appui court dans la bande basse (environ 2 cm) | Texte | Affiche ou masque les commandes (DEC-EE-0021) |
| Glisser vers le bas | Texte | Remonte dans le texte et affiche les commandes (DEC-EE-0028) |
| Balayage gauche-droite | Texte | Chapitre suivant ou précédent (DEC-EE-0019) |
| Défilement vertical | Texte | Défilement natif ; ne change pas l'état des commandes (DEC-EE-0028) |
| Toucher sur un lien interne | Texte | Déplace la lecture ; « Retour au passage » disponible dans les commandes |
| Toucher sur un lien externe | Texte | Message « Lien externe non ouvert » ; aucune action réseau |
| Retour | Lecteur | Ferme la fenêtre ouverte, puis masque les commandes, puis quitte le lecteur (accueil, ou app d'origine si ouverture depuis une autre app) |
| Retour | Accueil | Quitte l'app (comportement Android standard) |

- Les changements de chapitre ont une action texte équivalente dans la barre : « < » et « > » (A.9, DEC-EE-0021).
- Pas de double toucher de zoom : le texte est recomposé, la taille se règle dans le menu « Plus » et dans les Paramètres (DEC-EE-0030).
- Pas de masquage automatique par délai : les commandes restent visibles jusqu'à un appui sur le texte ou dans la bande basse (DEC-EE-0021, DEC-EE-0028). La proposition de 4 s n'est pas retenue.

### B.7 Textes propres

Libellés de référence (à reprendre dans `strings.xml`) :

| Contexte | Texte |
|---|---|
| Accueil | « Liste », « Aucun livre récent », « Ajout », « Paramètres » |
| Menu d'un récent | « Retirer de la liste », « Partager » |
| Commandes | « Table », « Rechercher », « * », « Plus », « < », « > » |
| Menu « Plus » | « Taille du texte : … sp », « Thème du texte : … », « Reste allumé pendant la lecture : oui / non » (DEC-EE-0042) |
| Fenêtre « * » | « Ajouter marque-page à cet endroit », « Liste des marque-pages », « Retour au passage », « Partager », « Annuler » (DEC-EE-0040, DEC-EE-0042) |
| Tailles | « Taille du texte : … sp » (un appui = taille suivante, DEC-EE-0033) |
| Liens | « Lien externe non ouvert : Épure EPUB ne se connecte pas à Internet. », « Retour au passage » |
| Listes vides | « Aucun marque-page », « Aucune table des matières », « Aucun résultat » |
| Erreurs | « Fichier introuvable. », « Accès au fichier retiré. », « Ce fichier n'est pas un EPUB lisible. », « Ce livre ne contient pas de texte lisible. », « Ce livre est protégé et ne peut pas être ouvert. », « Ce livre est trop volumineux ou mal construit pour être ouvert. », « Ce chapitre ne peut pas être affiché. », « Image indisponible », « Ce livre ne contient pas de texte. » |
| Réglages | « Liste des livres : activée / désactivée », « Thème du texte : … », « Taille du texte : … sp », « À propos », « Retour » |

Les messages d'erreur exacts restent ajustables au jalon concerné ; le ton reste neutre et factuel (A.7).

### B.8 Fonctions

| Fonction | Détail | Jalon | État |
|---|---|---|---|
| Ouverture par le sélecteur | `ACTION_OPEN_DOCUMENT`, `application/epub+zip`, autorisation persistante | 1 | Prévu |
| Lecture du texte recomposé | Pagination ou défilement selon décision ; marges et interligne | 1 | Prévu |
| Sécurité du contenu | Pas de script, pas de ressource distante, liens internes seulement, archive validée | 1, 5 | Prévu |
| Ouverture depuis une autre app | Intents `VIEW` et `SEND`, `application/epub+zip` | 2 | Prévu |
| Récents | Dernier livre ; liste facultative | 2 | Prévu |
| Reprise de lecture | Position mémorisée (ancre) | 2 | Prévu |
| « À propos » | Version, licence, dépôt, confidentialité et sécurité | 2 | Prévu |
| Commandes au toucher | Masquage automatique | 3 | Prévu |
| Taille du texte | Cinq paliers, conservation du passage | 3 | Prévu |
| Mode nuit | Styles imposés au contenu | 3 | Prévu |
| Écran allumé | Option, limitée au lecteur | 3 | Prévu |
| Partage | Partage système, sans réseau propre | 3 | Prévu |
| Table des matières | `nav` ou `NCX` | 4 | Prévu |
| Marque-pages | Passages par ancre, par livre | 4 | Prévu |
| Recherche de texte | Portée selon décision, sans stockage du texte | 4 | Prévu |
| Protection des archives | Limites, zip slip, bombes, entités XML | 5 | Prévu |
| Gros livres | Un chapitre à la fois, mémoire bornée | 5 | Prévu |
| DRM | Non pris en charge | — | **Écarté** (SPEC §1.3) |
| Mise en page fixe | — | — | **[À DÉCIDER]** (SPEC §9.1) |

Moteur de rendu : **[À DÉCIDER]** au jalon 1 (`SPEC.md` §6).

### B.9 Fonctions écartées

- Livres protégés par DRM (Adobe ADEPT, LCP) : ni ouverture ni déchiffrement.
- Annotations, surlignage, notes, édition, synchronisation cloud, lecture à voix haute.
- Dictionnaire, traduction, recherche sur le web, achat de livres.
- Exécution de JavaScript du livre.
- Lecture audio et vidéo intégrées.

### B.10 Permissions

| Permission / accès | Fonction | Obtention |
|---|---|---|
| Sélecteur système | Ouvrir un livre | Choix de l'utilisateur |
| Autorisation persistante sur une URI | Rouvrir le dernier livre | Accordée lors du choix ; libérée au retrait de la liste |
| Filtres d'intent `VIEW` et `SEND` | Ouverture depuis une autre app | Droit temporaire fourni par l'app d'origine |
| `FLAG_KEEP_SCREEN_ON` | Écran allumé | Aucune permission |
| `INTERNET`, `ACCESS_NETWORK_STATE`, `MANAGE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, `READ_MEDIA_*`, `POST_NOTIFICATIONS`, `WAKE_LOCK`, `QUERY_ALL_PACKAGES`, `SYSTEM_ALERT_WINDOW` | — | **Non déclarées** |

Objectif : **aucune permission déclarée** dans le manifeste fusionné. Le composant de rendu (si WebView) n'est pas une permission de l'app et ne doit pas en ajouter.

### B.11 Garanties de confidentialité et de sécurité

**Confidentialité** (reprise d'Épure PDF) :
- Aucune donnée ne quitte le téléphone.
- Pas de permission `INTERNET` : garantie vérifiable dans le manifeste.
- L'app n'accède qu'aux fichiers choisis par l'utilisateur.
- Le contenu du livre n'est pas copié ; seuls sont conservés le titre, la progression, la position, les marque-pages et les réglages.
- Aucune statistique de lecture, aucun historique au-delà de la liste des récents.
- Retirer un livre de la liste efface toutes ses données ; le fichier n'est jamais modifié ni supprimé.

**Sécurité du contenu** (propre à l'EPUB) :
- **Le livre n'est pas exécuté.** Aucun script ne tourne (S-1).
- **Le livre n'appelle rien à l'extérieur.** Aucune ressource ni requête hors de l'archive (S-2) ; aucun lien externe ouvert (S-3).
- **L'archive est vérifiée avant lecture.** Chemins contrôlés, tailles et nombre d'entrées bornés, rapport de compression limité, XML sans entités externes (S-4, S-5).
- **Le moteur ne garde rien.** Pas de cache, cookies ni stockage web du contenu (S-6).
- **Le moteur n'accède à aucun fichier de l'app** (S-7).
- Un livre hostile peut au pire afficher un message d'erreur ; il ne peut pas lire d'autres données de l'app ni accéder au réseau.

Ces garanties sont décrites au public dans le README et « À propos », en termes simples et sans promesse chiffrée.

### B.12 Jalons et état d'avancement (10 novembre 2026)

| Jalon | Contenu | État |
|---|---|---|
| 0 | Mise en place du projet, dépôt, licence, documents | À faire (documents en cours de rédaction) |
| 1 | Lecteur de base : choix du moteur, ouverture, texte, sécurité minimale | À faire |
| 2 | Ouverture depuis d'autres apps, récents, reprise, « À propos » | À faire |
| 3 | Commandes, taille du texte, thèmes de lecture, écran allumé, partage (mode nuit retiré) | En cours (3a et 3b implémentées, à valider) |
| 4 | Table des matières, marque-pages, recherche | À faire |
| 5 | Robustesse et sécurité : archives malveillantes, mémoire | À faire |

---

## Partie C — Démarrer un nouveau projet avec cette charte

Liste à suivre dans l'ordre :

1. Créer le projet Android Studio : Kotlin, Compose, `minSdk` 31, `targetSdk`/`compileSdk` 36, `applicationId` au format `com.alain.<nom>`.
2. Renseigner l'identité (A.2) : `app_name`, nom technique, dépôt, licence.
3. Copier le thème : `Color.kt` (trois jetons), `Theme.kt` (schéma sombre), `Type.kt` (échelle en `sp`), `themes.xml` (fond noir).
4. Créer les composants types (A.6) avant les écrans : liste en texte, fenêtre de menu.
5. Créer `docs/` avec `SPEC.md`, `ROADMAP.md`, `DECISIONS.md` (modèle d'entrée en tête) et `CLAUDE.md` à la racine.
6. Rédiger la **Partie B** pour le nouveau projet (écrans, fonctions, permissions, garanties, sécurité du contenu).
7. Vérifier le manifeste : aucune permission hors tableau de `SPEC.md`.
8. Mettre en place `.gitignore`, `LICENSE`, puis le premier build, les tests et le lint.
9. Consigner toute dérogation à cette charte dans `DECISIONS.md` ; ne pas la modifier silencieusement.

---

## Historique

- Partie A 1.0 (24/10/2026) : reprise sans modification de la charte du launcher Épure (DEC-0010).
- Partie B 0.1 (10/11/2026) : déclinaison pour Épure EPUB, à partir de la Partie B 0.1 d'Épure PDF et de `SPEC.md` 0.1 d'Épure EPUB. Non validée par le porteur.
- Partie B 0.2 (12/11/2026) : consignation des décisions DEC-EE-0021 à DEC-EE-0031 (barre de commandes, panneau « Plus », accueil et Paramètres, thèmes de lecture, marges et tailles, gestes, couleur secondaire). Modifiée par l'assistant sur demande explicite du porteur, alors que `CLAUDE.md` §5 réserve la charte au porteur. `SPEC.md` n'est pas modifié à ce stade.
- Partie B 0.3 (13/11/2026) : barre de commandes avec « * » et sa demande d'ajout de marque-page (DEC-EE-0040), exception à la règle des confirmations. Modifiée par l'assistant sur demande explicite du porteur.
