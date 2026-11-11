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
- Statut : Tranchée par DEC-EE-0015 (11/11/2026)
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

**Résultats du prototype A (11/11/2026, Samsung SM-A536B, branche `proto/webview`)**

Mesures (médians sur 3 essais, en ms ; fichier `mesures_ouverture.csv` du dossier de travail) : livre 1 : lecture 55, affichage 244 ; livre 2 : 69 / 139 ; livre 3 (34,6 Mo) : 1 553 / 1 691 ; livres 4 à 7, 10, 13, 14 : affichage entre 118 et 172. Refus : livre 8 (chemin `../`) environ 65 ; livre 9 (bombe ZIP) environ 100 ; livres 11 et 12 entre 45 et 80. Le premier essai du livre 1 est plus lent (762) : démarrage de la WebView.

Observations à l'écran (porteur du projet) :
- Livre 1 : mise en page lisible, alignée à gauche ; « Chapitre 1 » plus grand que le texte (styles du livre appliqués).
- Livre 3 : idem.
- Livre 4 : tableau de deux colonnes et sept lignes, titres et lignes mal alignés.
- Livre 5 : texte affiché, sans signe d'exécution de script.
- Livre 6 : texte affiché ; image distante non chargée, cadre cassé, sans texte « Image indisponible ».
- Livre 7 : cinq liens bleus soulignés ; comportement au toucher non noté.
- Livre 8 : message « chemin non autorisé » (après correction du message) ; livre 9 : message « trop volumineux ».

Écarts relevés, à traiter dans le moteur retenu ou dans les sessions suivantes : styles du livre appliqués (F-3.3, DEC-EE-0011) ; image manquante sans texte (F-15.3) ; liens externes non ouverts au toucher (constaté : aucun navigateur lancé), mais sans message (F-14.2) ; alignement des tableaux (F-2.1).

Aucune de ces observations ne tranche le moteur à elle seule : le statut reste « à trancher ».

**Résultats du prototype C (texte seul, branche `proto/text-only`, 11/11/2026)**

Mesures : neuf ouvertures réussies après correction, affichage entre 87 et 1 764 ms ; le plus long correspond vraisemblablement au livre 3 (34,6 Mo). Les journaux ne donnent pas le nom du livre : la correspondance reste à confirmer. Les refus (chemins `../`, bombe ZIP, etc.) sont traités comme dans le prototype A.

Observations à l'écran (porteur du projet) : livres 1 à 7 lisibles ; titres plus grands que le texte ; tableau du livre 4 lisible ; liens du livre 7 affichés comme du texte, sans action ; image du livre 6 remplacée par « [Image] image distante », sans chargement.

Point technique : le prototype C ne gère que les entités courantes ; un livre avec une autre entité HTML sera refusé. Le rendu repose sur le lecteur XML de la plateforme, sans parseur maison.

Comparaison à retenir pour la décision : A rend la mise en forme du livre, mais en reprend les styles (F-3.3 à corriger) et dépend de la configuration de la WebView. C impose la mise en forme de l'app, sans images, avec tableaux aplatis, et réduit la surface d'attaque. Le choix reste à faire par le porteur.

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

## DEC-EE-0014 — Agrandissement d'une image du livre

- Date : 11/11/2026
- Statut : Proposée
- Jalon : 3
- Exigences concernées : F-2.1, F-8.3 ; `CHARTE.md` (interaction : pas de double toucher de zoom, ligne 339)

**Contexte**
Les images intégrées au texte sont affichées à la largeur de l'écran (F-2.1). Sur un écran de téléphone, une illustration ou une carte peut être trop petite pour être lue. La charte interdit aujourd'hui tout zoom par double toucher : la taille se règle par le texte, dans le menu « Plus ». Un agrandissement d'image ne touche pas au texte, mais il ajoute une interaction qui n'existe pas encore.

**Options**
- (a) Aucun agrandissement : l'image reste à la largeur de la colonne.
- (b) Appui sur une image : elle s'ouvre seule en plein écran, zoomable au pincement, avec retour par la touche Retour ou un appui en dehors de l'image. Pas de double toucher.
- (c) Double toucher sur une image pour l'agrandir. Dérogation directe à la charte, dont la règle porte sur tout le texte et les pages.

