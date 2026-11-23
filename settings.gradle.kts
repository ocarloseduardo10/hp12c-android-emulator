/*
 * HP 12C Mobile — Emulador Financeiro para Android
 * Configuração do projeto Gradle multi-módulo:
 *   - engine : biblioteca Java pura com o motor financeiro (fork do Finanx 12c, MIT)
 *   - app    : aplicativo Android (Kotlin + XML Views) que consome o motor
 *
 * Repositórios ordenados para evitar rate-limit do Maven Central:
 * Aliyun primeiro (mirror liberal), Google para AndroidX, Maven Central/Gradle Portal
 * como fallback final.
 */
pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()
    }
}

rootProject.name = "hp12c-android"
include(":engine")
include(":app")
