# Cahier des charges — Épure EPUB

> Version 0.1 — 10 novembre 2026
> Statut : cadrage de déclinaison, avant le jalon 0. Document de travail, non validé par le porteur.
> Source : `SPEC.md` d'Épure PDF (version 0.1 et jalons 0 à 5), dont les exigences communes sont reprises ici.
> Ce document décrit **ce que l'application doit faire**. Les règles visuelles, d'interaction et de sécurité sont celles de `CHARTE.md`.
> Conventions : `F-x.y` = exigence fonctionnelle ; `T-n` = exigence transversale ; `S-n` = exigence de sécurité du contenu ; **[À DÉCIDER]** = point non tranché, à consigner dans `DECISIONS.md` avant toute implémentation ; **[À VÉRIFIER]** = point à tester sur l'appareil, résultat à consigner.
> Une valeur suivie de « (proposition) » est une proposition de départ, pas une décision.

---

## 1. Objet et périmètre

### 1.1 Objet

Épure EPUB est un lecteur de livres au format EPUB (versions 2 et 3, sans protection) pour Android, minimaliste, **entièrement hors ligne** et **sûr à ouvrir** : lire sans distraction, sans réseau, sans collecte de données, sans exécuter de code venu du livre. Usage principal : romans et essais, documents techniques au format EPUB, lus sur téléphone.

### 1.2 Identité

| Élément | Valeur |
|---|---|
| Nom affiché | « Épure EPUB » (`app_name`) |
| Nom technique | `EpureEpub` |
| `applicationId` | `com.alain.epure.epub` (proposition, à consigner dans `DECISIONS.md`) |
| Dépôt Git | `epure-epub` (public) |
| Licence | GPL v3 |
| Relation avec Épure PDF | Application distincte : dépôt, identifiant et cycle séparés ; aucune dépendance de code ; cohérence visuelle seulement (DEC-0037) |

