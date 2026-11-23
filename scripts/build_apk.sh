#!/bin/bash
# Build manual do APK HP 12C Mobile — sem Gradle.
# Usa kotlinc (K2JVMCompiler), d8 (R8 estável), aapt2 e apksigner diretamente.
#
# Etapas:
#  1. Compilar engine Java (--release 17 para mesma versão de bytecode do Kotlin)
#  2. Extrair classes.jar de cada AAR (com .class files reais)
#  3. Compilar recursos do app com aapt2 compile
#  4. Linkar APK base com aapt2 link (gera R.java)
#  5. Compilar Kotlin sources do app com kotlinc (incluindo R.java)
#  6. Converter todos os .class para classes.dex com d8 (R8 estável)
#  7. Adicionar classes.dex ao APK
#  8. Assinar com debug keystore via apksigner
set -e

# === Paths ===
PROJECT=/home/z/my-project/hp12c-android
# Detectar JDK em qualquer um dos dois locais possíveis
if [ -d /home/z/my-project/tools/jdk-21.0.5+11 ]; then
  JAVA_HOME=/home/z/my-project/tools/jdk-21.0.5+11
elif [ -d /tmp/my-project/tools/jdk-21.0.5+11 ]; then
  JAVA_HOME=/tmp/my-project/tools/jdk-21.0.5+11
elif [ -d /home/z/my-project/tools/jdk-17.0.13+11 ]; then
  JAVA_HOME=/home/z/my-project/tools/jdk-17.0.13+11
elif [ -d /tmp/my-project/tools/jdk-17.0.13+11 ]; then
  JAVA_HOME=/tmp/my-project/tools/jdk-17.0.13+11
else
  echo "ERRO: JDK nao encontrado em /home/z/my-project/tools/ nem /tmp/my-project/tools/"
  exit 1
fi
export JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH
ANDROID_HOME=/home/z/my-project/tools/android-sdk
BUILD_TOOLS=$ANDROID_HOME/build-tools/34.0.0
PLATFORM_JAR=$ANDROID_HOME/platforms/android-34/android.jar

LIBS_DIR=/home/z/my-project/tools/android-libs
ENGINE_SRC=$PROJECT/engine/src/main/java
APP_SRC=$PROJECT/app/src/main/java
APP_RES=$PROJECT/app/src/main/res
APP_ASSETS=$PROJECT/app/src/main/assets
APP_MANIFEST=$PROJECT/app/src/main/AndroidManifest.xml

WORK=/tmp/hp12c-build
rm -rf $WORK
mkdir -p $WORK/{classes-engine,classes-app,extracted,dex,res-compiled,res-linked,generated-java,apk}

# === Step 1: Compilar engine Java ===
echo "=== Step 1: Compilando engine Java (Java 17 bytecode) ==="
javac -encoding UTF-8 --release 17 -d $WORK/classes-engine \
  $(find $ENGINE_SRC -name "*.java") 2>&1 | tail -3
echo "Engine compilada: $(find $WORK/classes-engine -name '*.class' | wc -l) classes"

# === Step 2: Extrair classes.jar de cada AAR ===
echo "=== Step 2: Extraindo classes.jar dos AARs ==="
rm -rf $WORK/extracted
mkdir -p $WORK/extracted
i=0
for aar in $(find $LIBS_DIR -name "*.aar"); do
  if unzip -l $aar 2>/dev/null | grep -q "classes.jar"; then
    name=$(basename $aar .aar)
    safe_name=$(echo $name | tr '.' '_' | tr '-' '_')
    rm -rf /tmp/aar-extract-$i
    mkdir -p /tmp/aar-extract-$i
    unzip -qo $aar classes.jar -d /tmp/aar-extract-$i 2>/dev/null
    if [ -f /tmp/aar-extract-$i/classes.jar ]; then
      if unzip -l /tmp/aar-extract-$i/classes.jar 2>/dev/null | grep -q "\.class$"; then
        cp /tmp/aar-extract-$i/classes.jar $WORK/extracted/lib_${safe_name}.jar
      fi
    fi
  fi
  i=$((i + 1))
