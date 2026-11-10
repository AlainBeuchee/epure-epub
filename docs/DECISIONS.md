# Journal des décisions — Épure EPUB

> Ce journal consigne chaque décision technique, fonctionnelle ou de méthode, avec son contexte et sa justification.
> Règles :
> - Les entrées ne sont **jamais effacées**. Une décision révisée reçoit une nouvelle entrée qui la remplace ; l'ancienne passe au statut « Remplacée par DEC-EE-xxxx ».
> - Un point `[À DÉCIDER]` de `SPEC.md` est d'abord tranché ici, puis retiré du `SPEC.md` (avec renvoi à l'entrée).
> - Toute nouvelle dépendance est justifiée ici **avant** son ajout (licence, compatibilité GPL v3, permissions du manifeste fusionné).
> - Toute dérogation à `CHARTE.md` est consignée ici.
> - Numérotation continue propre à ce projet : DEC-EE-0001, DEC-EE-0002… (distincte de celle d'Épure PDF).
> - Une entrée au statut **Proposée** n'est pas encore validée par le porteur du projet ; elle ne se met pas en œuvre avant validation.

---

## Modèle d'entrée

```
## DEC-EE-xxxx — Titre court

- Date : JJ/MM/AAAA
- Statut : Proposée | Adoptée | Remplacée par DEC-EE-xxxx | Abandonnée
- Jalon : n
- Exigences concernées : F-x.y, T-n, S-n

**Contexte**
Pourquoi la question se pose.

**Options**
- (a) …
- (b) …

**Décision**
L'option retenue, formulée sans ambiguïté.

**Justification**
Pourquoi cette option : critères, mesures, contraintes.

**Conséquences**
Ce que cela implique (code, SPEC, charte, permissions, licence).

**Réversibilité**
Facile | Moyenne | Difficile — et ce qu'il faudrait refaire.
```

---

## DEC-EE-0001 — Identité de l'application

- Date : 10/11/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : `SPEC.md` §1.2

**Contexte**
Nommer l'app et fixer les identifiants avant le premier build : l'`applicationId` ne peut plus changer après une éventuelle publication.

**Décision (proposée)**
- Nom affiché : « Épure EPUB ».
- Nom technique : `EpureEpub`.
- `applicationId` : `com.alain.epure.epub`.
- Dépôt GitHub : `epure-epub`, public.

**Justification**
Cohérence avec Épure PDF (`com.alain.epure.pdf`) et le launcher Épure (`com.alain.epure`). « EPUB » désigne un format ouvert : usage descriptif sans risque. Aucun nom ni logo tiers.

**Conséquences**
Avant toute publication sur le Play Store : recherche d'homonymes, vérification INPI / EUIPO (classe 9).

**Réversibilité**
Nom affiché : facile. `applicationId` : difficile après publication.

---

## DEC-EE-0002 — Application distincte d'Épure PDF

- Date : 10/11/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : `SPEC.md` §1.2 ; DEC-0037 d'Épure PDF

**Contexte**
Les formats EPUB et PDF demandent des moteurs d'affichage différents. DEC-0037 d'Épure PDF prévoit une application séparée pour l'EPUB.

**Options**
- (a) Module ou format ajouté à Épure PDF.
- (b) Application séparée, dépôt et cycle propres.

**Décision (proposée)** : (b). Aucune dépendance de code entre les deux apps. La charte et les composants sont **copiés**, pas partagés par une bibliothèque commune (comme DEC-0002 d'Épure PDF).

**Justification**
Le nom « Épure PDF » ne couvrirait plus l'app. La surface de sécurité d'un EPUB (HTML, CSS, scripts) est différente de celle d'un PDF.

**Conséquences**
- Aucun fichier de ce projet n'est versionné dans le dépôt d'Épure PDF.
- Une correction de la charte d'Épure PDF ne se répercute pas ici automatiquement : elle doit être reportée à la main, avec une entrée dans ce journal.

**Réversibilité**
Facile tant qu'aucune version n'est publiée.

---

## DEC-EE-0003 — Licence GPL v3

- Date : 10/11/2026
- Statut : Adoptée
- Jalon : 0

**Décision (proposée)** : code publié sous GPL v3. Fichier `LICENSE` présent dès le premier commit.

**Justification**
Règle commune des projets publics (`CHARTE.md` A.2 et A.12).

**Conséquences**
Toute bibliothèque ou code repris doit avoir une licence compatible avec la GPL v3. Une bibliothèque sous AGPL ferait passer l'ensemble sous AGPL : décision spécifique requise.

**Réversibilité**
Difficile une fois du code de contributeurs extérieurs intégré.

---

## DEC-EE-0004 — Pile technique reprise d'Épure PDF

- Date : 10/11/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : T-9, T-10, T-12

**Décision (proposée)**
- Kotlin uniquement ; aucun code natif ajouté (pas de NDK, pas de Rust).
- Jetpack Compose, Material 3, thème sombre personnalisé (trois jetons, `CHARTE.md` A.5).
- `minSdk` 31 ; `targetSdk` / `compileSdk` 36.
- Une seule activité ; `ViewModel` + `StateFlow` ; navigation par état simple ; pas d'injection (`AppContainer`).
- Paquets `ui/`, `data/`, `system/`, `domain/` ; `domain/` sans dépendance Android, couvert par des tests unitaires.
- Appareil de référence : Samsung SM-A536B, Android 16 / One UI 8.0.

**Justification**
Pile déjà éprouvée sur Épure PDF et Épure ; méthode et conventions réutilisables.

**Conséquences**
Un moteur de rendu tiers à code natif exigerait une décision dérogeant à « pas de code natif ». Le choix du moteur est traité à part (DEC-EE-0009).

**Réversibilité**
Moyenne.

---

## DEC-EE-0005 — Aucune permission réseau

- Date : 10/11/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : T-1, T-2

**Décision (proposée)**
- `INTERNET` et `ACCESS_NETWORK_STATE` absentes du manifeste source **et** du manifeste fusionné.
- Contrôle à chaque ajout ou mise à jour de dépendance avec `aapt2` (DEC-0032 d'Épure PDF).
- Permission injectée par une bibliothèque : retrait par `tools:node="remove"`, ou bibliothèque écartée, avec entrée dans ce journal.
- Le composant de rendu (si WebView) n'ajoute aucune permission à l'app ; à vérifier sur le manifeste fusionné.

**Justification**
Garantie de confidentialité vérifiable par quiconque lit le manifeste. Pour un EPUB, c'est aussi une protection : un livre ne peut pas faire sortir de données ni charger de contenu, même s'il en contient.

**Réversibilité**
Facile techniquement, mais contraire au principe fondateur : toute révision exige une nouvelle entrée argumentée.

---

## DEC-EE-0006 — Accès aux fichiers par le Storage Access Framework seulement

- Date : 10/11/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : F-1, T-3

**Décision (proposée)** : accès par `ACTION_OPEN_DOCUMENT` et par les intents `VIEW` et `SEND` (`application/epub+zip`). Jamais de permission de stockage, jamais de parcours de dossiers.

**Justification**
Aucune permission demandée ; l'utilisateur choisit chaque livre. Les droits temporaires reçus par `VIEW` sont traités comme dans Épure PDF (DEC-0021, DEC-0034).

**Réversibilité**
Facile.

---

## DEC-EE-0007 — Aucun service en arrière-plan, aucune notification

- Date : 10/11/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : T-4

**Décision (proposée)** : pas de `Service`, pas de `WorkManager`, pas de `POST_NOTIFICATIONS`. Le traitement d'un livre a lieu pendant que l'utilisateur le lit.

**Réversibilité**
Facile.

---

## DEC-EE-0008 — Le livre est traité comme un fichier non fiable

- Date : 10/11/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : T-13, S-1 à S-7, F-14, F-15

**Contexte**
Un EPUB est une archive de pages HTML, CSS et parfois JavaScript, fournie par un tiers. Il peut contenir du code actif, des ressources à charger sur Internet, des liens externes, une archive piégée (chemins qui sortent de l'archive, bombe de compression) ou des XML avec entités externes.

**Décision (proposée)**
- S-1 : aucun JavaScript exécuté.
- S-2 : aucune requête ni ressource hors de l'archive.
- S-3 : seuls les liens internes déplacent la lecture ; les liens externes ne sont pas ouverts.
- S-4 : archive validée avant lecture (chemins normalisés, nombre et taille des entrées bornés, rapport de compression limité, `mimetype` strict).
- S-5 : XML lus sans DTD ni entités externes.
- S-6 : aucun cache, cookie ni stockage du moteur de rendu.
- S-7 : le moteur n'accède à aucun fichier de l'app.

**Justification**
Un lecteur n'a pas besoin de ces capacités pour lire un livre. Les refuser supprime la plupart des risques, sans changer la lecture du texte.

**Conséquences**
- Certains livres s'affichent moins fidèlement (contenu dépendant d'un script, ressources distantes).
- Toute levée d'une de ces règles exige une nouvelle entrée argumentée.

**Réversibilité**
Facile à assouplir, difficile à justifier : toute révision demande une nouvelle entrée.

---

## DEC-EE-0009 — Moteur de rendu : choix reporté au prototype

- Date : 10/11/2026
- Statut : Proposée (à trancher au jalon 1)
- Jalon : 1
- Exigences concernées : `SPEC.md` §6, T-12

**Contexte**
Il n'existe pas d'équivalent de PdfRenderer pour l'EPUB. Il faut un moteur d'affichage de texte HTML.

**Options**
- (a) WebView système, configurée selon S-1 à S-7 : intégrée à Android, aucune dépendance, mise en forme fidèle ; composant mis à jour par Google, configuration à maîtriser.
- (b) Readium Kotlin toolkit : bibliothèque ouverte prévue pour les lecteurs EPUB ; dépendances à justifier ; licence à confirmer avant ajout (annoncée BSD-3).
- (c) Rendu maison en texte seul, sans HTML ni WebView : surface d'attaque minimale ; mise en forme réduite (images, tableaux, styles perdus).

**Décision** : aucune. Prototype comparatif jetable des options (a) et (c) au jalon 1 (sur le modèle de DEC-0014 d'Épure PDF), puis décision consignée ici. L'option (b) n'est pas ajoutée sans entrée dédiée.

**Justification**
Le choix dépend de mesures (fidélité, mémoire, sécurité effective) qui n'existent pas encore.

**Réversibilité**
Moyenne : l'interface du moteur dans `system/` isole ce choix du reste de l'app.

---

## DEC-EE-0010 — Ancre de position : format maison

- Date : 10/11/2026
- Statut : Adoptée
- Jalon : 2
- Exigences concernées : F-5.1, F-10.2

**Décision (proposée)** : la position est mémorisée sous forme d'ancre maison : rang du chapitre dans la spine, décalage (ou rang de page écran), progression en pourcentage. Le format CFI (standard EPUB) n'est pas retenu pour la version 1.

**Justification**
Plus simple à écrire et à tester ; suffisant pour une reprise de lecture sur le même livre.

**Réversibilité**
Moyenne : un changement de format demanderait une migration des positions enregistrées.

---

## DEC-EE-0011 — Contenu du livre : couleurs et polices ignorées par défaut

- Date : 10/11/2026
- Statut : Adoptée
- Jalon : 3
- Exigences concernées : F-3.2, F-3.3, F-8.2, F-8.3

**Décision (proposée)**
- Les couleurs, fonds et polices définis par le livre sont ignorés. Le texte prend les jetons `TextPrimary` sur `Black`, ou l'inverse en mode nuit, par styles imposés au contenu.
- Police : police système sans empattement.
- Images : affichées telles quelles, sans inversion, y compris en mode nuit.
- Une option « Couleurs du livre », désactivée par défaut, reste possible plus tard par nouvelle entrée.

**Justification**
Cohérence avec la charte (fond noir, texte clair, aucune couleur d'accent) et lisibilité garantie quel que soit le livre.

**Dérogation** : aucune à la charte ; les couleurs du livre sont du contenu, comme les pages PDF d'Épure PDF, mais elles sont ici remplacées et non conservées.

**Réversibilité**
Facile.

---

## DEC-EE-0012 — Pagination ou défilement : à trancher

- Date : 10/11/2026
- Statut : Proposée (à trancher au jalon 1)
- Jalon : 1
- Exigences concernées : F-2.2, F-6.5, B.6

**Options**
- (a) Défilement vertical continu, chapitre après chapitre.
- (b) Pagination horizontale, une page écran à la fois, avec balayage et actions texte « Page précédente » / « Page suivante ».

**Proposition** : (b), plus adaptée à la lecture d'un livre. À confirmer après le prototype (DEC-EE-0009), car la pagination dépend du moteur.

**Réversibilité**
Moyenne : change la gestion des positions et des gestes.

---

## DEC-EE-0013 — Permission interne ajoutée par AndroidX conservée

- Date : 10/11/2026
- Statut : Adoptée
- Jalon : 0
- Exigences concernées : T-1 (`SPEC.md` §8), `CLAUDE.md` §4 (permissions injectées par une bibliothèque)

**Contexte**
Le fusionnement du manifeste ajoute `com.alain.epure.epub.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (niveau `signature`). Elle vient de `androidx.core` 1.18.0, dépendance transitive d'AndroidX, et apparaît dans la sortie de `aapt2 dump permissions`. Elle n'est liée ni au réseau, ni aux fichiers, ni à l'utilisateur, et aucune autre application ne peut l'obtenir.

**Options**
- (a) Conserver la permission telle qu'elle est ajoutée.
- (b) La retirer avec `tools:node="remove"`. Risque : casser l'enregistrement dynamique de récepteurs utilisé par AndroidX, sans bénéfice pour la confidentialité.
- (c) Écarter la bibliothèque. Impossible sans renoncer à AndroidX, dont dépendent Compose et l'activité.

**Décision**
(a) : la permission est conservée. Elle est une exception documentée à T-1 pour une permission injectée par une bibliothèque, à condition qu'elle reste de niveau `signature` et interne à l'app.

**Justification**
Les trois autres options ont un coût supérieur au bénéfice. La permission ne donne aucun accès à l'extérieur de l'app et n'apparaît pas dans « Autorisations » (contrôle du jalon 0 validé sur l'appareil).

**Conséquences**
- `aapt2 dump permissions` affiche cette permission après chaque build : c'est attendu, et tout autre résultat doit être signalé.
- Toute permission **autre** que celle-ci reste interdite sans nouvelle entrée.
- `SPEC.md` n'est pas modifié (règle A.11) : l'écart est signalé ici et doit être reconnu lors de la prochaine relecture de T-1.
- À réexaminer à chaque mise à jour d'AndroidX : si la permission disparaît, rien à faire.

**Réversibilité**
Facile : ajouter `tools:node="remove"` dans le manifeste, puis vérifier sur l'appareil que l'app se lance et que la lecture fonctionne.

---

## Décisions en attente

La liste à jour des points `[À DÉCIDER]` est tenue dans `SPEC.md` §9.1, avec le jalon auquel chacun doit être tranché. Les premiers à trancher sont :

1. Le moteur de rendu (DEC-EE-0009), à l'issue du prototype du jalon 1.
2. Le mode de défilement (DEC-EE-0012).
3. La copie temporaire pour un descripteur non repositionnable (`SPEC.md` F-1.4).
4. Le refus strict d'un `mimetype` incorrect et les limites chiffrées de S-4 (à mesurer).
