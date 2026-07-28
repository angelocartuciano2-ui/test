package fr.quentincillierre.hangman.controller;

import java.net.URL;
import java.util.ArrayList;

import fr.quentincillierre.hangman.Category;
import fr.quentincillierre.hangman.Difficulty;
import fr.quentincillierre.hangman.util.MusicManager;
import fr.quentincillierre.hangman.util.SoundManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

public class MenuController {

    @FXML
    private MediaView backgroundImg;

    @FXML
    private MenuButton catogoriesList;

    @FXML
    private RadioButton easyRadio;

    @FXML
    private RadioButton mediumRadio;

    @FXML
    private RadioButton hardRadio;

    private Category selectedCategory = Category.FOOD;
    private Difficulty selectedDifficulty = Difficulty.EASY;

    @FXML
    public void initialize() {
        loadBackgroundVideo();
        MusicManager.play("/music/bgmusic1.mp3");

        ToggleGroup difficultyGroup = new ToggleGroup();
        easyRadio.setToggleGroup(difficultyGroup);
        mediumRadio.setToggleGroup(difficultyGroup);
        hardRadio.setToggleGroup(difficultyGroup);
        easyRadio.setSelected(true);

        difficultyGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == easyRadio) {
                selectedDifficulty = Difficulty.EASY;
            } else if (newToggle == mediumRadio) {
                selectedDifficulty = Difficulty.MEDIUM;
            } else if (newToggle == hardRadio) {
                selectedDifficulty = Difficulty.HARD;
            }
        });

        ArrayList<MenuItem> categories = new ArrayList<>();
        for (Category category : Category.values()) {
            MenuItem categoryItem = new MenuItem(category.toString());
            categoryItem.setStyle("-fx-pref-width: 178;");
            categoryItem.setOnAction(event -> {
                SoundManager.playClick();
                selectedCategory = category;
                catogoriesList.setText(category.toString());
            });
            categories.add(categoryItem);
        }

        catogoriesList.getItems().addAll(categories);
        catogoriesList.setText(selectedCategory.toString());
    }

    private void loadBackgroundVideo() {
        if (backgroundImg == null) {
            return;
        }

        try {
            String[] candidates = {
                "/videos/vidoebackground4.mp4",
                "/videos/videobackground4.mp4",
                "/videos/videobackground.mp4",
                "/videos/backgroundvideo.mp4"
            };

            URL videoUrl = null;
            for (String candidate : candidates) {
                videoUrl = getClass().getResource(candidate);
                if (videoUrl != null) {
                    break;
                }
            }

            if (videoUrl != null) {
                Media media = new Media(videoUrl.toExternalForm());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setMute(true);
                backgroundImg.setMediaPlayer(mediaPlayer);
                backgroundImg.setFitHeight(475);
                backgroundImg.setFitWidth(848);
                backgroundImg.setPreserveRatio(false);
                mediaPlayer.play();
            } else {
                System.err.println("No menu background video resource was found.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load menu background video: " + e.getMessage());
        }
    }

    @FXML
public void launchGame(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/quentincillierre/hangman/application/game-view.fxml"));
        Parent root = loader.load();
        SoundManager.attachClickSound(root);
        Scene scene = new Scene(root, 721, 466);

        GameController gameController = loader.getController();
        gameController.start(selectedCategory, selectedDifficulty);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    } catch (Exception e) {
        System.err.println("Play button failed: " + e.getMessage());
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to start the game. Please check the console for details.", ButtonType.OK);
        alert.setHeaderText("Game launch error");
        alert.showAndWait();
    }
}

@FXML
public void exitApplication(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.close();
}
}