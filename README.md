# Épure EPUB

Lecteur EPUB minimaliste pour Android. Il lit un livre sans distraction, entièrement hors ligne.

> État : jalon 0 (mise en place du projet). L'application ne sait pas encore lire de livre.

## Principes

- Lire sans distraction : fond noir, texte seul, aucune publicité, aucune icône superflue.
- Hors ligne : l'application ne se connecte à aucun service.
- Un livre est traité comme un fichier non fiable (voir ci-dessous).

## Confidentialité et sécurité

- **Aucun accès à Internet.** L'application ne déclare ni la permission `INTERNET` ni `ACCESS_NETWORK_STATE`, ni dans son code source ni dans le manifeste final. Le contrôle se fait avec `aapt2 dump permissions` après chaque build.
- **Aucune collecte de données.** Pas d'outil d'analyse, de suivi de plantage ni de publicité.
- **Accès aux fichiers par le sélecteur système seulement.** L'application ne voit que le livre que tu choisis. Aucune permission de stockage n'est demandée.
- **Données sur l'appareil.** Ne sont enregistrés que l'adresse du livre, son titre, la position de lecture, les marque-pages et les réglages. Le contenu du livre n'est pas copié durablement. La sauvegarde automatique d'Android est désactivée.
- **Livre traité comme non fiable.** Un EPUB est une archive de pages web fournie par un tiers. Il n'exécute aucun script, ne charge aucune ressource depuis Internet, n'ouvre aucun lien externe et n'accède pas au système de fichiers. L'archive est vérifiée avant lecture (chemins, tailles, nombre d'entrées).

Les règles détaillées sont dans `docs/SPEC.md` et `docs/CHARTE.md`, et les choix techniques dans `docs/DECISIONS.md`.

## Compiler

Prérequis : Android Studio (avec son JDK intégré) et le SDK Android 36.

```
./gradlew assembleDebug          # build
./gradlew testDebugUnitTest      # tests unitaires
./gradlew lint                   # lint
./gradlew installDebug           # installation sur un appareil connecté
```

Sous Windows, utiliser `gradlew.bat` à la place de `./gradlew`.

## Licence

Épure EPUB est un logiciel libre distribué sous la [licence GNU GPL v3](LICENSE).
