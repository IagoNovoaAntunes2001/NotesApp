package com.notes.core.network.pagination

/**
 * Resultado de uma requisição paginada com cursor.
 *
 * Por que cursor pagination e não offset?
 *
 * OFFSET: SELECT * FROM posts LIMIT 10 OFFSET 20
 *   - Simples, mas: se um item for inserido enquanto o usuário pagina,
 *     itens podem aparecer duplicados ou ser pulados.
 *   - Degrada com tabelas grandes (o DB varre os 20 primeiros para descartar).
 *
 * CURSOR: SELECT * FROM posts WHERE id > :lastId LIMIT 10
 *   - Estável: não importa quantos itens foram inseridos, o cursor aponta
 *     para uma posição fixa no tempo.
 *   - Eficiente: usa índice (id), sem varredura.
 *   - Ideal para feeds e listas que mudam com frequência.
 *
 * @param data lista de itens desta página
 * @param nextCursor id do último item — use como parâmetro na próxima requisição.
 *                   null = chegou na última página
 * @param total total de itens disponíveis no servidor (para mostrar "X de Y")
 */
data class PagedResult<T>(
    val data: List<T>,
    val nextCursor: Int?,
    val total: Int
) {
    val hasMore: Boolean get() = nextCursor != null
}

