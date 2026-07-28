package fr.quentincillierre.hangman.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import fr.quentincillierre.hangman.Category;
import fr.quentincillierre.hangman.Difficulty;
import fr.quentincillierre.hangman.model.HangmanModel;
import fr.quentincillierre.hangman.model.WordRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import fr.quentincillierre.hangman.util.SoundManager;

public class GameController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label wordLabel;

    @FXML
    private Label resultLabel;

    @FXML
    private Label hintLabel;

    @FXML
    private Label difficultyLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private ImageView hangmanImageView;

    @FXML
    private GridPane keyboardGrid;

    @FXML
    private MediaView backgroundVideoView;

    @FXML
    private Label attemptsLabel;

    private MediaPlayer backgroundPlayer;
    private Media backgroundMedia;

    private HangmanModel model;
    // Optional clue associated with the current word (e.g. "fastest land animal")
    private String currentClue;

    private Difficulty difficulty;
    private Category currentCategory;

    public void start(Category category, Difficulty difficulty) {
        this.currentCategory = category;
        this.difficulty = difficulty;

        WordRepository wordRepository;
        switch (category) {
            case MUSIC:
                wordRepository = new WordRepository("/music.txt");
                break;
            case FOOD:
                wordRepository = new WordRepository("/food.txt");
                break;
            case COUNTRY:
                wordRepository = new WordRepository("/country.txt");
                break;
            default:
                throw new IllegalArgumentException("Unsupported category: " + category);
        }

        // Words in resources may include an optional clue separated by '|', e.g.
        // "CHEETAH|Fastest land animal". Parse and keep the clue for the UI.
        String raw = wordRepository.getRandomWord(difficulty);
        String wordOnly = raw;
        currentClue = null;
        if (raw != null && raw.contains("|")) {
            String[] parts = raw.split("\\|", 2);
            wordOnly = parts[0].trim();
            currentClue = parts[1].trim();
        }
        this.model = new HangmanModel(wordOnly);
        applyDifficultyTheme(difficulty);
        if (keyboardGrid != null) {
            keyboardGrid.setDisable(false);
        }
        generateKeyboard();
        refreshUI();
        loadBackgroundVideo();
        resultLabel.setOpacity(0);
        if (difficultyLabel != null) {
            difficultyLabel.setText(toDisplayDifficulty(difficulty));
        }
        if (categoryLabel != null) {
            categoryLabel.setText(toDisplayCategory(category));
        }
    }

    private String getHintText() {
        // Prefer an explicit clue if provided in the word resource.
        if (currentClue != null && !currentClue.isEmpty()) {
            return "Clue: " + currentClue;
        }

        if (model == null) {
            return "Clue: Choose a category and start playing.";
        }

        String word = model.getWordToGuess().replaceAll("[^A-Z]", "");
        if (word.isEmpty()) {
            return "Clue: No clue available.";
        }
        // Fallback: provide a simple descriptive hint (letters + first letter)
        char firstLetter = word.charAt(0);
        String base = String.format("%d letters, starts with '%s'", word.length(), firstLetter);
        if (difficulty == Difficulty.EASY) {
            return "Easy — " + base;
        } else if (difficulty == Difficulty.MEDIUM) {
            return "Medium — " + base;
        }
        return "Hard — " + base;
    }

    private void applyDifficultyTheme(Difficulty difficulty) {
        String accentColor;
        String resultColor;

        switch (difficulty) {
            case EASY:
                accentColor = "#4FD1C5";
                resultColor = "#BEE3F8";
                break;
            case MEDIUM:
                accentColor = "#F6AD55";
                resultColor = "#F7F2C1";
                break;
            default:
                accentColor = "#EF476F";
                resultColor = "#FFB3C1";
                break;
        }

        if (mainPane != null) {
            mainPane.setStyle("-fx-background-color: rgba(6, 12, 24, 0.26); -fx-background-radius: 24; -fx-border-color: " + accentColor + "; -fx-border-width: 2px; -fx-border-radius: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 18, 0.25, 0, 0);");
        }
        if (hintLabel != null) {
            hintLabel.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-weight: 600;");
        }
        if (difficultyLabel != null) {
            difficultyLabel.setStyle("-fx-text-fill: " + accentColor + "; -fx-background-color: rgba(255,255,255,0.08); -fx-padding: 6 12; -fx-background-radius: 12; -fx-font-weight: bold;");
        }
        if (resultLabel != null) {
            resultLabel.setStyle("-fx-text-fill: " + resultColor + "; -fx-font-weight: bold;");
        }
    }

    /**
     * Loads a looping background video into the MediaView. Each difficulty
     * has its own dedicated background video; falls back to a shared list
     * only if difficulty is not yet set.
     */
    private void loadBackgroundVideo() {
        if (backgroundVideoView == null) {
            return;
        }

        String[] candidates = getBackgroundVideoCandidatesForDifficulty();
        loadBackgroundVideoCandidate(candidates, 0);
    }

    private void loadBackgroundVideoCandidate(String[] candidates, int index) {
        if (index >= candidates.length) {
            System.err.println("No playable background video found for difficulty: " + difficulty);
            backgroundVideoView.setOpacity(0);
            return;
        }

        String requestedPath = candidates[index];
        System.err.println("Trying background candidate " + index + " for difficulty " + difficulty + ": " + requestedPath);
        URL mediaUrl = resolveResource(requestedPath);
        if (mediaUrl == null) {
            System.err.println("Resource not found: " + requestedPath);
            loadBackgroundVideoCandidate(candidates, index + 1);
            return;
        }

        Media media = createMediaFromResource(mediaUrl);
        if (media == null) {
            System.err.println("Unable to create media from resource: " + mediaUrl);
            loadBackgroundVideoCandidate(candidates, index + 1);
            return;
        }

        cleanupBackgroundPlayer();
        backgroundMedia = media;    
        backgroundPlayer = new MediaPlayer(media);
        backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        backgroundPlayer.setMute(true);
        backgroundPlayer.setAutoPlay(false);
        backgroundVideoView.setMediaPlayer(backgroundPlayer);
        backgroundVideoView.setOpacity(1.0);
        backgroundVideoView.setPreserveRatio(false);

        ColorAdjust videoAdjust = new ColorAdjust();
        videoAdjust.setBrightness(0.03);
        videoAdjust.setContrast(0.12);
        videoAdjust.setSaturation(0.10);
        backgroundVideoView.setEffect(videoAdjust);

        backgroundPlayer.setOnReady(() -> backgroundPlayer.play());
        backgroundPlayer.setOnError(() -> {
            System.err.println("Background player failed: " + backgroundPlayer.getError());
            loadBackgroundVideoCandidate(candidates, index + 1);
        });
        media.setOnError(() -> {
            System.err.println("Media failed to load: " + media.getError());
            loadBackgroundVideoCandidate(candidates, index + 1);
        });
    }

    /**
     * Returns the background video candidates for the current difficulty.
     * Each mode tries its own dedicated video first, then falls back to the
     * other difficulty videos and generic defaults if that file is missing
     * or fails to load.
     */
    private String[] getBackgroundVideoCandidatesForDifficulty() {
        if (difficulty == Difficulty.EASY) {
            return new String[] {
                "/videos/videobackground5.mp4",
                "/videos/videobackground.mp4",
                "/videos/backgroundvideo.mp4"
            };
        } else if (difficulty == Difficulty.MEDIUM) {
            return new String[] {
                "/videos/videobackground6.mp4",
                "/videos/videobackground.mp4",
                "/videos/backgroundvideo.mp4"
            };
        } else if (difficulty == Difficulty.HARD) {
            return new String[] {
                "/videos/videobackground7.mp4",
                "/videos/videobackground.mp4",
                "/videos/backgroundvideo.mp4"
            };
        }
        return new String[] {
            "/videos/videobackground5.mp4",
            "/videos/videobackground7.mp4",
            "/videos/videobackground6.mp4",
            "/videos/videobackground.mp4",
            "/videos/backgroundvideo.mp4"
        };
    }

    private URL resolveResource(String path) {
        URL resource = getClass().getResource(path);
        if (resource == null && path.startsWith("/")) {
            resource = getClass().getResource(path.substring(1));
        }
        return resource;
    }

    private Media createMediaFromResource(URL mediaUrl) {
        try (InputStream resourceStream = mediaUrl.openStream()) {
            Path tempFile = Files.createTempFile("hangman-bg-", ".mp4");
            Files.copy(resourceStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            return new Media(tempFile.toUri().toString());
        } catch (Exception e) {
            System.err.println("Failed to create media from resource: " + e);
            return null;
        }
    }

    private void cleanupBackgroundPlayer() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
            backgroundPlayer.dispose();
            backgroundPlayer = null;
        }
    }

    public void goBackToMenu(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/quentincillierre/hangman/application/menu-view.fxml"));
        Parent root = loader.load();
        SoundManager.attachClickSound(root);
        Scene scene = new Scene(root, 721, 466);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    private void refreshUI() {
    if (model == null) {
        return;
    }

    wordLabel.setText(model.getHiddenWord());
    hintLabel.setText(getHintText());

   if (attemptsLabel != null) {
    int wrongs = model.getCurrentWrongs();
    int max = model.getMaxWrongs();
    int remaining = max - wrongs;
    attemptsLabel.setText("Attempts Left: " + remaining);

    if (remaining <= 2) {
        attemptsLabel.setStyle("-fx-text-fill: #EF476F; -fx-background-color: rgba(255,255,255,0.08); -fx-padding: 6 12; -fx-background-radius: 12; -fx-font-weight: bold;");
    } else {
        attemptsLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-background-color: rgba(255,255,255,0.08); -fx-padding: 6 12; -fx-background-radius: 12; -fx-font-weight: bold;");
    }
}

    // Prefer realistic images if provided by the user in resources/pictures.
    String imageResource = findHangmanImageResource(model.getCurrentWrongs());
    if (imageResource != null) {
        hangmanImageView.setSmooth(true);
        hangmanImageView.setCache(true);
        hangmanImageView.setImage(new Image(getClass().getResource(imageResource).toExternalForm(), true));
    } else {
        var res = getClass().getResource(String.format("/pictures/%s-hangman.png", model.getCurrentWrongs()));
        if (res != null) {
            hangmanImageView.setSmooth(true);
            hangmanImageView.setCache(true);
            hangmanImageView.setImage(new Image(res.toExternalForm(), true));
        } else {
            hangmanImageView.setImage(null);
        }
    }

    if (model.isLose() || model.isWin()) {
        keyboardGrid.setDisable(true);
        wordLabel.setText(model.getWordToGuess());
        resultLabel.setOpacity(1);
        resultLabel.setAlignment(Pos.CENTER);
        resultLabel.setText(model.isWin() ? "Victory !" : "Game Over !");
    }
}

    private void generateKeyboard() {
        keyboardGrid.getChildren().clear();

        String accentColor;
        if (difficulty == Difficulty.MEDIUM) {
            accentColor = "#F6AD55";
        } else if (difficulty == Difficulty.HARD) {
            accentColor = "#EF476F";
        } else {
            accentColor = "#4FD1C5";
        }

        String defaultButtonStyle =
                "-fx-background-color: rgba(255,255,255,0.08);" +
                "-fx-text-fill: #F8FAFC;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: rgba(255,255,255,0.18);" +
                "-fx-border-width: 1px;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 4, 0, 0, 2);";

        String hoverButtonStyle =
                "-fx-background-color: " + accentColor + ";" +
                "-fx-text-fill: #0B1120;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: transparent;" +
                "-fx-background-radius: 10;";

        for (char c = 'A'; c <= 'Z'; c++) {
            Button letterButton = new Button(String.valueOf(c));
            letterButton.setPrefSize(40, 40);
            letterButton.setStyle(defaultButtonStyle);

            // Disable the button after use so the same letter can't be
            // guessed repeatedly (previously it stayed clickable forever).
            letterButton.setOnAction(event -> {
                handleKeyboardInput(letterButton.getText());
                letterButton.setDisable(true);
            });
            letterButton.setOnMouseEntered(event -> {
                if (!letterButton.isDisabled()) {
                    letterButton.setStyle(hoverButtonStyle);
                }
            });
            letterButton.setOnMouseExited(event -> {
                if (!letterButton.isDisabled()) {
                    letterButton.setStyle(defaultButtonStyle);
                }
            });

            int index = c - 'A';
            int col = index % 13;
            int row = index / 13;
            keyboardGrid.add(letterButton, col, row);
        }
    }

    public void handleKeyboardInput(String character) {
        if (model == null || model.isWin() || model.isLose()) {
            return;
        }

        if (character != null && character.length() == 1) {
            char letter = Character.toUpperCase(character.charAt(0));
            if (letter >= 'A' && letter <= 'Z') {
                model.tryLetter(letter);
                refreshUI();
            }
        }
    }

    @FXML
    public void restartGame() {
        if (currentCategory == null || difficulty == null) {
            return;
        }
        start(currentCategory, difficulty);
    }

    private String findHangmanImageResource(int stage) {
        String[] candidates = new String[] {
            String.format("/pictures/pic%d.jpeg", stage),
            String.format("/pictures/realistic-%d.png", stage),
            "/pictures/realistic-hangman.png",
            String.format("/pictures/%d-hangman.png", stage)
        };

        for (String c : candidates) {
            if (getClass().getResource(c) != null) {
                return c;
            }
        }
        return null;
    }

    private String toDisplayDifficulty(Difficulty difficulty) {
        if (difficulty == null) {
            return "UNKNOWN MODE";
        }
        String label = difficulty.name().toLowerCase();
        return label.substring(0, 1).toUpperCase() + label.substring(1) + " mode";
    }

    private String toDisplayCategory(Category category) {
        if (category == null) {
            return "UNKNOWN CATEGORY";
        }
        String label = category.name().toLowerCase();
        return "Category: " + label.substring(0, 1).toUpperCase() + label.substring(1);
    }
}