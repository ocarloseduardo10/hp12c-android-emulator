# HP 12C Mobile — Emulador Financeiro para Android

Calculadora financeira profissional inspirada na HP 12C, com motor de cálculo em Java puro (BigDecimal, 34 dígitos de precisão) derivado do projeto **Finanx 12c** (MIT, Fabio Lima, 2007-2020) e interface em Kotlin + XML Views fiel ao layout físico da calculadora.

## Status do projeto

| Item | Estado |
|------|--------|
| Motor financeiro (Java, MIT) | ✅ 75 testes JUnit passando |
| Interface HP 12C (Kotlin + Views) | ✅ Display LCD + 40 teclas |
| Modo horizontal preferencial | ✅ `sensorLandscape` no manifest |
| Modo vertical adaptativo | ✅ Layout LinearLayout responsivo |
| Persistência de estado | ✅ SharedPreferences (JSON) |
| Indicadores BEGIN/D.MY/C/PRGM/f/g | ✅ No display LCD |
| APK assinado | ✅ v2 + v3 signature schemes |
| Funciona offline | ✅ Sem permissões de rede |
| Min SDK 24 (Android 7.0+) | ✅ ~98% dos dispositivos |
| Target SDK 34 (Android 14) | ✅ Atualizado |

## Estrutura do projeto

```
hp12c-android/
├── settings.gradle.kts          # Configuração Gradle multi-módulo
├── build.gradle.kts              # Build script raiz (plugins AGP + Kotlin)
├── gradle.properties             # JVM args, AndroidX, etc.
├── local.properties              # Aponta para Android SDK
├── proguard-rules.pro            # Regras de ofuscação (preventivas)
├── gradle/wrapper/               # Gradle wrapper 8.7
│
├── engine/                       # Módulo Java puro (motor financeiro)
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/net/sf/finanx/fx12c/
│       │   ├── calc/             # Calculator, Key, Op, Display, Flags, etc.
│       │   ├── memory/           # Stack, FinanceMemory, GeneralMemory, ProgramMemory
│       │   ├── math/             # Number (BigDecimal de alta precisão)
│       │   └── utils/            # Date, Timer, StringEscape
│       └── test/java/            # 75 testes JUnit
│
└── app/                          # Módulo Android (Kotlin)
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml   # package, permissions, activity, orientação
        ├── java/com/hp12c/android/ui/
        │   ├── MainActivity.kt          # Activity + LifecycleOwner
        │   ├── CalculatorViewModel.kt   # MVVM com LiveData
        │   ├── CalculatorKeyView.kt     # View custom para teclas
        │   ├── LcdDisplayView.kt        # View custom para display LCD
        │   ├── HpKeyLayout.kt           # Layout 4x10 das teclas HP 12C
        │   ├── KeyPalette.kt           # Cores das teclas (bege/laranja/azul/escuro)
        │   └── StateRepository.kt       # Persistência JSON em SharedPreferences
        └── res/
            ├── layout/activity_main.xml # 4x10 LinearLayout (paisagem)
            ├── values/
            │   ├── attrs.xml           # Styleables custom (key_code, key_main, etc.)
            │   ├── colors.xml          # Paleta HP 12C
            │   ├── strings.xml          # Strings PT-BR
            │   ├── styles.xml           # Estilo HPKey
            │   └── themes.xml           # Tema DeviceDefault.NoActionBar
            ├── drawable/                # Ícone foreground/background vetorial
            ├── mipmap-*/                # Ícone em 5 densidades
            └── xml/                     # Backup rules
```

## Como construir

### Opção 1 — Android Studio (recomendado)

1. Abra o Android Studio
2. `File` → `Open` → selecione a pasta `hp12c-android/`
3. Aguarde a sincronização do Gradle (pode levar alguns minutos na primeira vez)
4. `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
5. O APK será gerado em `app/build/outputs/apk/debug/app-debug.apk`

### Opção 2 — Linha de comando com Gradle

```bash
# Pré-requisitos:
#  - JDK 17 ou 21
#  - Android SDK com platform-tools, platforms;android-34, build-tools;34.0.0
#  - Variáveis JAVA_HOME e ANDROID_HOME configuradas

cd hp12c-android
./gradlew :app:assembleDebug     # Debug APK
./gradlew :app:assembleRelease   # Release APK (assinado com keystore de debug)

# APK gerado em:
#   app/build/outputs/apk/debug/app-debug.apk
#   app/build/outputs/apk/release/app-release.apk
```

### Opção 3 — Build manual (sem Gradle)

Incluso neste projeto: `scripts/build_apk.sh`. Constrói o APK usando
javac + kotlinc + d8 + aapt2 + apksigner diretamente. Útil para ambientes
com restrições de memória onde o Gradle não consegue executar.

```bash
./scripts/build_apk.sh
# APK gerado em: /home/z/my-project/download/hp12c-mobile-debug.apk
```

## Instalação

```bash
# Habilitar fontes desconhecidas em Configurações > Segurança
adb install hp12c-mobile-debug.apk

