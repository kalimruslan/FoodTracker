package com.ruslan.foodtracker.domain.model

/**
 * Обертка для пагинированных результатов
 *
 * Используется для передачи данных с информацией о пагинации
 * из data слоя через domain слой в presentation
 *
 * @param T тип элементов в списке
 * @param data список элементов текущей страницы
 * @param currentPage текущая страница (начинается с 1)
 * @param totalPages общее количество страниц
 * @param pageSize количество элементов на странице
 * @param totalCount общее количество элементов
 */
data class PaginatedResult<T>(
    val data: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val pageSize: Int,
    val totalCount: Int
) {
    /**
     * Есть ли следующая страница для загрузки
     */
    val hasNextPage: Boolean
        get() = currentPage < totalPages

    /**
     * Есть ли предыдущая страница
     */
    val hasPreviousPage: Boolean
        get() = currentPage > 1

    /**
     * Пустой ли результат (нет данных)
     */
    val isEmpty: Boolean
        get() = data.isEmpty()

    companion object {
        /**
         * Создать пустой результат пагинации
         */
        fun <T> empty(): PaginatedResult<T> = PaginatedResult(
            data = emptyList(),
            currentPage = 1,
            totalPages = 0,
            pageSize = 0,
            totalCount = 0
        )

        /**
         * Создать результат с одной страницей (без пагинации)
         * Используется для fallback на локальный кэш
         */
        fun <T> single(data: List<T>): PaginatedResult<T> = PaginatedResult(
            data = data,
            currentPage = 1,
            totalPages = 1,
            pageSize = data.size,
            totalCount = data.size
        )
    }
}