# GooeyGame library source

This is the source of the library itself. Open THIS folder in VS Code (File > Open Folder) to
read or change it. It needs no jar. Students never need this folder; they use
`gooeygame.jar` from the examples or starter project.

    src/gooeygame/    the library (package gooeygame)

## Rebuilding the jar after you change the library

Open a terminal in this folder (the one that contains `src`), then:

    javac --release 8 -d out src/gooeygame/*.java
    jar cf gooeygame.jar -C out .

Then copy `gooeygame.jar` into the `lib/` folder of each project, replacing the old one, and in
VS Code run **Java: Clean Java Language Server Workspace**.

Why `--release 8`: it makes the jar run on any Java 8 or newer. Without it, the jar only runs on
the Java version you compiled with or newer, which causes the error
"UnsupportedClassVersionError ... class file version 65.0" on older computers.

The library avoids anything newer than Java 8.

## Testing status
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
