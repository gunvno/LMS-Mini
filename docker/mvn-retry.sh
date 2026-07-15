#!/bin/sh

set -eu

attempt=1
max_attempts="${MAVEN_DOWNLOAD_ATTEMPTS:-4}"

while [ "$attempt" -le "$max_attempts" ]; do
  echo "Maven attempt ${attempt}/${max_attempts}: mvn $*"

  # Interrupted downloads leave marker files that can poison the shared BuildKit cache.
  if [ -d /root/.m2/repository ]; then
    find /root/.m2/repository -type f \
      \( -name '*.lastUpdated' -o -name '*.part' -o -name '*.tmp' \) \
      -delete
  fi

  if mvn -B -ntp \
      -Dmaven.wagon.http.retryHandler.count=5 \
      -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
      "$@"; then
    exit 0
  fi

  if [ "$attempt" -eq "$max_attempts" ]; then
    echo "Maven failed after ${max_attempts} attempts." >&2
    exit 1
  fi

  attempt=$((attempt + 1))
  sleep $((attempt * 2))
done
