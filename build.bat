@echo off
rem Builds GooeyGame (Windows). Double-click it or run:  build.bat
rem
rem   GooeyGame.jar           the compiled library, for students' lib folders
rem   GooeyGame-sources.jar   the source code, so VS Code shows the docs on hover
rem   _site\               the website, exactly as GitHub Pages publishes it:
rem                          the showcase page (from site\), the Javadoc (in docs\),
rem                          and copies of both jars for the download links
rem
rem GitHub runs build.sh (the Mac/Linux version of this file) after every push,
rem so the published website always matches the code. Run this yourself to
rem rebuild the jars in this folder or to preview the website.
setlocal
cd /d "%~dp0"

set NAME=GooeyGame
set PKG=gooeygame
rem The oldest Java version the jar runs on:
set RELEASE=8

if exist build rmdir /s /q build
if exist _site rmdir /s /q _site
mkdir _site

if not exist src\%PKG%\*.java goto :nocode
if exist %NAME%.jar del %NAME%.jar
if exist %NAME%-sources.jar del %NAME%-sources.jar
mkdir build
javac --release %RELEASE% -Xlint:-options -d build src\%PKG%\*.java || goto :error
jar --create --file %NAME%.jar -C build . LICENSE || goto :error
jar --create --file %NAME%-sources.jar -C src . LICENSE || goto :error
javadoc @javadoc-options.txt || goto :error
copy %NAME%.jar _site\ >nul
copy %NAME%-sources.jar _site\ >nul
rmdir /s /q build
echo Built %NAME%.jar and %NAME%-sources.jar
goto :site

:nocode
echo No code in src\%PKG% yet, so only the showcase page was built.

:site
xcopy site _site /e /i /q /y >nul
type nul > _site\.nojekyll
echo Website is ready in _site\ (open _site\index.html to preview it)
exit /b 0

:error
echo Build failed.
exit /b 1
