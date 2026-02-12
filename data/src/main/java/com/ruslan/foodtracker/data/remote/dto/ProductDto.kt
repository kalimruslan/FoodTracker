package com.ruslan.foodtracker.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для продукта из Open Food Facts API
 */
data class ProductDto(
    @SerializedName("code")
    val code: String? = null,

    @SerializedName("product_name")
    val productName: String? = null,

    @SerializedName("brands")
    val brands: String? = null,

    @SerializedName("nutriments")
    val nutriments: NutrimentsDto? = null,

    @SerializedName("serving_size")
    val servingSize: String? = null,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("image_front_url")
    val imageFrontUrl: String? = null,

    @SerializedName("quantity")
    val quantity: String? = null
)