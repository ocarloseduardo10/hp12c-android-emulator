/*
 * engine — biblioteca Java pura contendo o motor financeiro HP 12C.
 *
 * Conteúdo:
 *  - Pacote net.sf.finanx.fx12c.* (Calculadora, pilha RPN, memória financeira,
 *    memória geral, matemática de alta precisão, flags, display, configuração).
 *  - Refatoração mínima sobre o projeto Finanx 12c (MIT, Fabio Lima, 2007-2020):
 *    removido o acoplamento direto com Swing (Controller/MainWindow) e
 *    introduzida uma interface CalculatorListener para notificação de UI.
 *  - Testes JUnit 4 que validam as operações contra valores de referência HP 12C.
 */
plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Compila com o JDK disponível no ambiente (JDK 21), com target 17.
// O toolchain seria ideal, mas evita dependência de download.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    // JSON parsing para os 242 vetores de teste do projeto hp12c-emulator-rust
    testImplementation("org.json:json:20240303")
}

tasks.withType<Test> {
    useJUnit()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
}

tasks.jar {
    archiveBaseName.set("hp12c-engine")
}