**Proposition** : (b). Il n'ajoute aucun geste nouveau au texte, reste compatible avec la règle contre le double toucher, et s'appuie sur des gestes standard. Il ne ferait pas de la page elle-même une image zoomable : seule l'illustration s'agrandit.

**Justification**
Les illustrations sont souvent la partie utile d'un livre technique ou d'un atlas. Le plein écran permet de les lire sans perdre la position de lecture, qui reste celle du chapitre. Aucune bibliothèque n'est nécessaire : une vue Compose ou la WebView suffit.

**Conséquences**
- Si (b) est retenu : ajout d'un écran ou d'une vue de plein écran pour les images, libellés TalkBack, chaînes dans `strings.xml`.
- `CHARTE.md` ne change pas, car (b) ne touche pas au double toucher. Si l'option (c) est retenue, une dérogation écrite dans la charte est requise, par nouvelle entrée.
- `SPEC.md` n'est pas modifié (règle A.11) ; une exigence F-2.x devrait être ajoutée par vous si la proposition est adoptée.
- Les images restent des fichiers de l'archive : aucune requête hors archive (S-2), aucune copie durable (T-3).

**Réversibilité**
Facile : une fonction d'affichage isolée, sans effet sur les données enregistrées.

---

## DEC-EE-0015 — Moteur de rendu : WebView système (option A)

- Date : 11/11/2026
- Statut : Adoptée
- Jalon : 1
- Exigences concernées : `SPEC.md` §6, S-1 à S-7, T-12, F-2.1, F-3.3 ; tranche DEC-EE-0009

**Contexte**
Le prototype comparatif (session 1a) a testé la WebView (A) et un rendu texte seul (C). Mesures et observations consignées dans DEC-EE-0009.

**Options**
- (a) WebView système, configurée selon S-1 à S-7.
- (b) Readium : non testé ; dépend d'un JavaScript injecté, en conflit avec S-1 ; ajout soumis à une décision dédiée.
- (c) Rendu texte seul.

**Décision**
Option (a) : la WebView système est le moteur de rendu de l'app. Elle est encapsulée dans `system/`, derrière une interface, comme le prévoit DEC-EE-0009.

**Conditions à respecter dans l'implémentation** (reprises du prototype, qui les a vérifiées sur l'appareil) :
- JavaScript désactivé, stockage DOM et base de données désactivés, accès fichiers et contenu désactivés, cookies refusés, zoom désactivé, cache désactivé.
- Seules les ressources de l'archive, servies depuis la mémoire sous une adresse fictive, sont accessibles ; toute autre requête est refusée.
- Aucune navigation : `shouldOverrideUrlLoading` refuse tout (la navigation interne sera ajoutée en session 1c, sans ouvrir de lien externe).
- Aucune permission ajoutée au manifeste fusionné (vérifié : seule la permission interne de DEC-EE-0013 apparaît).

**Points à traiter pendant l'implémentation** (écarts relevés au prototype) :
- Les styles du livre doivent être neutralisés par défaut (F-3.3, DEC-EE-0011).
- Une image indisponible doit afficher « Image indisponible » (F-15.3), et non un cadre cassé.
- Un lien externe doit afficher un message (F-14.2) ; aujourd'hui il est seulement ignoré.
- La pagination ou le défilement reste à trancher (DEC-EE-0012).

**Justification**
Fidélité de la mise en forme la plus élevée des trois options, aucune dépendance ajoutée, et une configuration sécurisée vérifiée sur l'appareil. La WebView est un composant Android intégré, conforme à CLAUDE.md §4 (préférer l'API intégrée à une bibliothèque tierce).

**Conséquences**
- Le code du prototype (branche `proto/webview`, non committée) est jetable : l'implémentation dans `main` est à écrire proprement, avec tests sur les règles de sécurité.
- Les règles de la WebView dépendent du composant système mis à jour par Google : à revérifier à chaque grande mise à jour d'Android ou de la WebView (CLAUDE.md §5).
- `SPEC.md` §6 contient encore le `[À DÉCIDER]` : c'est à vous de le retirer et d'y reporter DEC-EE-0015 (CLAUDE.md §5 : je ne modifie pas `SPEC.md`).
- Les cases de la session 1a restent à cocher par vous, après validation sur l'appareil.

**Réversibilité**
Moyenne : l'interface du moteur isole le choix ; un changement vers un autre moteur demande de réécrire `system/` et de revérifier S-1 à S-7.

