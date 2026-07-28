package fr.quentincillierre.hangman.util;

import java.net.URL;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public final class MusicManager {

    private static final String DEFAULT_MUSIC_PATH = "/music/bgmusic1.mp3";
    private static MediaPlayer musicPlayer;
    private static String currentTrackPath;

    private MusicManager() {
    }

    /**
     * Starts looping background music from the given resource path.
     * If music is already playing from the same track, this does nothing
     * (prevents restarting the track every time a screen reloads).
     */
    public static void play(String resourcePath) {
        if (resourcePath == null) {
            resourcePath = DEFAULT_MUSIC_PATH;
        }

        // Already playing this exact track — don't restart it.
        if (musicPlayer != null && resourcePath.equals(currentTrackPath)
                && musicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            return;
        }

        stop();

        URL resource = MusicManager.class.getResource(resourcePath);
        if (resource == null) {
            System.err.println("Background music resource not found: " + resourcePath);
            return;
        }

        Media media = new Media(resource.toExternalForm());
        musicPlayer = new MediaPlayer(media);
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.setVolume(0.35);
        currentTrackPath = resourcePath;

        musicPlayer.setOnReady(() -> musicPlayer.play());
        musicPlayer.setOnError(() ->
            System.err.println("Music playback error: " + musicPlayer.getError())
        );
    }

    public static void play() {
        play(DEFAULT_MUSIC_PATH);
    }

    public static void stop() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
            currentTrackPath = null;
        }
    }

    public static void setVolume(double volume) {
        if (musicPlayer != null) {
            musicPlayer.setVolume(volume);
        }
    }

    public static void fadeOut(double seconds) {
        if (musicPlayer == null) {
            return;
        }
        double startVolume = musicPlayer.getVolume();
        Duration duration = Duration.seconds(seconds);
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.ZERO,
                new javafx.animation.KeyValue(musicPlayer.volumeProperty(), startVolume)),
            new javafx.animation.KeyFrame(duration,
                event -> stop(),
                new javafx.animation.KeyValue(musicPlayer.volumeProperty(), 0))
        );
        timeline.play();
    }
}
