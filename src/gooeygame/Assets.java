package gooeygame;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Finds files for images and sounds. A file next to your project is tried
 * first; if there isn't one, the project's classpath is searched, which is
 * how assets are found when a game is packaged inside a JAR.
 */
final class Assets {

    private static final Map<String, BufferedImage> imageCache = new HashMap<String, BufferedImage>();

    private Assets() { }

    /** The folder students put their images and sounds in, next to src and lib. */
    static final String FOLDER = "assets";

    /**
     * Opens an asset by name. Looks, in order:
     *   1. in the assets folder:               "ship.png"  ->  assets/ship.png
     *   2. at the path exactly as given         (found outside assets: still works, with a reminder)
     *   3. on the classpath, inside a jar:      /assets/ship.png, then /ship.png
     * Throws a FileNotFoundException with a helpful message if none of them exist.
     */
    static InputStream open(String path) throws IOException {
        String clean = path.replace('\\', '/');

        File inAssets = new File(FOLDER, clean);
        if (inAssets.isFile()) {
            return new FileInputStream(inAssets);
        }

        File asGiven = new File(path);
        if (asGiven.isFile()) {
            if (!asGiven.isAbsolute() && !clean.startsWith(FOLDER + "/")) {
                Warnings.once("loose:" + clean, "'" + path + "' was found outside the " + FOLDER
                    + " folder. It works, but please move it into the " + FOLDER
                    + " folder (then you can just write \"" + clean + "\") to keep your project tidy.");
            }
            return new FileInputStream(asGiven);
        }

        InputStream stream = Assets.class.getResourceAsStream("/" + FOLDER + "/" + clean);
        if (stream == null) {
            stream = Assets.class.getResourceAsStream("/" + clean);
        }
        if (stream != null) {
            return stream;
        }
        throw new FileNotFoundException("Couldn't find '" + path + "'. Put it in your project's " + FOLDER
            + " folder. Java looked for it here: " + inAssets.getAbsolutePath());
    }

    /** Loads (and remembers) an image. Returns null, after one warning, if it can't be loaded. */
    static BufferedImage getImage(String path) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        BufferedImage img = null;
        try {
            InputStream in = open(path);
            try {
                img = ImageIO.read(in);
            } finally {
                in.close();
            }
            if (img == null) {
                Warnings.once("image:" + path, "Found '" + path
                    + "' but couldn't read it as an image. Try a .png, .jpg, or .gif file.");
            }
        } catch (IOException e) {
            Warnings.once("image:" + path, e.getMessage());
        }
        imageCache.put(path, img);
        return img;
    }
}