**Mesures complémentaires (11/11/2026, prototype A, Samsung SM-A536B)**

- Poids : APK de version finale non signé, 1,58 Mo (version texte seul : 1,76 Mo). Le fichier de débogage (environ 30 Mo) n'est pas représentatif.
- Mémoire, build de débogage : à l'accueil, 86 Mo (PSS) ; livre 3 ouvert (34,6 Mo) : 160 Mo pour l'app, plus environ 110 Mo pour le processus WebView séparé. Ce sont des valeurs hautes, car une version finale consomme moins. La mémoire d'une version finale n'a pas été mesurée.
- Livre 3 : texte et images affichés ; les images du fichier de test sont du bruit aléatoire, ce qui explique l'aspect de « brouillard de couleurs ».

**Vérifications de sécurité (`SPEC.md` §9.2) — état au 11/11/2026**

Constaté sur l'appareil :
- S-1 (livre 5) : texte affiché ; « SCRIPT EXECUTE » absent de l'écran (constaté).
- S-2 (livre 6) : aucune ressource distante chargée ; cadre cassé, sans texte « Image indisponible » (à corriger, F-15.3).
- S-3 (livre 7) : liens non ouverts au toucher ; message absent (F-14.2, à ajouter).
- S-4 (livres 8 et 9) : refus avec message ; le livre 8 est refusé avant toute écriture.
- T-1 : manifeste des prototypes A et C vérifié avec `aapt2` ; seule la permission interne d'AndroidX apparaît.
- S-6 (partiel) : après lecture, aucun texte du livre ni nom de site dans les données, caches ou fichiers de la WebView ; aucun cookie trouvé. Les dossiers `Local Storage`, `Session Storage` et `HTTP Cache` existent, créés par la WebView. À implémenter : suppression des données de la WebView à la fermeture du livre, et désactivation du cache.

- S-5 (livre 10) : l'écran affiche « Avant &secret; après. » tel quel ; l'entité externe `file:///etc/hosts` n'est pas résolue et aucun contenu de fichier local n'apparaît (constaté).