done
for jar in $(find $LIBS_DIR -name "*.jar"); do
  if unzip -l $jar 2>/dev/null | grep -q "\.class$"; then
    name=$(basename $jar .jar)
    safe_name=$(echo $name | tr '.' '_' | tr '-' '_')
    cp $jar $WORK/extracted/lib_${safe_name}.jar
  fi
done
# Adicionar kotlin-stdlib explicitamente
if [ -f "$LIBS_DIR/org/jetbrains/kotlin/kotlin-stdlib/1.9.24/kotlin-stdlib-1.9.24.jar" ]; then
  cp $LIBS_DIR/org/jetbrains/kotlin/kotlin-stdlib/1.9.24/kotlin-stdlib-1.9.24.jar $WORK/extracted/lib_kotlin_stdlib.jar
fi
# Adicionar kotlin-reflect (caminho fixo)
KOTLIN_REFLECT_JAR=/home/z/my-project/tools/kotlin-libs/kotlin-reflect-1.9.24.jar
if [ -f "$KOTLIN_REFLECT_JAR" ]; then
  cp $KOTLIN_REFLECT_JAR $WORK/extracted/lib_kotlin_reflect_1_9_24.jar
fi
# Adicionar jetbrains-annotations
ANNOTATIONS_JAR=$LIBS_DIR/org/jetbrains/annotations/23.0.0/annotations-23.0.0.jar
if [ -f "$ANNOTATIONS_JAR" ]; then
  cp $ANNOTATIONS_JAR $WORK/extracted/lib_annotations_23_0_0.jar
fi

# Dedup: remover kotlin-stdlib duplicado (lib_kotlin_stdlib_1_9_24.jar é o mesmo que lib_kotlin_stdlib.jar)
rm -f $WORK/extracted/lib_kotlin_stdlib_1_9_24.jar
echo "Bibliotecas extraídas: $(ls $WORK/extracted/*.jar | wc -l) JARs"

