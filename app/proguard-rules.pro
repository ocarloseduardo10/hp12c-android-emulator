# ProGuard — regras de ofuscação para a release.
# Como minifyEnabled=false no build release atual, este arquivo é preventivo.

# Motor finanx-12c (Java): mantém nomes de classe/método usados via reflection
-keep class net.sf.finanx.fx12c.** { *; }

# Compose (já gerenciado pelo AAR): mantém annotations
-dontwarn androidx.compose.**