- S-7 (livre de test 16, hors jeu d'origine, dans `~/EpubTest-securite`) : image et feuille de style pointant vers un fichier local, lien `file://` : aucune donnée de l'app affichée (constaté : cadre cassé avec le texte de remplacement, lien sans effet). Les phrases d'introduction et « Fin du test. » sont affichées.

Non encore vérifiés : F-1.3 et F-13.2 (intents, à l'étape 2a), F-2.4 (mode immersif), T-11 (TalkBack).

---

## DEC-EE-0016 — Mode de lecture : défilement continu, changement de chapitre par toucher sur les côtés

- Date : 11/11/2026
- Statut : Adoptée (dérogation confirmée par le porteur, 11/11/2026)
- Jalon : 1
- Exigences concernées : F-2.2, F-6.5 ; `CHARTE.md` B.6 et A.9 ; DEC-EE-0012 (tranchée ici)

**Contexte**
F-2.2 laissait le choix entre défilement continu et pagination (DEC-EE-0012). Le porteur a choisi le défilement vertical, avec passage de chapitre par un toucher sur le côté droit ou gauche de l'écran, sans boutons, pour permettre un mode immersif complet.

**Décision (proposée)**
- Le texte défile verticalement dans le chapitre courant ; la pagination horizontale n'est pas retenue (F-2.2 (a)). F-6.5 (pages précédente et suivante) ne s'applique pas.
- Un toucher sur le côté droit de l'écran passe au chapitre suivant ; un toucher sur le côté gauche passe au chapitre précédent (confirmé par le porteur).
- Un seul chapitre est chargé à la fois (`CLAUDE.md` §7) ; le défilement reste dans le chapitre. Au premier chapitre, le toucher à gauche ne fait rien ; au dernier, le toucher à droite ne fait rien (choix confirmé par le porteur, 11/11/2026).
- Pas de bouton « Page précédente » ni « Page suivante » dans les commandes.

**Dérogation à confirmer**
`CHARTE.md` B.6 impose qu'un geste de changement de page ait une action texte équivalente dans les commandes. Ce choix y déroge : les gestes latéraux n'ont pas d'équivalent texte. Une dérogation à la charte exige une entrée argumentée (règle de `CHARTE.md` et de `CLAUDE.md` §5). La charte n'est pas modifiée ici.

**Justification**
Mode immersif complet, lecture sans commandes visibles. Le défilement continu ne demande pas de découper le texte en pages, ce qui évite de devoir mesurer la pagination.

**Conséquences**
- F-2.2 et F-6.5 de `SPEC.md` à réécrire par le porteur (`SPEC.md` n'est pas modifié ici).
- Positions de lecture (F-5.1) : chapitre et décalage vertical, comme prévu pour le défilement.
- Points de vigilance : la zone tactile latérale doit rester distincte du toucher central des commandes (F-6.1) ; à tester sur l'appareil.

**Réversibilité**
Moyenne : change la gestion des positions et des gestes ; la pagination pourrait être ajoutée plus tard par nouvelle entrée.

---

## DEC-EE-0017 — Mimetype incorrect : ouverture avec avertissement

- Date : 11/11/2026
- Statut : Adoptée
- Jalon : 1
- Exigences concernées : S-4 (`SPEC.md` §5 bis), F-1.5 ; tranche le point « mimetype » de DEC-EE-0016 et de la décision en attente n° 4

**Contexte**
S-4 prévoit deux options pour un `mimetype` absent, mal placé ou de valeur fausse : refus strict (proposition du SPEC), ou acceptation avec avertissement. Le porteur a choisi l'acceptation avec avertissement.

**Décision**
- Un livre dont le `mimetype` est absent, mal placé, compressé ou d'une autre valeur que `application/epub+zip` est tout de même lu, à condition que :
  - il contienne `META-INF/container.xml` et un fichier de contenu (package) valide ;
  - toutes les autres protections de S-4 à S-7 s'appliquent sans changement (chemins, tailles, rapport de compression, XML sans entités externes, aucun script, aucune ressource hors archive).
- Un message d'avertissement est affiché à l'ouverture, sans bloquer la lecture. Texte validé par le porteur (11/11/2026) : « Ce fichier ne respecte pas tout à fait le format EPUB : la mise en forme peut différer. »
- Si `container.xml` ou le fichier de contenu manque ou est illisible, le livre est refusé avec le message existant « Ce fichier n'est pas un EPUB lisible. » (`CHARTE.md` §6).

**Justification**
La sécurité ne repose pas sur le `mimetype` mais sur les protections S-1 à S-7, qui restent appliquées. L'avertissement permet d'ouvrir des livres légitimes produits par des outils peu rigoureux, tout en informant la personne.

**Conséquences**
- Le prototype (refus sur valeur fausse) ne sera pas repris tel quel : la session 1b implémentera ce choix.
- Le texte d'avertissement est ajouté à `strings.xml` (clé `warning_mimetype`, branche `feature/session-1b`). `CHARTE.md` §6 n'est pas modifiée ; cet ajout de message est consigné ici.
- `SPEC.md` S-4 (point « mimetype ») à mettre à jour par le porteur.
- Les tests de `domain/` doivent couvrir : mimetype absent, mal placé, valeur fausse, container manquant.

**Réversibilité**
Facile : le contrôle est isolé dans la validation de l'archive ; un retour au refus strict demande une nouvelle entrée.

---

## DEC-EE-0018 — Lecture de l'archive : copie temporaire du livre

- Date : 11/11/2026
- Statut : Adoptée (dérogation à `CLAUDE.md` §4 confirmée par le porteur, 11/11/2026)
- Jalon : 1
- Exigences concernées : F-1.4, S-6, T-2, T-3 ; `CLAUDE.md` §4 (« Le contenu du livre n'est pas copié durablement » et « Aucune donnée du livre dans un cache système »)

**Contexte**
Un EPUB est une archive ZIP lue en accès aléatoire (images, feuilles de style, chapitres). Un flux SAF ne permet pas toujours cet accès (F-1.4).

**Décision retenue par le porteur (11/11/2026)** : copie temporaire du livre dans le cache privé de l'app, pour lecture aléatoire ; supprimée à la fermeture du livre et au démarrage suivant (proposition de F-1.4 (a), comme DEC-0022 d'Épure PDF).

**Point à confirmer (dérogation)**
`CLAUDE.md` §4 interdit de copier durablement le contenu du livre et toute donnée du livre dans un cache système. Une copie temporaire dans le cache privé déroge à cette règle pendant la lecture. Le porteur doit la confirmer par écrit ; l'implémentation attend cette confirmation. Garanties prévues : suppression à la fermeture et au démarrage suivant, aucune copie hors du cache privé de l'app, aucun partage de ce cache.

**Conséquences**
- Espace disque utilisé : jusqu'à la taille du livre (limite S-4 : 200 Mo décompressés).
- Une copie interrompue (arrêt forcé) est supprimée au démarrage suivant.
- Le test de « Arrêt forcé » du jalon 5 vérifiera cette suppression.

**Réversibilité**
Facile : remplacer la copie par une relecture de l'archive (DEC-EE-0018 (b)), au prix de la vitesse.

---

## DEC-EE-0019 — Changement de chapitre aussi par glisser horizontal

- Date : 11/11/2026
- Statut : Adoptée (demandée par le porteur après test sur l'appareil, 11/11/2026)
- Jalon : 1 (session 1c)
- Exigences concernées : F-6.5, F-2.2 ; complète DEC-EE-0016 ; `CHARTE.md` B.6 (interaction) : dérogation à confirmer

**Contexte**
DEC-EE-0016 prévoit le changement de chapitre par un toucher sur le côté droit ou gauche de l'écran. Au test sur l'appareil, le porteur a demandé que le glisser horizontal le fasse aussi.

**Décision**
Un glisser horizontal d'au moins 96 dp (vers la gauche : chapitre suivant ; vers la droite : chapitre précédent) change de chapitre. Un glisser vertical reste au moteur de rendu, qui fait défiler le chapitre. Un glisser plus horizontal que vertical, à vitesse suffisante, est seul pris en compte.

**Conséquences**
- Le toucher sur les côtés reste en place. Les deux gestes ont la même action.
- Risque : un geste horizontal involontaire change de chapitre. Le seuil de 96 dp limite ce risque ; à ajuster après test.
- Dérogation à `CHARTE.md` B.6, à confirmer par le porteur.

**Réversibilité**
Facile : retirer la méthode `onFling` de `BookWebView`.

---

## DEC-EE-0020 — Stockage des récents et des positions : préférences Android et org.json

- Date : 11/11/2026
- Statut : Adoptée (choix validé par le porteur, 11/11/2026) ; écart à `SPEC.md` §7 à confirmer
- Jalon : 2 (session 2b)
- Exigences concernées : §7 (données stockées), F-4.1 à F-4.5, F-5.1 à F-5.5, T-2 ; `CLAUDE.md` §4 (dépendances)

**Contexte**
`SPEC.md` §7 prévoit DataStore Preferences, avec les données sérialisées en `org.json`. Ajouter DataStore est une nouvelle bibliothèque, soumise à décision avant ajout (`CLAUDE.md` §4).

**Décision**
Les données (dernier livre, position, progression) sont stockées dans les préférences privées de l'application (`SharedPreferences`, intégré à Android), sous forme d'un objet JSON sérialisé avec `org.json`. Aucune bibliothèque ajoutée.

**Justification**
Pas de dépendance nouvelle ; les données tiennent dans un petit objet (un seul récent par défaut, F-4.2). `org.json` est déjà fourni par Android.

**Conséquences**
- Écart à `SPEC.md` §7 (DataStore) : le porteur doit consigner le changement dans `SPEC.md` (CLAUDE.md §5 : je ne modifie pas `SPEC.md`).
- Une seule entrée est conservée : ouvrir un autre livre remplace le récent précédent (F-4.2, réglage « Liste des livres » reporté à la session 2c).
- Les positions sont mesurées en pixels de défilement : elles restent valables sur le même appareil, pas forcément après un changement de taille de texte (session 3a).

**Réversibilité**
Facile : remplacer `RecentStore` par DataStore, sans changer les données métier.

---

## Décisions en attente

La liste à jour des points `[À DÉCIDER]` est tenue dans `SPEC.md` §9.1, avec le jalon auquel chacun doit être tranché. Les premiers à trancher sont :

1. Le moteur de rendu (DEC-EE-0009), à l'issue du prototype du jalon 1.
2. Le mode de défilement : tranché en DEC-EE-0016, dérogation à la charte à confirmer.
3. La copie temporaire pour un descripteur non repositionnable (`SPEC.md` F-1.4).
4. Le `mimetype` incorrect : tranché en DEC-EE-0017 (ouverture avec avertissement). Les limites chiffrées de S-4 : propositions retenues par le porteur, à mesurer (`ArchiveLimits`).
