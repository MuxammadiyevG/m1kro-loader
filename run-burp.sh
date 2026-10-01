#!/bin/bash
# Launch Burp Suite Pro with the M1kro loader agent.
# Must run on a JDK that still ships jdk.internal.org.objectweb.asm (JDK <= 21).
set -euo pipefail

JDK="${JDK:-/usr/lib/jvm/java-21-openjdk}"
HERE="$(cd "$(dirname "$0")" && pwd)"
AGENT="$HERE/loader.jar"

# Burp jar: first arg, or $BURP_JAR, or newest burpsuite_*.jar next to this script / in CWD.
BURP_JAR="${1:-${BURP_JAR:-}}"
if [ -z "$BURP_JAR" ]; then
  BURP_JAR="$(ls -t "$HERE"/burpsuite_*.jar ./burpsuite_*.jar 2>/dev/null | head -1 || true)"
fi
if [ -z "$BURP_JAR" ] || [ ! -f "$BURP_JAR" ]; then
  echo "Burp jar not found. Pass it explicitly:  $0 /path/to/burpsuite_pro_vXXXX.jar" >&2
  exit 1
fi

echo "[*] JDK   : $JDK"
echo "[*] Agent : $AGENT"
echo "[*] Burp  : $BURP_JAR"

exec "$JDK/bin/java" \
  --add-opens=java.desktop/javax.swing=ALL-UNNAMED \
  --add-opens=java.base/java.lang=ALL-UNNAMED \
  --add-opens=java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED \
  --add-opens=java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED \
  -javaagent:"$AGENT" \
  -noverify \
  -jar "$BURP_JAR"
