# HP 12C Android Emulator

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language: Kotlin + Java](https://img.shields.io/badge/Language-Kotlin%20%2B%20Java-orange.svg)](https://kotlinlang.org)
[![Min SDK: 24](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-blue.svg)](https://developer.android.com/about/versions/nougat)

> **RPN financial calculator for Android** — pure-Java BigDecimal engine (fork of [Finanx 12c](https://github.com/fabiolimace/finanx-12c), MIT) with a Kotlin UI inspired by the classic HP 12C physical calculator.

> ⚠️ **Disclaimer**: This is an independent **educational project** and is **not affiliated with, endorsed by, or sponsored by HP Inc.** "HP 12C" is a trademark of HP Inc. This emulator replicates essential functions for learning purposes.

## ✨ Features

### Arithmetic & RPN
- ✅ 4-register RPN stack (X, Y, Z, T) with proper lift/drop behavior
- ✅ `+ - × ÷` with stack preservation
- ✅ `yˣ`, `1/x`, `√`, `ln`, `eˣ`, `CHS`, `EEX`
- ✅ `%`, `Δ%`, `%T` (percentage of total)

### Financial (TVM)
- ✅ `n` (periods), `i` (interest rate), `PV`, `PMT`, `FV`
- ✅ `BEGIN` / `END` modes
- ✅ `12÷` / `12×` (rate conversion)

### Advanced Financial
- ✅ `AMORT` (amortization)
- ✅ `NPV` (net present value)
- ✅ `IRR` (internal rate of return)
- ✅ `BOND` (bond price)
- ✅ Depreciation: `SL`, `SOYD`, `DB`
- ✅ `RND` (rounding)

### Statistics
- ✅ `Σ+`, `Σ-` (data entry)
- ✅ `x̄` (mean), `s` (std dev), `x̄w` (weighted mean)
- ✅ Linear regression with `x̂r`, `ŷr`

### Memory & Dates
- ✅ 20 storage registers (`R0`-`R9`, `.0`-`.9`)
- ✅ Memory arithmetic (`STO+`, `STO-`, `STO×`, `STO÷`, `RCL+`, ...)
- ✅ `D.MY` / `M.DY` (date formats)
- ✅ `ΔDYS` (days between dates), `DATE`

### UI & Persistence
- ✅ 39-key layout in two sections (function keys + numeric keypad), HP 12C Platinum style
- ✅ LCD-style display with `BEGIN`, `D.MY`, `C`, `PRGM`, `f`, `g` indicators
- ✅ SharedPreferences persistence (full state restored on reopen)
- ✅ Haptic feedback on key press
- ✅ 100% offline — no network permissions

## 📊 Test Coverage

The financial engine passes **75 JUnit tests** validating results against HP 12C reference values:

| Suite | Tests | Coverage |
|-------|-------|----------|
| `CalculatorTest` | 4 | Basic arithmetic, %, RPN |
| `StackTest` | 15+ | Stack behavior, dates, RPN operations |
| `FinanceMemoryTest` | 10+ | TVM calculations |
| `GeneralMemoryTest` | 10+ | Memory registers, statistics |
| `ProgramMemoryTest` (1/2/3) | 20+ | Program memory |
| `NumberTest` | 10+ | BigDecimal precision |

Run tests:
```bash
cd engine
./gradlew test
```

## 🏗️ Architecture

```
hp12c-android-emulator/
├── engine/                          # Java library (MIT — Fabio Lima)
│   ├── src/main/java/net/sf/finanx/fx12c/
│   │   ├── calc/                    # Calculator, Key, Op, Display, Flags
│   │   ├── memory/                  # Stack, FinanceMemory, GeneralMemory
│   │   ├── math/                    # Number (BigDecimal DECIMAL128)
│   │   └── utils/                    # Date, Timer, StringEscape
│   └── src/test/java/               # 75 JUnit tests
│
├── app/                             # Kotlin Android app
│   └── src/main/
│       ├── AndroidManifest.xml      # sensorLandscape, minSdk 24, targetSdk 34
│       ├── java/com/hp12c/android/ui/
│       │   ├── MainActivity.kt      # Activity (framework, no AppCompat)
│       │   ├── CalculatorEngine.kt  # Singleton Kotlin, manages engine
│       │   ├── CalculatorKeyView.kt # Custom View per key
│       │   ├── LcdDisplayView.kt    # Custom View for LCD display
│       │   ├── HpKeyLayout.kt       # 39-key layout definition
│       │   ├── KeyPalette.kt        # Colors (beige/orange/blue/dark)
│       │   ├── StateRepository.kt   # JSON persistence
│       │   └── CalcListeners.kt     # UI callbacks
│       └── res/
│           ├── layout/activity_main.xml  # 2-section keyboard layout
│           ├── values/                   # colors, strings, styles, themes
│           ├── drawable/                 # adaptive icon
│           ├── mipmap-*/                # launcher icons (5 densities)
│           └── xml/                      # backup rules
│
├── gradle/wrapper/                  # Gradle 8.7 wrapper
├── scripts/
│   ├── build_apk.sh                 # Standalone APK build (no Gradle)
│   └── download_libs.sh             # Download AndroidX dependencies
└── build.gradle.kts, settings.gradle.kts, gradle.properties
```

## 🔧 Build

### Option 1: Android Studio (recommended)
1. Clone: `git clone https://github.com/ocarloseduardo10/hp12c-android-emulator.git`
2. Open in Android Studio
3. Sync Gradle → Build → Run

### Option 2: Gradle CLI
```bash
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Option 3: Standalone build (no Gradle)
Useful for environments with limited memory.
```bash
./scripts/build_apk.sh
```

## 📥 Install

```bash
adb install hp12c-mobile-debug.apk
```

Or copy the APK to your Android 7.0+ device and tap to install.

## 🧪 Test the calculator

After installing, try:
```
2 ENTER 3 +      → 5.00
100 ENTER 10 %   → 10.00
360 n 6.5 i 200000 PV PMT  → -1,264.14  (mortgage payment)
```

## 📜 License

**MIT License** — see [LICENSE](LICENSE)

- Engine: Copyright (c) 2007-2020 **Fabio Lima** ([finanx-12c](https://github.com/fabiolimace/finanx-12c))
- Android port & UI: Copyright (c) 2026 **Carlos Santos** ([@ocarloseduardo10](https://github.com/ocarloseduardo10))

## 🙏 Acknowledgments

- **Fabio Lima** — original Finanx 12c engine (MIT), the heart of this calculator
- **HP Inc.** — design inspiration (this project is not affiliated with HP)

## ⚠️ Disclaimer

This software is provided "as is" without warranty. Always verify critical financial calculations with the actual HP 12C calculator or professional financial software.
