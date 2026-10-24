# Pushes a Home Assistant long-lived token to HA Companion on the watch.
# Create the token in HA: Perfil -> Seguridad -> Tokens de acceso de larga duración -> Crear ("HA Companion").
# The token never appears on a command line: it goes through a temp file and `run-as` into the app's private files.
param([Parameter(Mandatory)][string]$Watch, [string]$Adb = "adb")  # -Watch <watch-ip>:5555

$sec = Read-Host "Pega el token de HA" -AsSecureString
$tok = [Runtime.InteropServices.Marshal]::PtrToStringBSTR([Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec)).Trim()
if ($tok.Length -lt 100) { Write-Error "Eso no parece un token de larga duración de HA."; exit 1 }

$tmp = New-TemporaryFile
try {
    [IO.File]::WriteAllText($tmp, $tok)
    & $Adb -s $Watch push $tmp /data/local/tmp/hacompanion_token | Out-Null
    & $Adb -s $Watch shell "run-as io.hacompanion.watch sh -c 'mkdir -p files && cat /data/local/tmp/hacompanion_token > files/token'; rm -f /data/local/tmp/hacompanion_token"
} finally {
    Remove-Item $tmp -Force
}
& $Adb -s $Watch shell "am force-stop io.hacompanion.watch; am start -n io.hacompanion.watch/.MainActivity" | Out-Null
"Token guardado y HA Companion reiniciado."
