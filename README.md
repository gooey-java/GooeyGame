# GooeyGame

Make real 2D games in Java with a simple game loop, built on top of Swing. Every
game is one class with three methods: `init()`, `update(dt)` and `draw(d)`.
Part of the [Gooey Java](https://gooey-java.github.io/) libraries for AP Computer
Science A.

**Website:** https://gooey-java.github.io/GooeyGame/

**Docs:** https://gooey-java.github.io/GooeyGame/docs/

**Download:** [GooeyGame.jar](https://gooey-java.github.io/GooeyGame/GooeyGame.jar) and [GooeyGame-sources.jar](https://gooey-java.github.io/GooeyGame/GooeyGame-sources.jar)

## Quick start

1. Put both jars in your project's `lib` folder. VS Code shows the docs when you
   hover over GooeyGame code.
2. Put images and sounds in an `assets` folder in your project (next to `src`
   and `lib`), and load them by name: `"ship.png"` reads `assets/ship.png`.
3. Import, write your game, and hit Run:

```java
import gooeygame.*;

public class Hello implements GameListener {
    private double x = 0;

    public void init() { }

    public void update(double dt) {
        x += 200 * dt;    // 200 pixels per second
    }

    public void draw(Draw d) {
        d.clear("black");
        d.fillCircle(x, 300, 20, "yellow");
    }

    public static void main(String[] args) {
        GameRunner.start(new Hello(), "Hello", 800, 600);
    }
}
```

`init()` runs once at the start, `update(dt)` runs about 60 times per second to
move things and check the rules (`dt` is the seconds since the last update), and
`draw(d)` draws the current state. (0, 0) is the top-left corner and y increases
downward. Colors can be names like `"red"` or hex codes like `"#FF8800"`.

`.wav` sounds work out of the box. MP3 and OGG need extra decoder jars in your
`lib` folder.

## What's in this repository

| Path | What it is |
|---|---|
| `src/gooeygame/` | The library's source code |
| `GooeyGame.jar` | The compiled library |
| `GooeyGame-sources.jar` | The source code as a jar, for hover docs in VS Code |
| `site/` | The showcase page (the website's home page) |
| `build.sh`, `build.bat` | Build the jars, the Javadoc, and the website |
| `javadoc-options.txt` | Settings for the docs (title, footer, Java version) |
| `.github/workflows/pages.yml` | Publishes the website after every push |
| `LICENSE` | The MIT License |

## Building and publishing

You need a JDK 17 or newer. Run `./build.sh` (Mac/Linux) or `build.bat`
(Windows). That rebuilds `GooeyGame.jar` and `GooeyGame-sources.jar` in this folder
(compiled to run on Java 8 or newer) and builds the whole website in
`_site/`: the showcase page, the Javadoc in `_site/docs/`, and both jars. Open
`_site/index.html` to preview it. (`_site/` isn't committed; GitHub builds its own.)

Publishing is automatic: after every push to `main`, GitHub runs `build.sh` and
publishes `_site/` to https://gooey-java.github.io/GooeyGame/. The **Actions** tab
on GitHub shows each run. This needs a one-time setting: **Settings > Pages >
Source: GitHub Actions**.

The showcase page is `site/index.html`. Its styles come from
`https://gooey-java.github.io/gooey.css` in the gooey-java.github.io repo, which
every Gooey Java page shares.

### Why Java 8

The jar is compiled with `--release 8` so it runs on any Java 8 or newer.
Without it, the jar only runs on the Java version it was compiled with or newer,
which causes the error "UnsupportedClassVersionError ... class file version 65.0"
on older computers. The library avoids anything newer than Java 8. The build
scripts take care of this; if you compile by hand, include `--release 8`.

### Testing status
Verified off-screen: all drawing and color behavior, classpath asset loading, input
state, collisions, graceful audio failure, and complete frame-by-frame simulations of both
example games (screen flow, movement, game over, restart). The streaming `Music` class was
tested against a simulated speaker: finishing, looping, pause/resume, volume, 40 rapid
stop/play restarts (never two speaker lines open at once, no leaked threads), bad files,
and .aiff, .au, and u-law .wav files (converted to PCM when needed). Sprite sheets (margin/spacing grids, frame numbering, flip, rotate, scale, error messages), image
flip/rotate/opacity/crisp scaling, and the camera (including on a 2x-scaled high-DPI surface) are tested
pixel by pixel. The dungeon example was simulated for 20,000 ticks of random movement (never inside a wall,
camera always in range) plus coin, exit, win, and restart rules, and its frames were rendered and inspected.
Asset lookup (assets folder first, subfolders, loose-file reminder, classpath fallback, missing-file
message) is tested. A simulated decoder jar
with a missing class was used to confirm that decoder problems produce a friendly message
naming the missing jar instead of an uncaught error.
The jar contains Java 8 bytecode (class file version 52).
NOT yet verified: the real window and game loop, real audio playback (Sound and Music), real
MP3/OGG decoding with the extra jars, and the VS Code project setup (the test machine had no display, sound device, or VS Code).

## License

GooeyGame is released under the [MIT License](LICENSE): anyone may use, copy,
change, and share it, as long as the copyright notice and license are kept with it.
