package com.ruslan.foodtracker.data.source.remote

import com.ruslan.foodtracker.data.remote.api.OpenFoodFactsApi
import com.ruslan.foodtracker.data.remote.api.handleApi
import com.ruslan.foodtracker.data.remote.dto.ProductResponse
import com.ruslan.foodtracker.data.remote.dto.SearchResponse
import com.ruslan.foodtracker.domain.model.NetworkResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Remote data source для работы с Open Food Facts API
 * Возвращает DTO модели (без маппинга в Domain)
 */
class FoodRemoteDataSource @Inject constructor(
    private val api: OpenFoodFactsApi
) {
    /**
     * Поиск продуктов по названию через API
     *
     * @param query Поисковый запрос
     * @param page Номер страницы (начинается с 1)
     * @param pageSize Количество результатов на странице
     * @return Flow с SearchResponse (содержит список ProductDto и информацию о пагинации)
     */
    fun searchProducts(
        query: String,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<NetworkResult<SearchResponse>> =
        handleApi { api.searchProducts(searchTerms = query, page = page, pageSize = pageSize) }

    /**
     * Получить продукт по штрих-коду через API
     *
     * @param barcode Штрих-код продукта
     * @return Flow с ProductResponse (содержит ProductDto)
     */
    fun getProductByBarcode(barcode: String): Flow<NetworkResult<ProductResponse>> =
        handleApi { api.getProductByBarcode(barcode) }
}