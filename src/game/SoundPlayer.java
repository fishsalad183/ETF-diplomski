package game;

import java.net.URL;
import java.util.HashMap;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundPlayer {

    //================================================================================
    // Sound effects
    //================================================================================
    public enum SoundEffect {
        COIN, HEART, CLOCK, DAMAGE, VICTORY, DEFEAT;
    }

    private final HashMap<SoundEffect, AudioClip> soundEffects = new HashMap<>(6);

    {
        soundEffects.put(SoundEffect.COIN, loadAudioClip("resources/coin2 trimmed3.wav"));
        soundEffects.put(SoundEffect.HEART, loadAudioClip("resources/heal1.wav"));
        soundEffects.put(SoundEffect.CLOCK, loadAudioClip("resources/clock1.wav"));
        soundEffects.put(SoundEffect.DAMAGE, loadAudioClip("resources/damage.wav"));
        soundEffects.put(SoundEffect.VICTORY, loadAudioClip("resources/victory.wav"));
        soundEffects.put(SoundEffect.DEFEAT, loadAudioClip("resources/defeat.wav"));
    }

    private double soundEffectVolume = 1;

    public void setSoundEffectVolume(double vol) {
        soundEffectVolume = vol;
        soundEffects.forEach((soundEffect, audioClip) -> audioClip.setVolume(vol));
    }

    public double getSoundEffectVolume() {
        return soundEffectVolume;
    }

    public void playSoundEffect(SoundEffect e) {
        if (soundEffectVolume > 0) {
            soundEffects.get(e).play();
        }
    }

    //================================================================================
    // Music
    //================================================================================
    private final MediaPlayer MUSIC_PLAYER = new MediaPlayer(loadMedia("resources/funky music.mp3"));

    {
        MUSIC_PLAYER.setCycleCount(MediaPlayer.INDEFINITE);
    }

    public void playMusic() {
        if (getMusicVolume() > 0) {
            MUSIC_PLAYER.play();
        }
    }

    public void stopMusic() {
        MUSIC_PLAYER.stop();
    }

    public void setMusicVolume(double vol) {
        MUSIC_PLAYER.setVolume(vol);
    }

    public double getMusicVolume() {
        return MUSIC_PLAYER.getVolume();
    }

    //================================================================================
    // General
    //================================================================================
    private static SoundPlayer instance = null;

    public static SoundPlayer getInstance() {
        if (instance == null) {
            instance = new SoundPlayer();
        }
        return instance;
    }

    private SoundPlayer() {
    }

    //================================================================================
    // Utility
    //================================================================================
    private AudioClip loadAudioClip(String resource) {
        URL soundURL = this.getClass().getClassLoader().getResource(resource);
        return new AudioClip(soundURL.toExternalForm());
    }

    private Media loadMedia(String resource) {
        URL soundURL = this.getClass().getClassLoader().getResource(resource);
        return new Media(soundURL.toExternalForm());
    }

    public static String getVolumeString(double vol) {
        if (vol == 0) {
            return "off";
        } else {
            return ((int) Math.ceil(vol * 100)) + "%";
        }
    }

}
