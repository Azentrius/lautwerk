#!/system/bin/sh
# Limited, package-filtered snapshots; each logcat exits and flushes its file.
# Run through adb shell, not as root. Does not change Android Auto settings.
set -eu

session="${1:-}"
seconds="${2:-1200}"
case "$session" in
    ''|*[!a-zA-Z0-9_-]*) echo 'Invalid session identifier' >&2; exit 2 ;;
esac
case "$seconds" in
    ''|0*|*[!0-9]*) echo 'Invalid duration' >&2; exit 2 ;;
esac
if [ "$seconds" -lt 5 ] || [ "$seconds" -gt 1800 ]; then
    echo 'Duration must be 5 to 1800 seconds' >&2
    exit 2
fi

base="/data/local/tmp/openauto-car-$session"
if [ -e "$base.status" ] || [ -e "$base.log" ]; then
    echo 'Session already exists; choose a new identifier' >&2
    exit 2
fi
since=$(date +%s)
deadline=$((since + seconds))
printf 'running start=%s deadline=%s\n' "$since" "$deadline" > "$base.status"
trap 'printf "interrupted\n" >> "$base.status"; exit 0' HUP INT TERM

while :; do
    current=$(date +%s)
    logcat -d -b main -b system -v epoch -T "$since.000" \
        -e 'dev[.]openautoai[.]prototype[.]dhu|^car[.](createHostValidator|onCreateSession|onCreateScreen|onGetTemplate|start)|isPackageInstalledByPlayCheck service not connected' \
        -f "$base.log" -r 128 -n 2 '*:V'
    if [ "$current" -ge "$deadline" ]; then
        break
    fi
    # One-second overlap avoids gaps at the time boundary. Deduplicate on reading.
    since=$((current - 1))
    sleep 5
done
printf 'completed end=%s\n' "$(date +%s)" >> "$base.status"