- « EPUB » est un format ouvert (standard de l'IDPF puis du W3C). Aucun nom, logo ou visuel d'une marque tierce ; aucune suggestion d'affiliation.
- Avant une éventuelle publication sur le Play Store : recherche d'homonymes, vérification INPI / EUIPO (classe 9). Publication non prioritaire.

### 1.3 Hors périmètre

- **Livres protégés par DRM** (Adobe ADEPT, Readium LCP, ou tout chiffrement de contenu). Ils ne sont pas ouverts ; message d'erreur (F-15.1).
- Annotations, surlignage, notes, signets dans le fichier, édition, synchronisation cloud, dictionnaire ou traduction en ligne, lecture à voix haute, achat ou téléchargement de livres.
- Exécution de JavaScript contenu dans le livre (voir S-1).
- Lecture des contenus audio et vidéo intégrés (ignorés, sans plantage).
- Mise en page fixe (« fixed layout ») : **[À DÉCIDER]**, voir §9.1.
- Formats voisins (PDF, CBZ, CBR) : hors de cette app (DEC-0037 ; PDF dans Épure PDF).

### 1.4 Appareil de référence

Samsung sous Android 16 / One UI 8.0 (SM-A536B, comme pour Épure PDF). Émulateur accepté pour le développement, validation finale toujours sur l'appareil.

---

## 2. Glossaire

| Terme | Sens dans ce document |
|---|---|
| Livre | Un fichier EPUB ouvert par l'utilisateur, identifié par son URI |
| URI | Adresse `content://` du fichier, fournie par le système ; l'app ne connaît pas le chemin réel |
| SAF | Storage Access Framework : le sélecteur de fichiers du système (`ACTION_OPEN_DOCUMENT`) |
| Autorisation persistante | Droit de relire un fichier après redémarrage (`takePersistableUriPermission`) |
| Archive | Le fichier EPUB est une archive ZIP : `mimetype`, `META-INF/container.xml`, fichier de package (OPF), chapitres XHTML, ressources |
| Package (OPF) | Fichier qui liste les chapitres, leur ordre (« spine ») et les métadonnées (titre, auteur) |
| Spine | Ordre de lecture des chapitres, défini dans le package |
| Table des matières | Fichier `nav` (EPUB 3) ou `NCX` (EPUB 2) ; liste hiérarchique des titres |
| Moteur de rendu | Composant qui affiche le contenu d'un chapitre (voir §6) |
| Position de lecture | Chapitre (rang dans la spine) et décalage à l'intérieur du chapitre, mémorisés pour un livre |
| Progression | Pourcentage du livre lu, calculé à partir de la position |
| Commandes | Barre de texte affichée au toucher dans le lecteur |
| Ressource distante | Toute image, police, feuille de style ou script désigné par une adresse hors de l'archive |

---

## 3. Écrans

| Écran | Rôle | Accès | Jalon |
|---|---|---|---|
| Accueil | Titre, « Ouvrir un fichier », dernier livre, accès aux paramètres | Lancement de l'app | 1 (sans récents), 2 (avec récents) |
| Lecteur | Texte du livre, commandes au toucher | Ouverture d'un livre | 1 |
| Table des matières | Liste hiérarchique des titres | Commandes | 4 |
| Marque-pages | Liste des marque-pages du livre | Commandes | 4 |
| Recherche | Champ de recherche et navigation entre résultats | Commandes | 4 |
| Paramètres | Options de lecture, « À propos » | Accueil | 2 (« À propos »), 3 (options) |

Navigation : état simple géré à la main, sans `navigation-compose` (`CHARTE.md` Partie A, A.3). Pas d'écran de mot de passe : les livres protégés ne sont pas ouverts (§1.3).

---

## 4. Exigences fonctionnelles

### F-1 Ouverture d'un livre

**F-1.1 Sélecteur système.** L'action « Ouvrir un fichier » de l'accueil lance `ACTION_OPEN_DOCUMENT` filtré sur `application/epub+zip`. Le livre choisi s'ouvre dans le lecteur.
- Critères : un EPUB choisi dans « Mes fichiers », Google Drive hors ligne ou « Téléchargements » s'ouvre ; annuler le sélecteur ramène à l'accueil sans message.
- Demande d'une autorisation persistante sur l'URI (lecture seule).

**F-1.2 Ouverture depuis une autre app.** Filtres `ACTION_VIEW` et `ACTION_SEND`, catégorie `DEFAULT`, type `application/epub+zip`, schéma `content`. Le livre apparaît dans « Ouvrir avec » et « Partager » des gestionnaires de fichiers, messageries et navigateurs.
- Critères : un EPUB ouvert depuis « Mes fichiers », une pièce jointe d'e-mail et un téléchargement du navigateur s'affichent ; le bouton Retour ramène à l'app d'origine (comme DEC-0020).
- Le schéma `file://` n'est pas accepté.

**F-1.3 Droits temporaires.** Comme pour Épure PDF (DEC-0034, DEC-0044) : un droit reçu par `VIEW` peut ne pas être persistant. Le récent devenu illisible affiche « Fichier inaccessible. Ouvre-le à nouveau depuis l'application où il a été ouvert initialement. » (DEC-0021, reprise). **[À VÉRIFIER]** sources réelles à tester sur l'appareil.

**F-1.4 Descripteur non repositionnable.** Un EPUB doit être lu en accès aléatoire (le répertoire central du ZIP est en fin de fichier). Si la source ne fournit pas un descripteur repositionnable : **[À DÉCIDER]** (a) copie temporaire dans le cache privé de l'app, supprimée à la fermeture du livre et au démarrage suivant (proposition, comme DEC-0022) ; (b) message d'erreur.

**F-1.5 Erreurs d'ouverture.** Un message en texte, sans plantage, et retour possible à l'accueil :
- fichier absent : « Fichier introuvable. » ;
- droit retiré : « Accès au fichier retiré. » ;
- fichier qui n'est pas un EPUB (mauvais `mimetype`, archive ZIP invalide) : « Ce fichier n'est pas un EPUB lisible. » ;
- EPUB sans package ou sans chapitre lisible : « Ce livre ne contient pas de texte lisible. » ;
- archive suspecte (voir S-4) : « Ce livre est trop volumineux ou mal construit pour être ouvert. » ;
- livre protégé (F-15.1).

### F-2 Affichage et pagination

**F-2.1 Texte recomposable.** Le texte s'adapte à la largeur de l'écran. Les paragraphes, titres, listes, citations, images intégrées et tableaux simples sont affichés. Les styles du livre sont ignorés par défaut (voir F-3.3, F-8.2).

**F-2.2 Mode de défilement. [À DÉCIDER]** (a) défilement vertical continu, chapitre après chapitre ; (b) pagination horizontale, une « page écran » à la fois, avec balayage gauche-droite et boutons « Page suivante » / « Page précédente » dans les commandes (proposition, plus adaptée à la lecture d'un livre).

**F-2.3 Marges et interligne.** Marges latérales de 24 dp (proposition, comme les écrans de Épure) ; interligne de 1,4 fois la taille du texte (proposition). Valeurs non modifiables par l'utilisateur en version 1 (voir F-3.1).

**F-2.4 Plein écran.** Pendant la lecture, barres d'état et de navigation masquées (mode immersif), comme F-2.4 de Épure PDF. Elles réapparaissent avec les commandes, ou par balayage depuis le bord.
- **[À VÉRIFIER]** Comportement sous One UI 8 (gestes et trois boutons).

**F-2.5 Chargement à la demande.** Seul le chapitre courant (et, si besoin, le suivant et le précédent) est chargé dans le moteur. Pendant le chargement, la page est vide, couleur `Black`, sans indicateur animé (`CHARTE.md` A.6).

**F-2.6 Rotation.** La rotation conserve la position de lecture (ancre décrite en F-5). Pas de verrouillage d'orientation propre à l'app, comme DEC-0016.

### F-3 Taille du texte et typographie

**F-3.1 Taille du texte.** Réglage de taille à cinq paliers (proposition), accessible dans le menu « Plus » du lecteur. Le réglage est global et mémorisé. Changer la taille conserve le passage lu (ancre, F-5), pas le numéro de page.

**F-3.2 Police. [À DÉCIDER]** (a) police système sans empattement (`FontFamily.Default`), seule police (proposition, conforme à la charte) ; (b) choix entre police système et police avec empattement, réglage global ; (c) polices du livre, si elles sont embarquées et libres (écarté par défaut : sécurité et cohérence).

**F-3.3 Couleurs du livre. [À DÉCIDER]** Les couleurs, fonds et polices définis par le livre sont ignorés (proposition) : le texte prend les jetons `TextPrimary` sur `Black`, ou l'inverse en mode nuit. Option « Couleurs du livre » désactivée par défaut, si le porteur la souhaite.

### F-4 Fichiers récents

**F-4.1 Accueil.** L'accueil affiche, sous « Ouvrir un fichier », le dernier livre ouvert, en texte seul :
- Ligne 1 : titre du livre (métadonnée `dc:title` du package ; à défaut, nom du fichier), passage à la ligne, sans troncature, en `LabelStyle` et `TextPrimary` (dérogation de même nature que DEC-0023, à consigner) ;
- Ligne 2 (proposition) : « 42 % lu » en `TextSecondary`.
- Aucun livre ouvert : pas d'entrée affichée.

**F-4.2 Historique facultatif.** Comme DEC-0024 : un seul récent par défaut ; réglage « Liste des livres » désactivé par défaut, liste limitée à 10 livres.

**F-4.3 Ouverture.** Appui court : ouverture à la position mémorisée (F-5).

**F-4.4 Menu d'appui long.** Fenêtre de menu (`CHARTE.md` A.6), titre = titre du livre, actions : « Retirer de la liste », « Partager ». « Retirer de la liste » supprime l'entrée, la position et les marque-pages, et libère l'autorisation. Le fichier n'est **jamais** supprimé. Pas de confirmation (DEC-0025, reprise).

**F-4.5 Identification.** Un livre est identifié par son URI. Identification secondaire par le nom du fichier (DEC-0023, reprise), pour retrouver la position si le même livre est rouvert par une autre URI. Pas de lecture d'empreinte du contenu.

### F-5 Reprise de lecture

**F-5.1 Ancre de position.** La position est mémorisée sous forme d'**ancre** : rang du chapitre dans la spine, décalage vertical ou rang de page écran (selon F-2.2) à l'intérieur du chapitre, et progression en pourcentage. **[À DÉCIDER]** ancre au format CFI (standard EPUB) ou ancre maison ; proposition : ancre maison (rang + décalage), plus simple à tester.

**F-5.2** Mémorisation à la sortie du lecteur, à la mise en arrière-plan et après une pause de lecture (délai d'1 s, comme F-5.1 de Épure PDF). Pas d'écriture à chaque défilement.

**F-5.3** À la réouverture, le lecteur s'ouvre à cette ancre, sans écran intermédiaire.

**F-5.4** Un livre ouvert pour la première fois s'ouvre au début du premier chapitre de la spine (et non à la couverture, sauf décision).

**F-5.5** Si le chapitre mémorisé n'existe plus (livre remplacé), ouverture au début du livre, sans message. Si l'ancre est hors limites dans un chapitre, ouverture au début de ce chapitre.

### F-6 Commandes du lecteur

**F-6.1 Affichage.** Un toucher simple au centre de la page affiche les commandes ; un nouveau toucher les masque. Apparition et disparition immédiates.

**F-6.2 Masquage automatique.** Les commandes disparaissent après 4 s sans interaction, ou dès que le texte défile ou change de page (proposition, comme DEC-0031).

**F-6.3 Contenu.**
- En haut : titre du livre (`LabelStyle`, `TextPrimary`, passage à la ligne autorisé).
- En bas : progression « 42 % » (`LabelStyle`, `TextSecondary`) et les actions texte « Table », « Rechercher », « Marque-page », « Plus ».
- « Plus » ouvre une fenêtre de menu : « Taille du texte », « Mode nuit », « Partager », « Écran allumé ».
- Cibles ≥ 48 dp, texte seul.

**F-6.4 Bouton Retour.** Dans l'ordre : ferme la fenêtre ouverte, puis masque les commandes, puis quitte le lecteur (retour à l'accueil, ou à l'app d'origine si ouverture par `VIEW`). Implémentation par `BackHandler`.

**F-6.5 Pages précédente et suivante** (si F-2.2 (b) est retenu) : actions texte « Page précédente » et « Page suivante » dans les commandes, en plus du balayage.

### F-7 Progression et navigation dans le livre

**F-7.1 Progression.** Indicateur « 42 % » calculé à partir de la position dans la spine et dans le chapitre (domaine pur, testé). Affiché avec les commandes.

**F-7.2 Aller à un chapitre.** Par la table des matières (F-12) uniquement en version 1. Pas de saut à un pourcentage ou à un numéro de page : un livre reflowable n'a pas de pagination stable (**[À DÉCIDER]** si l'on veut un curseur de progression, voir §9.1).

### F-8 Mode nuit

**F-8.1** Action « Mode nuit » dans le menu « Plus » et dans les Paramètres. Fond `Black`, texte `TextPrimary`, liens et titres en `TextPrimary`. L'interface reste inchangée.

**F-8.2** Le mode nuit est appliqué par des **styles imposés** au contenu (surcharge CSS, `!important`), et non par un filtre de couleur sur l'écran : le texte garde ses couleurs de rendu et reste lisible quelles que soient les couleurs du livre.

**F-8.3 Images. [À DÉCIDER]** (a) images affichées telles quelles (proposition) ; (b) images atténuées ; (c) images inversées, comme Épure PDF (F-8.2 de Épure PDF). Proposition : (a), pour respecter les illustrations.

**F-8.4** Réglage global, mémorisé, désactivé par défaut (comme DEC-0029).

### F-9 Écran allumé

Reprise de F-9 de Épure PDF : option « Écran allumé pendant la lecture », activée par défaut (DEC-0030), effet limité au lecteur, aucune permission.

### F-10 Marque-pages

**F-10.1** Action « Marque-page » dans les commandes : ajoute la position courante ; si elle est déjà marquée, l'action devient « Retirer le marque-page ».

**F-10.2** Un marque-page est une ancre (F-5.1). Il est identifié par le titre du chapitre et la progression : « Chapitre 3 — Titre du chapitre, 42 % ».

**F-10.3** Écran « Marque-pages » : liste triée par position dans le livre. Appui court = aller à la position ; appui long = « Retirer ». Liste vide : « Aucun marque-page ». Retrait immédiat, sans confirmation (DEC-0035, reprise).

**F-10.4** Stockage local, par livre (§7). Les marque-pages ne sont pas écrits dans le fichier EPUB.

### F-11 Recherche de texte

**F-11.1** Action « Rechercher » : champ de recherche (`CHARTE.md` A.6), clavier ouvert à l'arrivée. Entrée = lancer la recherche.

**F-11.2** Recherche insensible à la casse. Insensibilité aux accents **[À DÉCIDER]**, à vérifier sur l'appareil.

**F-11.3** Portée. **[À DÉCIDER]** (a) chapitre courant seulement, avec la recherche native de la WebView ; (b) tout le livre, en lisant chaque chapitre à la demande, sans stocker le texte (proposition, fonction annoncée comme telle).

**F-11.4** Résultat : saut à la première occurrence ; compteur « 3 / 17 » ; actions « Précédent » et « Suivant » ; « Aucun résultat » si rien n'est trouvé. Signalement de l'occurrence : négatif, comme DEC-0035 (F-11.4 de Épure PDF).

**F-11.5** La recherche sur tout le livre ne bloque pas l'interface et peut être interrompue par Retour. Pas d'indicateur animé ; le compteur apparaît à la fin.

**F-11.6** Un livre dont les chapitres ne contiennent pas de texte (images seules) affiche : « Ce livre ne contient pas de texte. »

### F-12 Table des matières

**F-12.1** Action « Table » : liste hiérarchique construite à partir du fichier `nav` (EPUB 3) ou du `NCX` (EPUB 2), retrait de 16 dp par niveau, texte seul ; appui court = aller à la position du titre.

**F-12.2** Si le livre n'a pas de table des matières, ou si elle est illisible : « Aucune table des matières ». L'action reste visible en `TextSecondary`.

**F-12.3** Les liens de la table pointent uniquement vers l'intérieur de l'archive (S-3). Un lien qui ne peut pas être résolu est ignoré sans message.

### F-13 Partage

**F-13.1** Action « Partager » (menu « Plus » et menu d'appui long d'un récent) : `ACTION_SEND` avec l'URI du livre et un droit de lecture temporaire. Aucun réseau propre à l'app.

**F-13.2** Si le droit sur l'URI ne permet pas de la transmettre : « Ce fichier ne peut pas être partagé depuis Épure EPUB. » **[À VÉRIFIER]**.

### F-14 Liens et contenu actif

**F-14.1 Liens internes.** Un lien vers un autre chapitre ou une ancre du même livre déplace la lecture. Il est ajouté à l'historique de retour (action « Retour au passage » dans les commandes, **[À DÉCIDER]** : proposition oui, une seule étape).

**F-14.2 Liens externes.** Un lien vers une adresse web, un courriel ou tout autre schéma n'est **pas ouvert**. Message en texte : « Lien externe non ouvert : Épure EPUB ne se connecte pas à Internet. » Aucun navigateur n'est lancé.

**F-14.3 Contenu actif.** Les scripts du livre ne sont pas exécutés (S-1). Un livre qui dépend d'un script pour afficher son texte est affiché sans le script ; le texte reste lisible s'il est présent dans le XHTML.

### F-15 Livres protégés, mal formés et difficiles

**F-15.1 DRM.** Si un chiffrement de contenu est détecté (`META-INF/encryption.xml` décrivant du contenu chiffré hors polices, ou DRM connu) : « Ce livre est protégé et ne peut pas être ouvert. » Aucune tentative de déchiffrement.

**F-15.2 Chapitre mal formé.** Un chapitre dont le XML est invalide est affiché tel quel si le moteur peut le lire ; sinon « Ce chapitre ne peut pas être affiché. » à sa place, et la lecture continue au chapitre suivant. Pas de plantage.

**F-15.3 Ressource manquante.** Une image absente est remplacée par un rectangle de texte « Image indisponible » en `TextSecondary`. Pas de plantage.

**F-15.4 Livre très long.** Un livre de plusieurs centaines de pages s'ouvre sans chargement de l'intégralité du texte en mémoire (T-6).

### F-16 Paramètres et « À propos »

**F-16.1 Paramètres** (liste de réglages, `CHARTE.md` A.6) : « Mode nuit », « Écran allumé pendant la lecture », « Liste des livres » (facultative, F-4.2), « À propos ». Rien d'autre sans nouvelle exigence. Taille du texte : dans le menu « Plus » du lecteur (F-3.1), et non dans les Paramètres.

**F-16.2 « À propos »** : nom, version, licence GPL v3, lien « Dépôt GitHub » (ouvert par le navigateur, sans permission réseau propre à l'app), section confidentialité et sécurité (texte de T-2 et T-13), licences des bibliothèques et du moteur de rendu (et de la WebView, si retenue).

### F-17 Autres formats

Hors de cette app : PDF (Épure PDF), CBZ et CBR (applications séparées, DEC-0037). Un EPUB protégé par DRM n'est pas un format pris en charge (F-15.1).

---

## 5. Exigences transversales

**T-1 Aucune permission réseau.**
- `INTERNET` et `ACCESS_NETWORK_STATE` absentes du manifeste source **et** du manifeste fusionné.
- Contrôle à chaque ajout ou mise à jour de dépendance par `aapt2` (DEC-0032, reprise).
- Une permission injectée par une bibliothèque est retirée par `tools:node="remove"`, ou la bibliothèque est écartée (décision consignée).
- Point de vigilance propre à la WebView : le composant système ne reçoit aucune donnée de l'app, mais il doit être vérifié comme les autres dépendances (**[À VÉRIFIER]** au prototype).

**T-2 Confidentialité.** Aucune donnée ne quitte l'appareil. Aucune bibliothèque d'analytics, de suivi de plantage ou de publicité. Aucune statistique de lecture. L'app conserve seulement : URI, titre, nom du fichier, progression, position, marque-pages, réglages. Ni la date d'ouverture ni la taille du fichier ne sont conservées ni affichées. Le contenu du livre n'est jamais stocké, sauf copie temporaire si F-1.4 (a) est retenu.

**T-3 Accès aux fichiers.** Uniquement par le SAF et les intents `VIEW` et `SEND`. Jamais de permission de stockage. L'app ne parcourt pas le stockage et n'écrit dans aucun fichier de l'utilisateur.

**T-4 Aucun service en arrière-plan, aucune notification.** Pas de `Service`, pas de `WorkManager`, pas de `POST_NOTIFICATIONS`.

**T-5 Sobriété.** Dépendances au strict minimum ; chacune justifiée dans `DECISIONS.md` avant ajout, avec sa licence et sa compatibilité GPL v3.

**T-6 Mémoire.**
- Un seul chapitre complet chargé dans le moteur à la fois (proposition) ; les chapitres voisins sont chargés à la demande.
- Taille maximale d'un chapitre XHTML lu en une fois : **[À DÉCIDER]** (proposition : 5 Mo décompressés) ; au-delà, « Ce chapitre ne peut pas être affiché. » (F-15.2).
- Cache de chapitres ou d'images borné en octets (proposition : 16 Mo, après mesure).
- Aucun plantage pour manque de mémoire : le chapitre concerné est remplacé par un message.

**T-7 Performances (cibles, à mesurer sur l'appareil de référence).**
- Premier texte visible en moins de 1 s pour un livre de 300 pages ; moins de 2 s pour un livre de 1 000 pages (proposition, à aligner sur les cibles d'Épure PDF après mesure).
- Changement de chapitre sans blocage perceptible.
- Ouverture et lecture de l'archive en dehors du fil principal.

**T-8 Robustesse.** Aucun plantage pour : fichier absent, droit retiré, EPUB mal formé, livre protégé, chapitre illisible, ressource manquante, archive suspecte, mémoire insuffisante, rotation, mise en arrière-plan, arrêt du processus par le système (au retour, réouverture à la position mémorisée).

**T-9 Conformité à la charte.** `CHARTE.md` Partie A s'applique (reprise d'Épure PDF) ; Partie B adapte les règles au lecteur d'EPUB.

**T-10 Contraintes Android.** Edge-to-edge géré par `WindowInsets` ; retour prédictif ; `allowBackup="false"` ; une seule activité ; `ViewModel` + `StateFlow` ; pas de framework d'injection (`AppContainer`).

**T-11 Accessibilité.** Commandes, récents, table des matières et fenêtres lisibles par TalkBack. Le texte du livre est lu par TalkBack lorsque le moteur le permet (**[À VÉRIFIER]**). Chaque position de lecture a une description (« Chapitre 3 sur 12 »).

**T-12 Architecture et tests.**
- Paquets : `ui/`, `data/` (récents, positions, marque-pages, réglages), `system/` (moteur de rendu EPUB, lecture de l'archive, SAF, intents), `domain/` (logique pure).
- Logique couverte par des tests unitaires dans `domain/` : calcul de la progression, ancre de position et restauration, aplatissement de la table des matières, validation des chemins d'archive, limites de taille et de nombre d'entrées, politique des URL (autorisées et refusées), réglages de taille du texte, limite des récents, identification secondaire.
- Le moteur de rendu est isolé derrière une interface dans `system/`, pour pouvoir en changer sans toucher à l'interface (même principe que `PdfEngine` dans Épure PDF).

**T-13 Sécurité du contenu.** Le livre est traité comme un fichier hostile. Exigences S-1 à S-7 ci-dessous.

---

## 5 bis. Exigences de sécurité du contenu

**S-1 Aucun script.** Le moteur n'exécute aucun JavaScript. Dans une WebView : exécution désactivée. Un livre contenant du script s'ouvre sans lui.

**S-2 Aucune ressource hors de l'archive.** Toute requête (image, police, feuille de style, script, iframe) dont l'adresse n'est pas à l'intérieur de l'archive est refusée. Le refus est silencieux pour l'image (F-15.3).

**S-3 Liens.** Seuls les liens internes à l'archive déplacent la lecture (F-14.1). Les autres sont refusés (F-14.2). Aucune navigation de la fenêtre vers une autre adresse.

**S-4 Archive validée avant lecture.**
- Chaque chemin d'entrée est normalisé ; un chemin absolu, contenant `..` ou sortant de l'archive est refusé (protection contre « zip slip ») ;
- nombre d'entrées limité (proposition : 5 000) ;
- taille totale décompressée limitée (proposition : 200 Mo) ; taille d'une entrée limitée (proposition : 50 Mo) ;
- rapport de compression limité (proposition : 100 pour 1), contre les bombes ZIP ;
- le fichier `mimetype` doit être la première entrée, non compressé, et valoir `application/epub+zip`. Décision (DEC-EE-0017) : un `mimetype` absent, mal placé, compressé ou de valeur fausse n'empêche pas la lecture ; le livre s'ouvre avec un avertissement non bloquant, à condition que `META-INF/container.xml` et le fichier de package soient lisibles. Sinon : « Ce fichier n'est pas un EPUB lisible. » (F-1.5).

**S-5 XML sans entités externes.** Les fichiers XML (`container.xml`, package, `nav`, `NCX`, chapitres) sont lus avec un parseur qui n'accepte ni DTD, ni entités externes, ni résolution de réseau.

**S-6 Aucun stockage du moteur.** Aucun cache de page, aucun cookie, aucun stockage local ou base de données du moteur de rendu (WebView : `cacheMode` sans cache, `domStorageEnabled` faux, `databaseEnabled` faux, cookies désactivés). Le contenu du livre ne reste pas dans le cache de l'app après fermeture (T-2).

**S-7 Aucun accès fichier depuis le contenu.** Le moteur n'accède ni aux fichiers de l'app ni au système de fichiers ; les ressources ne sont servies que depuis l'archive ouverte par l'app, par interception des requêtes.

---

## 6. Moteur de rendu — Tranché : option A (DEC-EE-0015)

**Décision** : option A, WebView système, configurée selon S-1 à S-7. Encapsulée derrière une interface dans `system/`. Options B et C conservées dans le tableau ci-dessous à titre de référence.

Conditions de mise en œuvre : JavaScript, stockage DOM, base de données, cookies, zoom et cache désactivés ; accès aux fichiers et au contenu refusés ; seules les ressources de l'archive sont servies ; aucune navigation ; aucune permission ajoutée au manifeste fusionné.

Points à traiter pendant l'implémentation : styles du livre neutralisés par défaut (F-3.3) ; « Image indisponible » pour une image absente (F-15.3) ; message pour un lien externe (F-14.2) ; mode de défilement (F-2.2) à trancher.

Résultats des prototypes A et C, sur le jeu de test (annexe A) : consignés dans `DECISIONS.md`, DEC-EE-0009.

| Option | Description | Points forts | Points de vigilance |
|---|---|---|---|
| A — WebView système, configurée strictement | Affichage HTML/CSS par le composant Android, avec S-1 à S-7 | Intégrée à Android, aucune dépendance, mise en forme fidèle | Composant système mis à jour par Google ; configuration à maîtriser (JS, réseau, cache) ; mémoire à mesurer ; interception des requêtes à écrire |
| B — Readium Kotlin toolkit | Bibliothèque ouverte, prévue pour les lecteurs EPUB | Lecture EPUB 2/3 complète, navigation et ancres prêtes | Dépendances à justifier ; licence à confirmer avant ajout (annoncée BSD-3) ; manifeste fusionné à contrôler ; couche de sécurité à vérifier |
| C — Rendu maison en texte seul | Lecture du XHTML, conversion en paragraphes Compose, sans HTML ni CSS ni WebView | Pas de JavaScript ni de réseau possibles ; surface d'attaque minimale ; charte respectée à l'identique | Mise en forme réduite (images, tableaux, styles perdus) ; travail important de parsing |

**Critères du choix** (appliqués aux prototypes de session 1a) : sécurité (S-1 à S-7 respectées), fidélité du rendu, mémoire, temps d'ouverture, poids, permissions du manifeste fusionné, respect de la charte.

---

## 7. Données stockées

| Donnée | Portée | Contenu |
|---|---|---|
| Récents | Par livre | URI, titre, nom du fichier, progression |
| Position de lecture | Par livre | Ancre (F-5.1) |
| Marque-pages | Par livre | Liste d'ancres avec titre de chapitre |
| Réglages | Globale | Taille du texte, mode nuit, écran allumé, liste des livres |

- Stockage local, dans l'espace privé de l'app ; sauvegarde Android désactivée.
- Technique : DataStore Preferences, données par livre sérialisées avec `org.json`, comme Épure PDF (DEC-0027, reprise).
- Retirer un livre des récents supprime toutes ses données (F-4.4).
- Aucun contenu du livre n'est stocké.

---

## 8. Permissions et accès

| Permission / accès | Fonction | Obtention |
|---|---|---|
| Sélecteur système (`ACTION_OPEN_DOCUMENT`) | Ouvrir un livre | Choix de l'utilisateur, à chaque ouverture |
| Autorisation persistante sur une URI | Rouvrir le dernier livre | Accordée par le système lors du choix ; libérée au retrait de la liste |
| Filtres d'intent `VIEW` et `SEND` (`application/epub+zip`) | Ouverture depuis une autre app | Aucune permission ; droit temporaire fourni par l'app d'origine |
| `FLAG_KEEP_SCREEN_ON` | Écran allumé | Aucune permission |
| `INTERNET`, `ACCESS_NETWORK_STATE` | — | **Non déclarées**, absentes du manifeste fusionné |
| `MANAGE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, `READ_MEDIA_*` | — | **Non déclarées** |
| `POST_NOTIFICATIONS`, `WAKE_LOCK`, `QUERY_ALL_PACKAGES`, `SYSTEM_ALERT_WINDOW` | — | **Non déclarées** |

Conséquence : **aucune permission déclarée** (objectif). Pas d'écran d'autorisation (`CHARTE.md` A.10).

---

## 9. Points ouverts

### 9.1 [À DÉCIDER]

| Réf. | Sujet | Jalon | Proposition |
|---|---|---|---|
| F-1.4 | Copie temporaire si descripteur non repositionnable | 2 | Copie en cache privé, supprimée à la fermeture |
| F-2.2 | Défilement continu ou pagination | 1 | Pagination horizontale, avec commandes de page |
| F-3.2 | Police | 3 | Police système sans empattement |
| F-3.3 | Couleurs du livre | 3 | Ignorées ; option désactivée |
| F-5.1 | Ancre de position : maison ou CFI | 2 | Ancre maison (rang + décalage) |
| F-6.5 | Pages précédente et suivante | 3 | Oui, si pagination |
| F-8.3 | Images en mode nuit | 3 | Affichées telles quelles |
| F-11.2 | Recherche insensible aux accents | 4 | Si le moteur le permet |
| F-11.3 | Portée de la recherche | 4 | Tout le livre, sans stockage du texte |
| F-14.1 | « Retour au passage » après un lien interne | 3 | Oui, une étape |
| F-16.1 | Liste des livres (facultative) | 2c | Désactivée par défaut, 10 livres |
| S-4, T-6 | Limites de taille et de nombre d'entrées | 5 | Valeurs de la section S-4, à mesurer |
| T-6 | Taille maximale d'un chapitre | 5 | 5 Mo décompressés |
| F-3.1 | Nombre de paliers de taille du texte | 3 | Cinq |
| F-2.3 | Marges et interligne | 3 | 24 dp et 1,4 |
| F-4.1 | Titre affiché dans les récents | 2 | Titre du package ; nom du fichier à défaut |
| F-10.2 | Libellé des marque-pages | 4 | Chapitre et progression |

### 9.2 [À VÉRIFIER] sur l'appareil

Tous les points restent à traiter.

| Réf. | Point |
|---|---|
| F-1.3 | Persistance des droits reçus par `VIEW` selon la source |
| F-2.4 | Mode immersif sous One UI 8 |
| F-13.2 | Repartage d'une URI selon la source |
| S-1 à S-7 | Test de chaque protection sur les EPUB malveillants (annexe A) |
| T-1 | Manifeste fusionné : aucune permission réseau, y compris via la WebView si retenue |
| T-11 | Lecture du texte par TalkBack |
| §6 | Mesures de mémoire et de temps pour chaque option du prototype |

---

## Annexe A — Jeu de test EPUB

Conservé **hors du dépôt** (aucun livre personnel ou sous droits d'auteur versionné). Les EPUB inclus dans le dépôt sont générés par nous ou sous licence libre, avec mention de la source.

| N° | Type | Ce qu'il teste |
|---|---|---|
| 1 | Roman EPUB 3, 300 pages, avec table des matières `nav` | Usage principal, progression, table |
| 2 | Essai EPUB 2, avec `NCX` et sans `nav` | Compatibilité EPUB 2, table via NCX |
| 3 | Livre avec images nombreuses et grandes | Mémoire, images, S-6 |
| 4 | Livre avec tableaux et listes | F-2.1 |
| 5 | Livre avec JavaScript dans un chapitre | S-1, F-14.3 |
| 6 | Livre avec images et feuilles de style distantes (adresses `https://`) | S-2, F-15.3 |
| 7 | Livre avec liens externes et liens internes | S-3, F-14 |
| 8 | Livre avec chemins d'archive `../` | S-4, zip slip |
| 9 | Archive ZIP compressée à l'extrême (bombe) | S-4, F-1.5 |
| 10 | Livre avec entité externe dans un XML | S-5 |
| 11 | Livre tronqué ou corrompu (généré) | F-1.5, F-15.2 |
| 12 | Fichier non EPUB renommé en `.epub` | F-1.5 |
| 13 | Livre protégé par DRM (structure de test, sans contenu réel) | F-15.1 |
| 14 | Livre avec un seul chapitre, sans table | Cas limite, F-12.2 |
| 15 | EPUB d'une pièce jointe d'e-mail et d'un téléchargement | F-1.2, F-1.4 |

---

## Historique

- 0.1 (10/11/2026) : première version de déclinaison, à partir de `SPEC.md` d'Épure PDF 0.1 (et de ses jalons 0 à 5). Non validée par le porteur.
- 0.2 (11/11/2026) : §6 tranché (option A, DEC-EE-0015) ; retrait du point §6 de §9.1. Mise à jour faite par le porteur.
- 0.3 (11/11/2026) : S-4 « mimetype » tranché (ouverture avec avertissement, DEC-EE-0017) ; retrait du point de §9.1. Mise à jour faite par le porteur.
