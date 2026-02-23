package com.ruslan.foodtracker.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для ответа API при поиске продуктов
 * GET /api/v2/search
 */
data class SearchResponse(
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("page")
    val page: Int? = null,
    @SerializedName("page_count")
    val pageCount: Int? = null,
    @SerializedName("page_size")
    val pageSize: Int? = null,
    @SerializedName("products")
    val products: List<ProductDto>? = null
)
