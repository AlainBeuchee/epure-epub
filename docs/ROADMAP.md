# Feuille de route — Épure EPUB

> Version 0.1 — 10 novembre 2026. Déclinaison de la feuille de route d'Épure PDF.
> Règle : une tâche n'est cochée qu'**après validation sur l'appareil** par le porteur du projet (`CHARTE.md` A.13). Aucune case n'est cochée à la rédaction de ce document.
> Références : `SPEC.md` (exigences), `DECISIONS.md` (décisions, à créer au jalon 0), `CHARTE.md` (règles visuelles et de sécurité).

---

## État d'avancement

| Jalon | Contenu | État |
|---|---|---|
| 0 | Mise en place du projet, dépôt, licence, documents | À faire |
| 1 | Lecteur de base : choix du moteur (prototype), ouverture par le sélecteur, texte, pagination, sécurité minimale | À faire |
| 2 | Ouverture depuis d'autres apps, récents, reprise, « À propos » | À faire |
| 3 | Confort : commandes, taille du texte, mode nuit, écran allumé, partage | À faire |
| 4 | Table des matières, marque-pages, recherche | À faire |
| 5 | Robustesse et sécurité : archives malveillantes, EPUB mal formés, mémoire | À faire |
| — | Livres protégés par DRM, mise en page fixe, autres formats | Hors feuille de route |

---

## Contrôles communs à toutes les tâches (`CHARTE.md` A.13)

- [ ] `./gradlew assembleDebug` sans erreur.
- [ ] `./gradlew testDebugUnitTest` au vert.
- [ ] `./gradlew lint` sans nouvelle erreur.
- [ ] `aapt2 dump permissions app/build/outputs/apk/debug/app-debug.apk` : aucune permission, sauf celles de `SPEC.md` §8.
- [ ] Relecture des règles de sécurité du contenu (`CHARTE.md` B.11, `SPEC.md` S-1 à S-7).
- [ ] Vérification sur l'appareil (Samsung SM-A536B, Android 16 / One UI 8.0).

---

## Jalon 0 — Mise en place

