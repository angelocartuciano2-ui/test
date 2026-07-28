To use a more realistic hangman image, add image files under `src/main/resources/pictures` with one of these names (the loader prefers them in order):

1) realistic-<stage>.png  -> e.g. `realistic-0.png`, `realistic-1.png`, ..., `realistic-6.png`
2) realistic-hangman.png -> a single realistic image used for all stages

If none of the above exist, the app falls back to the existing `<stage>-hangman.png` images (e.g. `0-hangman.png`).

Recommendations:
- Format: PNG (supports transparency) or JPG
- Suggested resolution: 800x600 or larger for clarity; the ImageView will scale it down
- Prefer a transparent background so the game's background shows through

After adding images, restart the application. If the video isn't displaying or the image appears too dark, try increasing `MediaView` opacity in `game-view.fxml` or the controller.
