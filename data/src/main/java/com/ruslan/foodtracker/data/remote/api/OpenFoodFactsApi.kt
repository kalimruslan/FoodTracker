package com.ruslan.foodtracker.data.remote.api

import com.ruslan.foodtracker.data.remote.dto.ProductResponse
import com.ruslan.foodtracker.data.remote.dto.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit интерфейс для Open Food Facts API
 * Base URL: https://world.openfoodfacts.net
 */
interface OpenFoodFactsApi {

    /**
     * Получить продукт по штрих-коду
     *
     * @param barcode Штрих-код продукта (EAN-13, EAN-8, UPC-A, etc.)
     * @return Response<ProductResponse> с данными о продукте
     */
    @GET("api/v2/product/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): Response<ProductResponse>

    /**
     * Поиск продуктов по текстовому запросу
     *
     * @param searchTerms Поисковый запрос (название продукта, бренд и т.д.)
     * @param page Номер страницы результатов (по умолчанию 1)
     * @param pageSize Количество результатов на странице (по умолчанию 20)
     * @param fields Поля для возврата (по умолчанию все основные поля)
     * @return Response<SearchResponse> со списком найденных продуктов
     */
    @GET("api/v2/search")
    suspend fun searchProducts(
        @Query("search_terms") searchTerms: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("fields") fields: String = "code,product_name,brands,nutriments,serving_size,image_url,image_front_url,quantity"
    ): Response<SearchResponse>
}