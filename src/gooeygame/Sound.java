package gooeygame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * A short sound effect, kept entirely IN MEMORY so it can be played instantly
 * and often. (For long background tracks, use Music, which streams from disk.)
 * Load it ONCE (in init), then play it as often as you like:
 *
 *   private Sound coin;
 *   public void init()   { coin = Sound.load("coin.wav"); }
 *   public void update(double dt) { ... coin.play(); ... }
 *
 * Each Sound keeps four copies ready, so rapid repeats overlap instead of
 * cutting each other off. That means a Sound uses about four times the memory
 * of its uncompressed audio (for a .wav file, roughly four times the file
 * size), so keep sounds short (a few seconds). Use .wav files. (.aiff and .au
 * also work; .mp3 and .ogg need extra decoder jars, see the README.)
 * If a file is missing or the computer has no audio, you get one friendly
 * warning and the game keeps running silently.
 */
public class Sound {

    private static final int VOICES = 4;

    private final Clip[] voices = new Clip[VOICES];
    private int next = 0;
    private boolean ready = false;

    private Sound(String path) {
        try {
            byte[] bytes = readAll(path);
            for (int i = 0; i < VOICES; i++) {
                AudioInputStream ais = AudioUtil.open(new ByteArrayInputStream(bytes));
                voices[i] = AudioSystem.getClip();
                voices[i].open(ais);
            }
            ready = true;
        } catch (Exception | LinkageError e) {
            closeAll();
            Warnings.once("sound:" + path, AudioUtil.failure("load the sound", path, e));
        }
    }

    /** Loads a sound from the assets folder: Sound.load("coin.wav") reads assets/coin.wav. Call this once, not every frame. */
    public static Sound load(String path) {
        return new Sound(path);
    }

    /** Plays the sound at full volume. */
    public void play() {
        play(1.0, 0.0);
    }

    /** Plays the sound. volume: 0.0 (silent) to 1.0 (normal). */
    public void play(double volume) {
        play(volume, 0.0);
    }

    /** Plays the sound. pan: -1.0 (left speaker) to 1.0 (right speaker). */
    public void play(double volume, double pan) {
        if (!ready) {
            return;
        }
        Clip clip = voices[next];
        next = (next + 1) % VOICES;
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        AudioUtil.setVolume(clip, volume);
        AudioUtil.setPan(clip, pan);
        clip.start();
    }

    /** Stops every copy of this sound that is playing. */
    public void stop() {
        for (Clip clip : voices) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
    }

    private void closeAll() {
        ready = false;
        for (int i = 0; i < VOICES; i++) {
            if (voices[i] != null) {
                voices[i].close();
                voices[i] = null;
            }
        }
    }

    private static byte[] readAll(String path) throws IOException {
        InputStream in = Assets.open(path);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } finally {
            in.close();
        }
    }
}