# Ou copie o APK para o celular e toque para instalar
```

## Funcionalidades implementadas

### Operações RPN (Notação Polonesa Reversa)
- Pilha de 4 registradores (X, Y, Z, T) com comportamento idêntico à HP 12C
- Tecla ENTER (empilha), R↓ (rotaciona), x↔y (troca), CLx (limpa X)
- LastX (recupera último X usado em operação)

### Operações aritméticas
- `+ - × ÷` com preservação correta da pilha
- `yˣ` (potenciação), `1/x` (recíproco), `√` (raiz)
- `%` (porcentagem), `Δ%` (variação percentual), `%T` (porcentagem do total)
- `CHS` (troca de sinal), `EEX` (notação científica)

### Funções financeiras (TVM)
- `n` (número de períodos), `i` (taxa de juros)
- `PV` (valor presente), `PMT` (pagamento periódico), `FV` (valor futuro)
- Cálculo automático de qualquer variável a partir das outras 4
- `BEGIN` / `END` (pagamentos no início ou fim do período)
- `12÷` e `12×` (conversão de taxas anuais ↔ mensais)

### Funções financeiras avançadas
- `AMORT` (amortização)
- `NPV` (valor presente líquido)
- `IRR` (taxa interna de retorno)
- `BOND` / `YTM` (preço e yield de títulos)
- `SL`, `SOYD`, `DB` (depreciação: linear, soma dos dígitos, declínio)
- `RND` (arredondamento conforme precisão configurada)

### Funções estatísticas
- `Σ+`, `Σ-` (entrada/remoção de dados)
- `x̄` (média), `s` (desvio padrão)
- `x̄r`, `x̄w` (média ponderada)
- Regressão linear com `x̂r` e `ŷr`

### Funções de data
- `D.MY` / `M.DY` (formato de data)
- `ΔDYS` (dias entre datas)
- `DATE` (data futura + dia da semana)

### Memória e registradores
- 20 registradores (R0-R9, .0-.9) com `STO` / `RCL`
- Aritmética com memória (`STO+`, `STO-`, `STO×`, `STO÷`, `RCL+`, etc.)
- `f CLEAR REG` (limpa todos os registradores)
- `f CLEAR FIN` (limpa registradores financeiros)
- `f CLEAR Σ` (limpa registradores estatísticos)
- `f CLEAR PRGM` (limpa memória de programa)

### Configurações
- Casas decimais: `f` + dígito (0-9) define precisão
- Separador decimal: `ON` + `.` alterna entre ponto e vírgula
- Persistência completa: estado da pilha, memória financeira, geral e
  configurações são salvos em SharedPreferences e restaurados ao reabrir

## Licenças

### Motor financeiro (engine/)
- **MIT License** — Copyright (c) 2007-2020 Fabio Lima
- Origem: [finanx-12c](https://github.com/fabiolimace/finanx-12c)
- 75 testes JUnit validados contra valores de referência HP 12C

### Aplicativo Android (app/)
- Código próprio, licença MIT
- UI em Kotlin + XML Views, MVVM com LiveData
- View customizadas (`CalculatorKeyView`, `LcdDisplayView`)

### Bibliotecas de terceiros
- AndroidX AppCompat 1.7.0 — Apache 2.0
- AndroidX Core KTX 1.13.1 — Apache 2.0
- Material Components for Android 1.12.0 — Apache 2.0
- AndroidX Lifecycle 2.8.2 (runtime, viewmodel, livedata) — Apache 2.0
- AndroidX Activity 1.9.0 — Apache 2.0
- Kotlin Coroutines 1.7.3 — Apache 2.0
- Kotlin Stdlib 1.9.24 — Apache 2.0

## Marcas registradas

HP 12C é marca registrada da HP Inc. Este aplicativo é uma ferramenta
educacional independente e não é afiliada, endossada ou patrocinada pela HP Inc.

## Build manual — detalhes técnicos

O projeto pode ser construído SEM Gradle usando `scripts/build_apk.sh`.
Isto é útil quando:
- O ambiente tem restrições de memória (< 4 GB RAM)
- O Maven Central está rate-limitando downloads
- Você quer entender o pipeline de build Android

O script faz:
1. Compila o engine Java com `javac --release 8` (bytecode Java 8 para máxima compatibilidade)
2. Extrai `classes.jar` de cada AAR (apenas os que têm classes reais)
3. Compila recursos com `aapt2 compile --dir`
4. Linka o APK base com `aapt2 link` (gera `R.java`)
5. Compila `R.java` com `javac` (gera `R.class`)
6. Compila os fontes Kotlin com `K2JVMCompiler` (incluindo `R.class` no classpath)
7. Empacota engine + app em JARs
8. Converte todos os .class para `classes.dex` usando `d8` (R8 estável 8.5.35 —
   as builds "dev" nos build-tools têm bug NPE em enums Java 21)
9. Adiciona `classes.dex` ao APK base
10. Assina com `apksigner` usando keystore de debug

## Contato e contribuição

Para reportar bugs ou contribuir, consulte o repositório de origem:
- Motor: https://github.com/fabiolimace/finanx-12c (MIT, Fabio Lima)
