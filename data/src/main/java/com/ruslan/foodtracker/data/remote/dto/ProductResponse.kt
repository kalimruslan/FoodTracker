package com.ruslan.foodtracker.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для ответа API при получении продукта по штрих-коду
 * GET /api/v2/product/{barcode}
 */
data class ProductResponse(
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("product")
    val product: ProductDto? = null,
    @SerializedName("status")
    val status: Int? = null,
    @SerializedName("status_verbose")
    val statusVerbose: String? = null
)
