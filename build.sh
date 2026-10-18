#!/bin/sh
# Builds GooeyGame (Mac / Linux). Run from anywhere:  ./build.sh
#
#   GooeyGame.jar           the compiled library, for students' lib folders
#   GooeyGame-sources.jar   the source code, so VS Code shows the docs on hover
#   _site/               the website, exactly as GitHub Pages publishes it:
#                          the showcase page (from site/), the Javadoc (in docs/),
#                          and copies of both jars for the download links
#
# GitHub runs this same script after every push (see .github/workflows/pages.yml),
# so the published website always matches the code. Run it yourself to rebuild
# the jars in this folder or to preview the website by opening _site/index.html.
set -e
cd "$(dirname "$0")"

NAME=GooeyGame
PKG=gooeygame
RELEASE=8    # the oldest Java version the jar runs on

rm -rf build _site
mkdir -p _site

if ls src/$PKG/*.java >/dev/null 2>&1; then
    rm -f $NAME.jar $NAME-sources.jar
    mkdir -p build
    javac --release $RELEASE -Xlint:-options -d build src/$PKG/*.java
    jar --create --file $NAME.jar -C build . LICENSE
    jar --create --file $NAME-sources.jar -C src . LICENSE
    javadoc @javadoc-options.txt
    cp $NAME.jar $NAME-sources.jar _site/
    rm -rf build
    echo "Built $NAME.jar and $NAME-sources.jar"
else
    echo "No code in src/$PKG yet, so only the showcase page was built."
fi

cp -R site/. _site/
touch _site/.nojekyll
echo "Website is ready in _site/ (open _site/index.html to preview it)"
