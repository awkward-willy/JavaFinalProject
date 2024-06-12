package finalProject.Game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class MusicPlayer {
    private static MediaPlayer mediaPlayer;
    private static boolean isPlaying = false;

    public static void playBackgroundMusic() {
        try {
            if (mediaPlayer == null) {
                File backgroundMusic = new File("resources/BackgroundMusic.mp3");
                Media media = new Media(backgroundMusic.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            }
            mediaPlayer.setVolume(0.15);
            mediaPlayer.play();
            isPlaying = true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void playBombMusic() {
        try {
            File bombMusic = new File("resources/BombMusic.mp3");
            Media media = new Media(bombMusic.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(1.0);
            mediaPlayer.play();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stopBackgroundMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }

    public static void toggleBackgroundMusic() {
        if (isPlaying) {
            stopBackgroundMusic();
        } else {
            playBackgroundMusic();
        }
    }
}
