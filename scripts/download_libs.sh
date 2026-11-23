#!/bin/bash
# Baixa dependências Android necessárias (AARs/JARs) do mirror Aliyun
# para evitar rate-limit do Maven Central.
set -e
DEST=/home/z/my-project/tools/android-libs
mkdir -p "$DEST"

ALIYUN_GOOGLE="https://maven.aliyun.com/repository/google"
ALIYUN_PUBLIC="https://maven.aliyun.com/repository/public"

# Lista de dependências no formato: group_path artifact version tipo
DEPS=(
  # AndroidX core
  "androidx/core/core/1.13.1/core-1.13.1.aar"
  "androidx/core/core-ktx/1.13.1/core-ktx-1.13.1.aar"
  "androidx/appcompat/appcompat/1.7.0/appcompat-1.7.0.aar"
  "androidx/appcompat/appcompat-resources/1.7.0/appcompat-resources-1.7.0.aar"
  "androidx/constraintlayout/constraintlayout/2.1.4/constraintlayout-2.1.4.aar"
  "androidx/gridlayout/gridlayout/1.0.0/gridlayout-1.0.0.aar"
  "androidx/activity/activity/1.9.0/activity-1.9.0.aar"
  "androidx/activity/activity-ktx/1.9.0/activity-ktx-1.9.0.aar"
  "androidx/lifecycle/lifecycle-runtime/2.8.2/lifecycle-runtime-2.8.2.aar"
  "androidx/lifecycle/lifecycle-runtime-ktx/2.8.2/lifecycle-runtime-ktx-2.8.2.aar"
  "androidx/lifecycle/lifecycle-viewmodel/2.8.2/lifecycle-viewmodel-2.8.2.aar"
  "androidx/lifecycle/lifecycle-viewmodel-ktx/2.8.2/lifecycle-viewmodel-ktx-2.8.2.aar"
  "androidx/lifecycle/lifecycle-viewmodel-savedstate/2.8.2/lifecycle-viewmodel-savedstate-2.8.2.aar"
  "androidx/lifecycle/lifecycle-common/2.8.2/lifecycle-common-2.8.2.jar"
  "androidx/lifecycle/lifecycle-livedata/2.8.2/lifecycle-livedata-2.8.2.aar"
  "androidx/lifecycle/lifecycle-livedata-core/2.8.2/lifecycle-livedata-core-2.8.2.aar"
  "androidx/lifecycle/lifecycle-livedata-core-ktx/2.8.2/lifecycle-livedata-core-ktx-2.8.2.aar"
  "androidx/savedstate/savedstate/1.2.1/savedstate-1.2.1.aar"
  "androidx/mainactivity/mainactivity/1.2.0/mainactivity-1.2.0.aar"
  "androidx/window/window/1.3.0/window-1.3.0.aar"
  "androidx/window/window-java/1.3.0/window-java-1.3.0.aar"
  "androidx/datastore/datastore/1.1.1/datastore-1.1.1.aar"
  "androidx/datastore/datastore-preferences/1.1.1/datastore-preferences-1.1.1.aar"
  "androidx/datastore/datastore-core/1.1.1/datastore-core-1.1.1.aar"
  "androidx/datastore/datastore-core-android/1.1.1/datastore-core-android-1.1.1.aar"
  "androidx/annotation/annotation/1.8.0/annotation-1.8.0.jar"
  "androidx/annotation/annotation-experimental/1.4.0/annotation-experimental-1.4.0.aar"
  "androidx/collection/collection/1.4.0/collection-1.4.0.jar"

  # Material Components
  "com/google/android/material/material/1.12.0/material-1.12.0.aar"

  # Kotlin
  "org/jetbrains/kotlin/kotlin-stdlib/1.9.24/kotlin-stdlib-1.9.24.jar"
  "org/jetbrains/kotlin/kotlin-stdlib-common/1.9.24/kotlin-stdlib-common-1.9.24.jar"
  "org/jetbrains/kotlinx/kotlinx-coroutines-android/1.7.3/kotlinx-coroutines-android-1.7.3.jar"
  "org/jetbrains/kotlinx/kotlinx-coroutines-core/1.7.3/kotlinx-coroutines-core-1.7.3.jar"
)

# Determina qual mirror usar (google para androidx.*, public para outros)
get_url() {
  local dep_path=$1
  if [[ "$dep_path" == androidx/* ]]; then
    echo "$ALIYUN_GOOGLE/$dep_path"
  else
    echo "$ALIYUN_PUBLIC/$dep_path"
  fi
}

download_one() {
  local dep_path=$1
  local filename=$(basename "$dep_path")
  local group_path=$(dirname "$dep_path")
  local dest_dir="$DEST/$group_path"
  mkdir -p "$dest_dir"

  if [ -f "$dest_dir/$filename" ] && [ $(stat -c%s "$dest_dir/$filename" 2>/dev/null) -gt 1000 ]; then
    return 0
  fi

  local url=$(get_url "$dep_path")
  curl -sL --max-time 30 -o "$dest_dir/$filename" "$url"
  local size=$(stat -c%s "$dest_dir/$filename" 2>/dev/null)
  if [ -z "$size" ] || [ "$size" -lt 1000 ]; then
    echo "FALHA: $dep_path"
    rm -f "$dest_dir/$filename"
    return 1
  else
    echo "OK: $dep_path (${size} bytes)"
    return 0
  fi
}

# Baixar em paralelo (3 por vez para não estourar rate limit)
count=0
for dep in "${DEPS[@]}"; do
  download_one "$dep" &
  count=$((count + 1))
  if [ $((count % 3)) -eq 0 ]; then
    wait
  fi
done
wait

echo "---"
echo "=== Total baixado ==="
du -sh $DEST
ls -la $DEST/
