#!/bin/sh
# The version lives in gradle.properties (`version`); both apps derive versionName and
# versionCode from it. It must be a version like 0.1.16 and CHANGELOG.md needs a section for it.
# Prints the version; exit 1 when anything is off.
# SPDX-License-Identifier: Apache-2.0
set -eu
cd "$(dirname "$0")/.."
v=$(sed -n 's/^version=\([^[:space:]]*\).*/\1/p' gradle.properties | head -n 1)
if [ -z "$v" ]; then
    echo "gradle.properties has no version=" >&2
    exit 1
fi
if ! printf '%s\n' "$v" | grep -Eq '^[0-9]+\.[0-9]{1,2}\.[0-9]{1,2}(-[0-9A-Za-z.]+)?$'; then
    echo "version \"$v\" is not a version like 0.1.16 (minor and patch below 100 for versionCode)" >&2
    exit 1
fi
if ! sh tools/release-notes.sh "$v" > /dev/null; then
    echo "CHANGELOG.md has no section for $v" >&2
    exit 1
fi
echo "$v"
