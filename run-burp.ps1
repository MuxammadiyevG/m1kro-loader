# Launch Burp Suite Pro with the M1kro loader agent on Windows (PowerShell).
# Requires JDK 21 (it still ships jdk.internal.org.objectweb.asm). Either have
# JDK 21 'java' on PATH, or set $env:JDK to a JDK 21 home before running.
$ErrorActionPreference = "Stop"

$here  = Split-Path -Parent $MyInvocation.MyCommand.Path
$agent = Join-Path $here "loader.jar"

# Burp jar: first argument, or the newest burpsuite_*.jar next to this script.
$burp = if ($args.Count -gt 0) { $args[0] } else {
  Get-ChildItem -Path $here -Filter "burpsuite_*.jar" |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1 -ExpandProperty FullName
}
if (-not $burp -or -not (Test-Path $burp)) {
  Write-Error "Burp jar not found. Pass it: .\run-burp.ps1 C:\path\to\burpsuite_pro.jar"
  exit 1
}

$java = if ($env:JDK) { Join-Path $env:JDK "bin\java.exe" } else { "java" }

Write-Host "[*] JDK   : $java"
Write-Host "[*] Agent : $agent"
Write-Host "[*] Burp  : $burp"

& $java `
  --add-opens=java.desktop/javax.swing=ALL-UNNAMED `
  --add-opens=java.base/java.lang=ALL-UNNAMED `
  --add-opens=java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED `
  --add-opens=java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED `
  -javaagent:$agent `
  -noverify `
  -jar $burp
