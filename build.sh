#!/bin/bash
# Build the M1kro loader.jar from source using JDK 21 (which still ships the
# internal jdk.internal.org.objectweb.asm packages the agent relies on).
set -euo pipefail

JDK="${JDK:-/usr/lib/jvm/java-21-openjdk}"
HERE="$(cd "$(dirname "$0")" && pwd)"
SRC="$HERE/src"
OUT="$HERE/build"
JAR="$HERE/loader.jar"

ASM_EXPORTS=(
  --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED
  --add-exports java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED
)

echo "[*] Using JDK: $JDK"
"$JDK/bin/javac" -version

rm -rf "$OUT"
mkdir -p "$OUT"

echo "[*] Compiling..."
"$JDK/bin/javac" "${ASM_EXPORTS[@]}" -d "$OUT" \
  $(find "$SRC" -name '*.java')

echo "[*] Packaging loader.jar..."
"$JDK/bin/jar" --create --file "$JAR" --manifest "$HERE/MANIFEST.MF" -C "$OUT" .

echo "[*] Done: $JAR"
"$JDK/bin/jar" --describe-module --file "$JAR" 2>/dev/null || true
unzip -l "$JAR"
