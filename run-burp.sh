#!/bin/bash
# Launch Burp Suite Pro with the M1kro loader agent.
# Must run on a JDK that still ships jdk.internal.org.objectweb.asm (JDK <= 21).
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"

# Pick a JDK that still ships jdk.internal.org.objectweb.asm (JDK <= 21).
# Honor an explicit $JDK; otherwise try common Linux/macOS locations.
if [ -z "${JDK:-}" ]; then
  for candidate in \
    /usr/lib/jvm/java-21-openjdk \
    /usr/lib/jvm/java-21-openjdk-amd64 \
    /usr/lib/jvm/jdk-21 \
    /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
    /usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
    /Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home; do
    if [ -x "$candidate/bin/java" ]; then JDK="$candidate"; break; fi
  done
fi
if [ -z "${JDK:-}" ] && [ -x /usr/libexec/java_home ]; then
  JDK="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
fi
if [ -z "${JDK:-}" ]; then
  if command -v java >/dev/null 2>&1; then
    JDK="$(dirname "$(dirname "$(command -v java)")")"
    echo "[!] JDK 21 not found in known paths; using 'java' on PATH ($JDK)." >&2
    echo "    If you hit an asm error, install JDK 21 and re-run with JDK=/path/to/jdk-21." >&2
  else
    echo "No JDK found. Install JDK 21, or set JDK=/path/to/jdk-21." >&2
    exit 1
  fi
fi
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