# === Step 3: Compilar recursos com aapt2 ===
echo "=== Step 3: Compilando recursos do app com aapt2 ==="
mkdir -p $WORK/res-input
cp -r $APP_RES/* $WORK/res-input/ 2>/dev/null || true
$BUILD_TOOLS/aapt2 compile --dir $WORK/res-input -o $WORK/res-compiled.zip 2>&1 | tail -5
echo "Recursos compilados: $(ls -la $WORK/res-compiled.zip 2>&1)"

# === Step 4: Linkar APK base com aapt2 (gera R.java) ===
echo "=== Step 4: Linkando APK base com aapt2 (gera R.java) ==="
mkdir -p $WORK/generated-java
$BUILD_TOOLS/aapt2 link \
  --manifest $APP_MANIFEST \
  -I $PLATFORM_JAR \
  --java $WORK/generated-java \
  -o $WORK/res-linked/base.apk \
  --min-sdk-version 24 \
  --target-sdk-version 34 \
  --auto-add-overlay \
  $WORK/res-compiled.zip 2>&1 | tail -5
echo "APK base: $(ls -la $WORK/res-linked/base.apk 2>&1)"
echo "R.java gerado: $(find $WORK/generated-java -name "*.java")"

# === Step 5: Compilar Kotlin sources do app (incluindo R.java) ===
echo "=== Step 5: Compilando Kotlin sources do app ==="
CLASSPATH=$PLATFORM_JAR
for jar in $WORK/extracted/*.jar; do
  CLASSPATH=$CLASSPATH:$jar
done
CLASSPATH=$CLASSPATH:$WORK/classes-engine:$WORK/generated-java

KOTLIN_JAR=/home/z/my-project/tools/kotlin-libs/kotlin-compiler-embeddable-1.9.24.jar
KOTLIN_STDLIB=$WORK/extracted/lib_kotlin_stdlib.jar
TROVE_JAR=/home/z/my-project/tools/kotlin-libs/trove4j-1.0.20200330.jar

if [ -z "$KOTLIN_JAR" ]; then
  echo "ERRO: kotlin-compiler-embeddable não encontrado"
  exit 1
fi
echo "Usando Kotlin compiler: $KOTLIN_JAR"

KOTLIN_FILES=$(find $APP_SRC -name "*.kt")
JAVA_FILES=$(find $WORK/generated-java -name "*.java")
echo "Arquivos Kotlin: $(echo $KOTLIN_FILES | wc -w), Java: $(echo $JAVA_FILES | wc -w)"

KOTLIN_CP=$KOTLIN_JAR
[ -f "$KOTLIN_STDLIB" ] && KOTLIN_CP=$KOTLIN_CP:$KOTLIN_STDLIB
[ -f "$TROVE_JAR" ] && KOTLIN_CP=$KOTLIN_CP:$TROVE_JAR
[ -f "$WORK/extracted/lib_kotlin_reflect_1_9_24.jar" ] && KOTLIN_CP=$KOTLIN_CP:$WORK/extracted/lib_kotlin_reflect_1_9_24.jar
# jetbrains-annotations (necessária pelo Kotlin compiler para gerar @NotNull etc)
ANNOTATIONS_JAR=$LIBS_DIR/org/jetbrains/annotations/23.0.0/annotations-23.0.0.jar
if [ -f "$ANNOTATIONS_JAR" ]; then
  KOTLIN_CP=$KOTLIN_CP:$ANNOTATIONS_JAR
fi

# Primeiro compilar R.java (Java puro) para .class files, para o Kotlin compiler encontrar
if [ -n "$JAVA_FILES" ]; then
  echo "Compilando R.java..."
  javac -encoding UTF-8 --release 17 -cp $CLASSPATH -d $WORK/classes-app $JAVA_FILES 2>&1 | tail -5
  echo "R.class criado? $([ -f $WORK/classes-app/com/hp12c/android/R.class ] && echo 'SIM' || echo 'NÃO')"
  find $WORK/classes-app -name "*.class" | head -15
fi

# Agora compilar Kotlin (vai encontrar R.class no classpath)
java -cp $KOTLIN_CP org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -classpath $CLASSPATH:$WORK/classes-app \
  -d $WORK/classes-app \
  -jvm-target 17 \
  -no-reflect \
  -Xjvm-default=all-compatibility \
  $KOTLIN_FILES 2>&1 | tail -20
echo "Kotlin app compilado: $(find $WORK/classes-app -name '*.class' | wc -l) classes"

# === Step 6: Empacotar engine + app em JARs e dexar com R8 estável ===
echo "=== Step 6: Convertendo para dex com d8 (R8 estável 8.5.35) ==="
cd $WORK/classes-engine && jar cf $WORK/engine.jar -C . .
cd $WORK/classes-app && jar cf $WORK/app.jar -C . .

R8_JAR=/home/z/my-project/tools/r8.jar
if [ ! -f "$R8_JAR" ]; then
  echo "Baixando R8 8.5.35 estável..."
  curl -sL -o $R8_JAR "https://maven.aliyun.com/repository/google/com/android/tools/r8/8.5.35/r8-8.5.35.jar"
fi

# CRÍTICO: combinar TODOS os .class em um único mega-diretório antes de dexar.
# d8 às vezes falha em incluir classes de inputs múltiplos quando há conflitos
# de package entre JARs (ex.: engine finanx tem classes em net.sf.finanx.fx12c
# que colidem com metadata-only jars de libs androidx). Extraímos tudo em
# um único diretório e passamos como lista de .class files ao d8.
echo "Combinando todas as classes em diretório único..."
rm -rf $WORK/classes-all
mkdir -p $WORK/classes-all
# Engine
cd $WORK/classes-engine && find . -name "*.class" -exec cp --parents {} $WORK/classes-all/ \;
# App
cd $WORK/classes-app && find . -name "*.class" -exec cp --parents {} $WORK/classes-all/ \;
# Bibliotecas (extrair .class de cada JAR)
for jar in $WORK/extracted/*.jar; do
  cd $WORK/classes-all && unzip -qo $jar 2>/dev/null || true
done
# Remover module-info.class e assinaturas META-INF que causam problemas
find $WORK/classes-all -name "module-info.class" -delete 2>/dev/null
find $WORK/classes-all -name "META-INF" -type d -exec rm -rf {} + 2>/dev/null
# Remover .kotlin_module e .kotlin_metadata (não são .class)
find $WORK/classes-all -name "*.kotlin_module" -delete 2>/dev/null
find $WORK/classes-all -name "*.knm" -delete 2>/dev/null
echo "Total classes combinadas: $(find $WORK/classes-all -name '*.class' | wc -l)"

# Agora dexar a partir do diretório único.
# IMPORTANTE: passar 7600+ arquivos .class como argumentos na linha de comando
# excede ARG_MAX do shell, e os últimos arquivos são silenciosamente descartados.
# Por isso criamos um único mega-JAR contendo TODAS as classes combinadas.
echo "Criando mega-JAR com todas as classes combinadas..."
cd $WORK/classes-all
rm -f $WORK/all-classes.jar
jar cf $WORK/all-classes.jar -C . .
echo "Tamanho do mega-JAR: $(ls -la $WORK/all-classes.jar | awk '{print $5}') bytes"
echo "Classes no mega-JAR: $(unzip -l $WORK/all-classes.jar | grep -c '\.class')"

mkdir -p $WORK/dex
java -cp $R8_JAR com.android.tools.r8.D8 \
  --min-api 24 \
  --lib $PLATFORM_JAR \
  --output $WORK/dex \
  $WORK/all-classes.jar 2>&1 | tail -10
echo "DEX(es) gerados: $(ls $WORK/dex/classes*.dex 2>&1)"
echo "Total de classes em todos os DEXs:"
total=0
for dex in $WORK/dex/classes*.dex; do
  if [ -f "$dex" ]; then
    c=$($BUILD_TOOLS/dexdump $dex 2>/dev/null | grep -c "Class descriptor" || true)
    fn=$($BUILD_TOOLS/dexdump $dex 2>/dev/null | grep "Class descriptor" | grep -c "finanx" || true)
    echo "  $(basename $dex): $c classes ($fn finanx)"
    total=$((total + c))
  fi
done
echo "TOTAL: $total classes"

# === Step 7: Adicionar TODOS os classes*.dex ao APK base ===
echo "=== Step 7: Adicionando classes*.dex ao APK ==="
cd $WORK/res-linked
cp base.apk hp12c-unsigned.apk
# CRÍTICO: o d8 pode gerar múltiplos DEXs (classes.dex, classes2.dex, classes3.dex)
# quando o tamanho excede 65K methods. Precisamos incluir TODOS no APK.
# Como minSdk=24 (Android 7.0+), o ART carrega multidex nativamente.
cp $WORK/dex/classes*.dex .
for dex in classes*.dex; do
  if [ -f "$dex" ]; then
    zip -q hp12c-unsigned.apk "$dex"
    echo "Adicionado: $dex ($(stat -c%s $dex) bytes)"
  fi
done
echo "APK com dex(es): $(ls -la hp12c-unsigned.apk)"

# === Step 8: Assinar com debug keystore via apksigner ===
echo "=== Step 8: Assinando APK ==="
KEYSTORE=/home/z/my-project/tools/debug.keystore
if [ ! -f $KEYSTORE ]; then
  echo "Gerando debug keystore..."
  keytool -genkey -v -keystore $KEYSTORE \
    -alias androiddebugkey \
    -dname "CN=Android Debug,O=Android,C=US" \
    -storepass android -keypass android \
    -keyalg RSA -keysize 2048 -validity 10000 2>&1 | tail -3
fi

FINAL_APK=/home/z/my-project/download/hp12c-mobile-debug.apk
mkdir -p /home/z/my-project/download
$BUILD_TOOLS/apksigner sign \
  --ks $KEYSTORE \
  --ks-pass pass:android \
  --key-pass pass:android \
  --out $FINAL_APK \
  hp12c-unsigned.apk 2>&1 | tail -5

echo "=== APK FINAL ==="
ls -la $FINAL_APK
echo "---"
$BUILD_TOOLS/apksigner verify --verbose $FINAL_APK 2>&1 | head -5
