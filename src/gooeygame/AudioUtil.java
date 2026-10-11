package gooeygame;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.UnsupportedAudioFileException;

/** Volume and pan helpers shared by Sound (a Clip) and Music (a SourceDataLine). */
final class AudioUtil {

    private AudioUtil() { }

    /**
     * Opens an audio stream and makes sure it is plain PCM that a speaker line can play.
     * Java itself reads .wav, .aiff and .au. Compressed formats (u-law/a-law .wav files,
     * and .mp3 or .ogg when a decoder jar is in the project's lib folder) arrive in their
     * own encoding and are converted to 16-bit PCM here.
     */
    static AudioInputStream open(InputStream raw) throws UnsupportedAudioFileException, IOException {
        AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
        AudioFormat format = in.getFormat();
        AudioFormat.Encoding encoding = format.getEncoding();
        if (encoding.equals(AudioFormat.Encoding.PCM_SIGNED) || encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
            return in;
        }
        AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), 16,
                                          format.getChannels(), format.getChannels() * 2,
                                          format.getSampleRate(), false);
        return AudioSystem.getAudioInputStream(pcm, in);
    }

    /** Which jar a missing decoder class lives in (for a helpful message), or null if unknown. */
    static String jarFor(String className) {
        if (className.startsWith("javazoom.jl.")) return "jlayer-1.0.1.4.jar";
        if (className.startsWith("javazoom.spi.vorbis")) return "vorbisspi-1.0.3.3.jar";
        if (className.startsWith("javazoom.spi.")) return "mp3spi-1.9.5.4.jar";
        if (className.startsWith("org.tritonus.")) return "tritonus-share-0.3.7.4.jar";
        if (className.startsWith("com.jcraft.")) return "jorbis-0.0.17.4.jar";
        return null;
    }

    /** Plain-language help for a NoClassDefFoundError / missing class. */
    static String missingClassHelp(Throwable e) {
        String cls = String.valueOf(e.getMessage()).replace('/', '.');
        String jar = jarFor(cls);
        return "Java couldn't find the class " + cls + "."
            + (jar != null
                ? " It comes from " + jar + ", so make sure that jar is in your project's lib folder."
                : " If it comes from a jar, make sure that jar is in your project's lib folder.");
    }

    /**
     * The warning to print when loading or playing audio fails.
     * action is like "load the sound"; path is the audio file; e is what went wrong.
     */
    static String failure(String action, String path, Throwable e) {
        if (e instanceof LinkageError) {
            return "Couldn't " + action + " '" + path + "'. " + missingClassHelp(e)
                + " (MP3 needs tritonus-share, jlayer, AND mp3spi in lib; OGG needs tritonus-share, jorbis, AND vorbisspi.)";
        }
        return "Couldn't " + action + " '" + path + "'. Check that it is a .wav file in the right folder,"
            + " and that this computer has audio output." + formatHint(path) + " (" + e.getMessage() + ")";
    }

    /** Extra advice for the warning message when someone tries a format Java can't read by itself. */
    static String formatHint(String path) {
        String lower = String.valueOf(path).toLowerCase();
        if (lower.endsWith(".mp3") || lower.endsWith(".ogg")) {
            return " MP3 and OGG files need extra decoder jars in your project's lib folder"
                 + " (see the README), or convert the file to .wav.";
        }
        return "";
    }

    /** volume: 0.0 is silent, 1.0 is the file's normal loudness. Does nothing if the device can't change volume. */
    static void setVolume(Line line, double volume) {
        if (!line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (volume <= 0.0) ? gain.getMinimum() : (float) (20.0 * Math.log10(volume));
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
    }

    /** pan: -1.0 is all left, 0.0 is centered, 1.0 is all right. */
    static void setPan(Line line, double pan) {
        if (!line.isControlSupported(FloatControl.Type.PAN)) {
            return;
        }
        FloatControl control = (FloatControl) line.getControl(FloatControl.Type.PAN);
        control.setValue((float) Math.max(-1.0, Math.min(1.0, pan)));
    }
}
