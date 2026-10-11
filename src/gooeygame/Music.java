package gooeygame;

import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Background music, STREAMED from disk a little at a time instead of being
 * loaded into memory. Use Music for long tracks (a whole song is fine) and
 * Sound for short effects that play over and over.
 *
 *   music = Music.load("theme.wav");
 *   music.setLooping(true);
 *   music.setVolume(0.4);
 *   music.play();
 *
 * Like most game libraries, play() on music that is already playing does
 * nothing, and play() on paused music resumes it. stop() rewinds, so the next
 * play() starts from the beginning. Use .wav files. (.aiff and .au also work;
 * .mp3 and .ogg need extra decoder jars, see the README.) A missing file or
 * missing audio device prints one warning, and the game runs on.
 *
 * Streaming runs on a background thread, but all of that is handled here:
 * call these methods from update() like any other.
 */
public class Music {

    private final String path;
    private final boolean available;
    private volatile boolean looping = false;
    private volatile double volume = 1.0;
    private Player player;          // the current playback; guarded by "this"

    Music(String path) {
        this.path = path;
        this.available = probe(path);
    }

    /** Opens a file from the assets folder for streaming: Music.load("theme.wav") reads assets/theme.wav. Call this once, not every frame. */
    public static Music load(String path) {
        return new Music(path);
    }

    /** Should the music repeat forever? Takes effect at the end of the current pass, even if already playing. */
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public boolean isLooping() {
        return looping;
    }

    /** Sets the volume: 0.0 (silent) to 1.0 (normal). */
    public void setVolume(double volume) {
        this.volume = volume;
        Player current;
        synchronized (this) {
            current = player;
        }
        if (current != null) {
            SourceDataLine line = current.line;
            if (line != null) {
                AudioUtil.setVolume(line, volume);
            }
        }
    }

    public double getVolume() {
        return volume;
    }

    /** Starts the music, or resumes it if paused. Does nothing if it is already playing. */
    public synchronized void play() {
        if (!available) {
            return;
        }
        if (player != null && !player.finished) {
            if (player.paused) {
                player.resume();
            }
            return;
        }
        player = new Player();
        player.start();
    }

    /** Stops the music and rewinds it to the beginning. */
    public synchronized void stop() {
        if (player != null) {
            player.requestStop();
            player = null;
        }
    }

    /** Pauses the music where it is. Call play() or resume() to continue. */
    public synchronized void pause() {
        if (player != null && !player.finished) {
            player.pause();
        }
    }

    /** Continues music that was paused. Does nothing if the music isn't paused. */
    public synchronized void resume() {
        if (player != null && !player.finished && player.paused) {
            player.resume();
        }
    }

    /** Is the music playing right now (not paused, stopped, or finished)? */
    public synchronized boolean isPlaying() {
        return player != null && !player.finished && !player.paused;
    }

    // ------------------------------------------------------------------
    // Everything below is machinery students don't need to read.
    // ------------------------------------------------------------------

    /** Opens an output line for this audio format. (Tests replace this with a fake.) */
    SourceDataLine openLine(AudioFormat format) throws LineUnavailableException {
        SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        return line;
    }

    /** Checks at load time that the file exists and is a readable .wav, without keeping it open. */
    private static boolean probe(String path) {
        try {
            InputStream raw = Assets.open(path);
            try {
                AudioUtil.open(raw).close();
            } finally {
                raw.close();
            }
            return true;
        } catch (Exception | LinkageError e) {
            Warnings.once("music:" + path, AudioUtil.failure("load the music", path, e));
            return false;
        }
    }

    /**
     * One playback of the music, on its own thread with its own output line.
     * Each play gets a fresh Player, so an old one can never touch a new one's
     * line, which is what makes restarting safe.
     */
    private final class Player implements Runnable {
        private final Thread thread = new Thread(this, "GooeyGame-Music");
        private volatile boolean stopRequested = false;
        private volatile boolean paused = false;
        private volatile boolean finished = false;
        private volatile SourceDataLine line;

        Player() {
            thread.setDaemon(true);   // never keeps the program alive after the window closes
        }

        void start() {
            thread.start();
        }

        void pause() {
            paused = true;
            SourceDataLine l = line;
            if (l != null) {
                l.stop();
            }
        }

        void resume() {
            paused = false;
            SourceDataLine l = line;
            if (l != null) {
                l.start();
            }
        }

        /** Asks the thread to end, unblocks it, and waits (briefly) for it to finish. */
        void requestStop() {
            stopRequested = true;
            paused = false;
            SourceDataLine l = line;
            if (l != null) {
                l.stop();
                l.flush();
                l.close();       // closing is what wakes a thread blocked inside write()
            }
            try {
                thread.join(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void run() {
            SourceDataLine l = null;
            try {
                byte[] buffer = new byte[4096];
                do {
                    AudioInputStream in = AudioUtil.open(Assets.open(path));
                    try {
                        if (l == null) {                       // open the speaker once, reuse it across loops
                            l = openLine(in.getFormat());
                            line = l;
                            AudioUtil.setVolume(l, volume);
                            if (!paused) {
                                l.start();
                            }
                        }
                        int n;
                        while (!stopRequested && (n = in.read(buffer, 0, buffer.length)) != -1) {
                            while (paused && !stopRequested) {
                                Thread.sleep(20);
                            }
                            if (stopRequested) {
                                break;
                            }
                            l.write(buffer, 0, n);             // waits here when the speaker's buffer is full
                        }
                    } finally {
                        in.close();
                    }
                } while (!stopRequested && looping);

                if (!stopRequested) {
                    l.drain();                                 // let the last bit finish naturally
                }
            } catch (Exception | LinkageError e) {
                if (!stopRequested) {
                    Warnings.once("music-play:" + path, AudioUtil.failure("play the music", path, e));
                }
            } finally {
                if (l != null) {
                    l.stop();
                    l.close();
                }
                finished = true;
            }
        }
    }
}
