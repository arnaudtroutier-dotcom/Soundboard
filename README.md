# Soundboard — Application Android

Application soundboard personnalisable inspirée d'AudioTiles, développée en Kotlin + Jetpack Compose.

---

## Fonctionnalités

- **Multi-soundboards** : Créez autant de soundboards que vous voulez, naviguez entre eux via les onglets en haut.
- **Tuiles libres** : Chaque tuile se déplace par glisser-déposer et se redimensionne librement (poignée en bas à droite).
- **Sons multiples** : Associez un ou plusieurs fichiers audio à une tuile → un son est joué aléatoirement à chaque appui.
- **Sons superposés** : Plusieurs tuiles peuvent jouer en même temps.
- **Mode édition** : Activé via l'icône ✏️ — permet de déplacer, redimensionner et éditer les tuiles.
- **Éditeur de tuile** : Nom, couleur (12 présets + personnalisé), volume, loop, comportement au clic (Pause / Arrêt total).
- **Sauvegarde automatique** : Tout est persisté via Room (SQLite local).
- **Renommer / Supprimer** les soundboards depuis le menu ⋮.

---

## Prérequis

- **Android Studio Hedgehog** (2023.1.1) ou plus récent
- **JDK 17**
- Android SDK avec `compileSdk 34`

---

## Installation

1. **Ouvrez le projet** dans Android Studio :
   `File → Open → sélectionnez le dossier SoundboardApp`

2. **Sync Gradle** : Android Studio proposera automatiquement de synchroniser les dépendances. Cliquez "Sync Now".

3. **Branchez votre appareil** (ou démarrez un émulateur Android 8.0+).

4. **Lancez** : cliquez ▶ Run ou `Shift+F10`.

L'APK sera compilé et installé automatiquement.

---

## Structure du projet

```
app/src/main/java/com/soundboard/app/
├── MainActivity.kt
├── audio/
│   └── AudioManager.kt          ← Gestion MediaPlayer par tuile
├── data/
│   ├── dao/Daos.kt               ← Requêtes Room
│   ├── database/AppDatabase.kt   ← Singleton BDD
│   └── entities/Entities.kt      ← Soundboard, Tile, SoundFile
├── ui/
│   ├── components/
│   │   ├── SoundTile.kt          ← Tuile draggable + resize
│   │   └── TileEditorDialog.kt   ← Éditeur complet
│   ├── screens/
│   │   ├── MainScreen.kt         ← Écran principal + onglets
│   │   └── SoundboardCanvas.kt   ← Canvas des tuiles
│   └── theme/Theme.kt            ← Palette sombre ambrée
└── viewmodel/
    └── SoundboardViewModel.kt    ← État + logique
```

---

## Utilisation

### Créer un soundboard
Appuyez sur **＋** (haut à droite) → saisissez un nom → Créer.

### Ajouter une tuile
Menu ⋮ → "Ajouter une tuile" (active le mode édition automatiquement).

### Configurer une tuile
En mode édition → **appui long** sur une tuile → éditeur :
- **Sons** : "Ajouter" → sélecteur de fichiers audio (MP3, OGG, WAV, FLAC…)
- **Couleur** : 12 présets ou personnalisée
- **Loop** : active la boucle
- **Comportement clic** : Pause/Reprendre ou Arrêt total
- **Volume** : slider 0–100%

### Déplacer une tuile
Mode édition → **glisser** la tuile n'importe où sur le canvas.

### Redimensionner une tuile
Mode édition → **glisser la poignée ↗** en bas à droite.

### Jouer un son
Mode normal (édition désactivée) → **appuyez** sur une tuile.

### Tout arrêter
Bouton ⏹ dans la barre du haut.

---

## Permissions requises

- `READ_MEDIA_AUDIO` (Android 13+) ou `READ_EXTERNAL_STORAGE` (Android ≤12) : pour accéder aux fichiers audio.

La permission est demandée automatiquement à l'import du premier son.

---

## Dépendances principales

| Bibliothèque | Version | Usage |
|---|---|---|
| Jetpack Compose BOM | 2024.01.00 | UI |
| Room | 2.6.1 | Persistance |
| Material3 | via Compose BOM | Composants UI |
| Kotlin Coroutines | 1.7.3 | Async |
| KSP | 1.9.20-1.0.14 | Génération de code Room |
