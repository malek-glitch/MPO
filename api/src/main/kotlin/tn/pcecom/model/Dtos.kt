package tn.pcecom.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String)

@Serializable
data class Page<T>(val items: List<T>, val total: Long, val page: Int, val pageSize: Int)

// ---------- Categories ----------

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String,
    val slug: String,
    val parentId: Int?,
    val position: Int,
    val visible: Boolean,
    val children: List<CategoryDto> = emptyList(),
)

@Serializable
data class CategoryInput(
    val name: String,
    val slug: String? = null,
    val parentId: Int? = null,
    val position: Int = 0,
    val visible: Boolean = true,
)

// ---------- Products ----------

object Condition {
    val ALL = setOf("like_new", "very_good", "good", "fair")
}

object Status {
    const val AVAILABLE = "available"
    const val SOLD = "sold"
    const val HIDDEN = "hidden"
    val ALL = setOf(AVAILABLE, SOLD, HIDDEN)
}

@Serializable
data class UsedDetailsDto(
    val condition: String,
    val batteryHealth: Int? = null,
    val defects: String? = null,
    val accessories: String? = null,
)

@Serializable
data class PhotoDto(
    val id: Int,
    val position: Int,
    val thumb: String,
    val medium: String,
    val large: String,
)

@Serializable
data class ProductDto(
    val id: Int,
    val slug: String,
    val categoryId: Int,
    val isUsed: Boolean,
    val brand: String,
    val model: String,
    val processor: String?,
    val ramGb: Int?,
    val storageGb: Int?,
    val storageType: String?,
    val gpu: String?,
    val screenInches: Double?,
    val os: String?,
    val keyboard: String?,
    val priceTnd: Int,
    val compareAtPriceTnd: Int?,
    val quantity: Int,
    val status: String,
    val description: String?,
    val warrantyMonths: Int,
    val featured: Boolean,
    val used: UsedDetailsDto?,
    val photos: List<PhotoDto>,
    val createdAt: String,
)

@Serializable
data class ProductInput(
    val categoryId: Int,
    val isUsed: Boolean,
    val brand: String,
    val model: String,
    val processor: String? = null,
    val ramGb: Int? = null,
    val storageGb: Int? = null,
    val storageType: String? = null,
    val gpu: String? = null,
    val screenInches: Double? = null,
    val os: String? = null,
    val keyboard: String? = null,
    val priceTnd: Int,
    val compareAtPriceTnd: Int? = null,
    val quantity: Int = 1,
    val status: String = Status.AVAILABLE,
    val description: String? = null,
    val warrantyMonths: Int = 0,
    val featured: Boolean = false,
    val used: UsedDetailsDto? = null,
)

@Serializable
data class StatusInput(val status: String)

@Serializable
data class PhotoOrderInput(val photoIds: List<Int>)

// ---------- Store settings ----------

@Serializable
data class StoreSettingsDto(
    val name: String,
    val logoUrl: String?,
    val whatsappNumber: String,
    val phone: String?,
    val address: String?,
    val facebookUrl: String?,
    val instagramUrl: String?,
    val tiktokUrl: String?,
    val whatsappTemplate: String?,
    val deliveryInfo: String?,
)

@Serializable
data class StoreSettingsInput(
    val name: String,
    val whatsappNumber: String,
    val phone: String? = null,
    val address: String? = null,
    val facebookUrl: String? = null,
    val instagramUrl: String? = null,
    val tiktokUrl: String? = null,
    val whatsappTemplate: String? = null,
    val deliveryInfo: String? = null,
)

// ---------- Auth ----------

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String)

@Serializable
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

@Serializable
data class MeResponse(val id: Int, val email: String)

// ---------- Contact clicks & stats ----------

@Serializable
data class ContactClickInput(val productId: Int? = null)

@Serializable
data class ProductClicks(val productId: Int, val brand: String, val model: String, val clicks: Long)

@Serializable
data class DailyCountDto(val date: String, val count: Long)

@Serializable
data class CategoryCountDto(val categoryId: Int, val name: String, val count: Long)

@Serializable
data class StatsDto(
    val productsAvailable: Long,
    val soldThisMonth: Long,
    val clicksLast30Days: Long,
    val topClicked: List<ProductClicks>,
    val revenueThisMonthTnd: Long,
    val revenueAllTimeTnd: Long,
    val salesLast30Days: List<DailyCountDto>,
    val topCategories: List<CategoryCountDto>,
)