- [x] Créer le dépôt `epure-epub` (public), copier `SPEC.md`, `ROADMAP.md`, `CHARTE.md` dans `docs/` et `CLAUDE.md` à la racine, comme indiqué dans `CHARTE.md` A.11.
- [x] Créer `docs/DECISIONS.md` (modèle d'entrée en tête) et y consigner les décisions de cadrage : identité (DEC-EE-0001 : `applicationId` `com.alain.epure.epub`), licence GPL v3, relation avec Épure PDF.
- [x] Projet Android Studio : Kotlin, Compose, `minSdk` 31, `targetSdk` / `compileSdk` 36, `applicationId` `com.alain.epure.epub`, nom technique `EpureEpub`.
- [x] `app_name` = « Épure EPUB » dans `strings.xml`.
- [x] Reprendre le thème d'Épure PDF : `Color.kt` (trois jetons), `Theme.kt` (schéma sombre), `Type.kt` (échelle en `sp`), `themes.xml` (fond noir, pas de flash blanc).
- [x] Créer les paquets `ui/`, `data/`, `system/`, `domain/` et la classe `AppContainer`.
- [x] Manifeste : une seule activité, `allowBackup="false"`, aucune permission, aucun `INTERNET`.
- [x] `LICENSE` (GPL v3) et `.gitignore` Android (`*.jks`, `*.keystore`, `local.properties`, livres de test).
- [x] README : présentation, section Confidentialité et sécurité (pas d'accès à Internet, livre traité comme non fiable, permission absente du manifeste, données sur l'appareil), licence.
- [ ] Constituer le jeu de test **hors du dépôt** (`SPEC.md` annexe A).
- [x] Premier build, tests, lint ; app vide lancée sur l'appareil (écran noir, aucun flash blanc).
- [x] Premier commit (adresse « noreply »). Pas de `git push` sans demande explicite.

### Contrôles du jalon 0

- [x] L'app s'installe et s'ouvre sur un écran noir.
- [x] « Infos sur l'application » (One UI) → « Autorisations » : aucune autorisation listée.

---

## Jalon 1 — Lecteur de base

### Session 1a — Prototype comparatif des moteurs (§6 de `SPEC.md`)

- [x] Prototype jetable, sur une branche séparée, de l'option A (WebView configurée selon S-1 à S-7).
- [x] Prototype jetable de l'option C (rendu maison en texte seul), sur une autre branche.
- [x] Option B (Readium) : **proposition seulement**, à justifier dans `DECISIONS.md` avant ajout ; ne pas l'ajouter sans décision.
- [x] Mesures sur le jeu de test (annexe A) : poids de l'APK, temps d'ouverture (documents 1, 2, 9), mémoire, fidélité du rendu (documents 1, 3, 4).
- [x] Vérifier sur l'appareil : JavaScript non exécuté (document 5), ressources distantes non chargées (document 6), liens externes non ouverts (document 7).
- [x] Vérifier le manifeste fusionné de chaque prototype (aucune permission réseau).
- [x] Entrée `DECISIONS.md` : moteur retenu, mesures, tests de sécurité.
- [ ] Mettre à jour `SPEC.md` (retirer le `[À DÉCIDER]` du §6, consigner les `[À VÉRIFIER]` tranchés) — par le porteur.

### Session 1b — Ouverture et affichage

- [x] Interface du moteur dans `system/` (le reste de l'app n'en dépend pas directement).
- [x] Accueil minimal : titre, action « Ouvrir un fichier » (F-1.1).
- [x] Sélecteur système filtré sur `application/epub+zip` ; autorisation persistante demandée.
- [x] Lecture de l'archive hors du fil principal : validation des chemins, des tailles et du `mimetype` (S-4).
- [x] Lecture du package (titre, spine) et du XML sans entités externes (S-5).
- [x] Texte du chapitre courant affiché, pagination selon F-2.2 retenu, marges et interligne (F-2.1, F-2.3).
- [x] Chargement à la demande d'un chapitre ; page vide de bonne proportion pendant le chargement, sans indicateur animé (F-2.5).
- [x] Plein écran immersif (F-2.4).
- [x] Retour : ramène à l'accueil (F-6.4, version simple).
- [x] Messages d'erreur de base, sans plantage (F-1.5).
- [x] Rotation : position conservée (F-2.6).

### Session 1c — Navigation de base et sécurité du contenu

- [x] Liens internes à l'archive : déplacent la lecture ; liens externes : non ouverts, message (F-14.1, F-14.2).
- [x] Blocage de toute requête hors de l'archive (S-2) et du JavaScript (S-1).
- [x] Tests unitaires `domain/` : validation des chemins d'archive, limites de taille et de nombre d'entrées, politique des URL.

### Contrôles du jalon 1 (sur l'appareil)

- [x] Ouvrir le document 1 depuis « Mes fichiers » via le sélecteur : affichage correct, lecture fluide.
- [x] Document 2 (EPUB 2, sans `nav`) : chapitres lisibles jusqu'à la fin.
- [x] Document 4 (tableaux, listes) : mise en forme conforme à F-2.1.
- [x] Document 5 (JavaScript) : le texte s'affiche, aucun script exécuté.
- [x] Document 6 (ressources distantes) : aucune requête réseau ; images manquantes signalées sans plantage.
- [x] Document 7 (lien externe) : message, aucun navigateur lancé.
- [x] Document 8 (`../` dans l'archive) : refus, message, pas de plantage.
- [x] Document 11 (corrompu) et 12 (non EPUB) : message, pas de plantage.
- [x] Rotation au milieu d'un livre : même passage.
- [x] Annuler le sélecteur : retour à l'accueil sans message.
- [x] Aucune permission dans « Infos sur l'application » ; contrôle `aapt2` passé.

---

## Jalon 2 — Ouverture depuis d'autres apps, récents, reprise

### Session 2a — Intents `VIEW` et `SEND`

- [x] Filtres `VIEW` et `SEND` (`application/epub+zip`, schéma `content`) (F-1.2).
- [ ] Vérifier sur l'appareil la persistance des droits et le type de descripteur selon la source (F-1.3, F-1.4) ; consigner les résultats.
- [x] Copie temporaire ou message, selon la décision (F-1.4).
- [x] Retour depuis le lecteur ouvert par `VIEW` : retour à l'app d'origine.

### Session 2b — Récents et reprise

- [x] Persistance (`data/`, DataStore, `org.json`) : dernier livre, position (F-5.1, §7).
- [x] Accueil : le dernier livre, avec « 42 % lu » ; aucune entrée si rien n'a été ouvert (F-4.1).
- [x] Menu d'appui long : « Retirer de la liste » (libère l'autorisation, efface la position ; le fichier reste) (F-4.4).
- [x] Récent illisible : message « Fichier inaccessible… », entrée conservée (F-1.3).
- [x] Reprise à la dernière position, écriture différée après une pause de lecture (F-5.2, F-5.3).
- [x] Tests unitaires : ancre de position, restauration hors limites, identification par nom (F-5.5, F-4.5).

### Session 2c — Paramètres et « À propos »

- [x] Écran Paramètres minimal avec « À propos » (F-16.2) : version, licence, dépôt, confidentialité et sécurité, moteur et sa licence.
- [x] Réglage « Liste des livres » (désactivé par défaut) et liste limitée à 10 livres (F-4.2, F-16.1).

### Contrôles du jalon 2 (sur l'appareil)

- [x] « Ouvrir avec » Épure EPUB depuis « Mes fichiers », Gmail ou WhatsApp : le livre s'ouvre.
- [x] Retour après ouverture par `VIEW` : retour à l'app d'origine.
- [x] Fermer au chapitre 12, rouvrir depuis les récents : même passage.
- [x] Redémarrer le téléphone, rouvrir le récent : il s'ouvre.
- [x] Déplacer ou supprimer le fichier récent : message, pas de plantage.
- [x] « Retirer de la liste » : l'entrée disparaît ; le fichier existe toujours.
- [x] Forcer l'arrêt de l'app pendant la lecture puis la relancer : position conservée.
- [x] « À propos » : lien du dépôt ouvert dans le navigateur ; aucune permission ajoutée.

---

## Jalon 3 — Confort de lecture

### Session 3a — Commandes et réglages du texte

- [x] Appui court dans la bande basse (environ 2 cm) : ouvre la barre de commandes et les barres système ; fermeture par un appui sur le texte ou dans la bande basse ; le défilement ne la ferme plus ; glisser vers le bas l'affiche (F-6.1, F-6.2, DEC-EE-0021, DEC-EE-0028).
- [x] Barre du haut : titre (F-6.3).
- [x] Barre du bas, de gauche à droite : « Table » (visible mais désactivée jusqu'à la session 4a, DEC-EE-0021) ; pourcentage de progression (« 42 % ») ; « < » et « > », chapitre précédent et suivant (F-6.3, F-6.5, DEC-EE-0021 amendée) ; « Plus » à l'extrémité droite.
- [x] Panneau « Plus » ancré au-dessus de la barre, texte à gauche : « Taille du texte » (un appui = taille suivante, DEC-EE-0033), « Retour au passage ». « Rechercher » et « Marque-page » arrivent en 4a et 4b ; « Mode nuit », « Partager » et « Écran allumé » en 3b. Ne sont pas affichées avant leur session.
- [x] Accueil : barre basse séparée par un filet, « Ajout » au milieu, « Paramètres » à droite (DEC-EE-0030, remplace DEC-EE-0023 pour la barre et l'emplacement de « À propos »).
- [x] Interligne 1,4 appliqué au livre (F-2.3, DEC-EE-0021) (validé sur l'appareil).
- [x] Marges de 16 dp, longueur de ligne plafonnée à 75 caractères, taille par défaut 16 sp (DEC-EE-0024) (validé sur l'appareil).
- [x] Marges ramenées à 10 dp ; tailles de texte de 12 à 24 sp (paliers 12, 14, 16, 18, 20, 22, 24, défaut 16 sp) ; « Reste allumé pendant la lecture » (DEC-EE-0027) (validé sur l'appareil).
- [x] Thèmes de lecture « Jour (papier) », « Sépia », « Nuit (ardoise) », « Nuit (AMOLED) » dans « Plus » ; « Nuit (noir) » ; thème par défaut « Nuit (noir) » (fond noir, texte blanc atténué `#E6E6E6`) ; remplacent le blanc pur / atténué (DEC-EE-0026)  (validé sur l'appareil).
- [x] Taille du texte à cinq paliers, mémorisée, avec conservation du passage (F-3.1).
- [x] Police et couleurs du livre selon les décisions F-3.2 et F-3.3.
- [x] « Retour au passage » dans les commandes après un lien interne, une seule étape (F-14.1).

- [x] Latéralité « droite » (défaut) / « gauche » dans Paramètres : inverse le côté et l'ordre des barres (DEC-EE-0032) (validé sur l'appareil).
- [x] Page Paramètres reprise de Épure Notes : « Liste des livres », « Thème du texte » (passe au suivant à chaque appui), « Taille du texte » (augmente à chaque appui, revient à 12 sp après 24), « À propos » à la fin ; barre basse avec « Retour » (DEC-EE-0029, DEC-EE-0030) ; tailles, police et couleur reprises de Épure Notes (DEC-EE-0031) (validé sur l'appareil).

### Session 3b — Mode nuit, écran allumé, partage

- [~] Mode nuit (F-8) : **retiré** par le porteur (DEC-EE-0025, option 3) ; l'app est déjà noire. SPEC.md F-8 à mettre à jour par le porteur.
- [~] Images en mode nuit (F-8.3) : sans objet, le mode nuit est retiré (DEC-EE-0025).
- [x] Réglage « Écran allumé pendant la lecture », activé par défaut (F-9) (DEC-EE-0025, validé sur l'appareil).
- [x] « Partager » ajouté au menu « Plus » et au menu d'appui long d'un récent (F-13) (DEC-EE-0025, validé sur l'appareil).

### Contrôles du jalon 3 (sur l'appareil)

- [x] Appui court dans la bande basse : la barre apparaît ; « Table », « < », « > » et « Plus » réagissent ; le pourcentage est juste.
- [x] Changement de taille du texte : le passage lu reste affiché.
- [x] Commandes : apparition et disparition immédiates, sans animation.
- [x] Écran qui reste allumé pendant la lecture, et s'éteint à la sortie.

---

## Jalon 4 — Table des matières, marque-pages, recherche

### Session 4a — Table des matières et marque-pages

- [x] Table des matières à partir du `nav` ou du `NCX` (F-12.1) (DEC-EE-0037, option 1 : début du chapitre ; validé sur l'appareil).
- [x] Ancres des entrées sans JavaScript : option 1 retenue (DEC-EE-0037).
- [x] « Aucune table des matières » si absente ; liens hors archive ignorés (F-12.2, F-12.3) (validé sur l'appareil).
- [x] Marque-pages : ajout, retrait, écran listé, retrait immédiat (F-10) (DEC-EE-0036, validé sur l'appareil).
- Retours de test validés sur l'appareil, hors session (13/11/2026) : table et marque-pages défilants ; nom du chapitre et % dans le marque-page ; plusieurs marque-pages par chapitre (DEC-EE-0039) ; bouton « * » avec demande et fenêtre de navigation (DEC-EE-0040 à DEC-EE-0042) ; « Retour au passage » après sauts par table, « < », « > », bord de l'écran et liste (DEC-EE-0043). Commits : `9256a3c`, `302e613`, `8ec8d65`, `34005a0`, `d8860c9`, `ac5dd1f`, `5860c33`.

### Session 4b — Recherche

> En cours (13/11/2026). Décisions prises : DEC-EE-0038 (recherche : accents, portée sur tout le livre, signalement négatif), DEC-EE-0045 (extraction du texte par jsoup, mis en place et testé). Logique de recherche, panneau, entrée « Rechercher » dans la fenêtre « * » (DEC-EE-0042) et parcours des chapitres : mis en place et compilés (DEC-EE-0046). Reste : validation sur l'appareil, puis cases à cocher. Aucune case n'est cochée avant validation sur l'appareil.

- [ ] Champ de recherche, clavier ouvert à l'arrivée (F-11.1).
- [ ] Portée de la recherche selon F-11.3 retenu ; lecture chapitre par chapitre sans stockage du texte.
- [ ] Compteur à la fin de la recherche, « Précédent » et « Suivant », « Aucun résultat » (F-11.4).
- [ ] Insensibilité aux accents selon F-11.2 retenu, vérifiée sur l'appareil.
- [ ] Interruption possible par Retour ; pas d'indicateur animé (F-11.5).
- [ ] Livre sans texte : « Ce livre ne contient pas de texte. » (F-11.6).

### Contrôles du jalon 4 (sur l'appareil)

- [x] Table des matières du document 1 : appui sur un titre, saut au bon passage.
- [ ] Table du document 2 (NCX) : fonctionnelle.
- [ ] Marque-page ajouté, puis rouvert depuis la liste : même passage.
- [ ] Recherche sur le document 1 : compteur juste, navigation correcte.
- [ ] Recherche sur le document 3 (images) : message de fin, pas de plantage.

---

## Jalon 5 — Robustesse et sécurité

### Session 5a — Archives malveillantes et EPUB mal formés

- [ ] Limites de taille et de nombre d'entrées, rapport de compression (S-4) ; valeurs confirmées par mesure.
- [ ] Refus strict d'un `mimetype` incorrect, selon la décision de S-4.
- [ ] Protection contre les entités externes dans les XML (S-5).
- [ ] Livre protégé par DRM : message F-15.1, aucune tentative de déchiffrement.
- [ ] Chapitre mal formé : message à sa place, lecture continue (F-15.2).
- [ ] Ressource manquante : « Image indisponible », pas de plantage (F-15.3).

### Session 5b — Mémoire et gros livres

- [ ] Un seul chapitre complet chargé à la fois ; taille maximale d'un chapitre (T-6).
- [ ] Cache borné en octets, valeur confirmée par mesure.
- [ ] Message « Ce chapitre ne peut pas être affiché. » pour un chapitre trop grand ; pas de plantage.
- [ ] Mesure de la mémoire sur les documents 3 et 9.

### Session 5c — Vérifications finales

- [ ] Protocole de sécurité complet (annexe A, documents 5 à 10) : chaque protection vérifiée, consignée dans `DECISIONS.md`.
- [ ] Manifeste fusionné final : aucune permission réseau, y compris via la WebView si retenue.
- [ ] Points `[À VÉRIFIER]` de `SPEC.md` §9.2 : résultats consignés.

### Contrôles du jalon 5 (sur l'appareil)

- [ ] Document 8 (zip slip) : refus, message, aucun fichier écrit en dehors de l'app.
- [ ] Document 9 (bombe ZIP) : refus rapide, message, pas de plantage ni de blocage.
- [ ] Document 10 (entité externe) : pas de lecture externe, pas de plantage.
- [ ] Document 13 (DRM) : message, pas de plantage.
- [ ] Document 3 et 9 : défilement et changement de chapitre sans plantage.
- [ ] Arrêt forcé pendant la lecture d'un gros livre, puis relance : position conservée.

---

## Hors feuille de route

- Livres protégés par DRM (Adobe ADEPT, LCP) : hors périmètre.
- Mise en page fixe (« fixed layout ») : à réévaluer après le jalon 5 (`SPEC.md` §9.1).
- Autres formats (PDF, CBZ, CBR) : applications séparées (DEC-0037).
