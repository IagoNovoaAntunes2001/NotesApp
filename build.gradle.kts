// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    // Convention plugins do build-logic
    alias(libs.plugins.notes.android.library) apply false
    alias(libs.plugins.notes.android.library.compose) apply false
    alias(libs.plugins.notes.android.hilt) apply false
    alias(libs.plugins.notes.android.application) apply false
    alias(libs.plugins.notes.android.application.compose) apply false
    // Valida o grafo de módulos em cada build — protege a arquitetura de regressões
    alias(libs.plugins.modules.graph.assert)
}

// ──────────────────────────────────────────────────────────────────────────────
// modules-graph-assert (https://github.com/jraska/modules-graph-assert)
//
// moduleLayers define camadas em ordem: [0] é a mais alta (app), [último] é a base.
// Um módulo SÓ pode depender de módulos em camadas de índice MAIOR (mais baixas).
// Módulos na MESMA camada NÃO podem depender uns dos outros.
//
// Camada 0 — :app              → pode depender de features + core
// Camada 1 — :home | :detail   → podem depender de core, NUNCA entre si
// Camada 2 — :core:* | :design-system → base, não depende de features
//
// TAREFAS DISPONÍVEIS:
//   ./gradlew generateModuleGraph   → gera arquivo .gv (Graphviz) do grafo
//   ./gradlew assertModuleGraph     → valida as regras (roda no CI)
// ──────────────────────────────────────────────────────────────────────────────
moduleGraphAssert {
    // restricted: pares de módulos que NÃO podem ter dependência entre si.
    // Formato 2.9.0: ":from -X> :to" (o -X> indica dependência PROIBIDA)
    restricted = arrayOf(
        ":home -X> :detail",
        ":detail -X> :home"
    )
}
