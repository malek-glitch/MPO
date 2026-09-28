package tn.pcecom.service

import io.ktor.server.plugins.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inSubQuery
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import tn.pcecom.db.*
import tn.pcecom.model.*
import java.math.RoundingMode
import java.time.LocalDateTime

data class ProductFilter(
    val categorySlug: String? = null,
    val used: Boolean? = null,
    val conditions: List<String> = emptyList(),
    val brands: List<String> = emptyList(),
    val ramGb: List<Int> = emptyList(),
    val storageMinGb: Int? = null,
    val priceMin: Int? = null,
    val priceMax: Int? = null,
    val screenMin: Double? = null,
    val screenMax: Double? = null,
    val query: String? = null,
    val featured: Boolean? = null,
    val status: String? = null,        // admin only
    val includeSold: Boolean = false,  // public only
    val sort: String = "newest",       // newest | price_asc | price_desc
    val page: Int = 1,
    val pageSize: Int = 24,
)

class ProductService(private val storage: PhotoStorage) {

    companion object {
        const val MAX_PHOTOS = 12
    }

    // ---------- Read ----------

    suspend fun list(f: ProductFilter, public: Boolean): Page<ProductDto> = dbQuery {
        val where = buildWhere(f, public) ?: return@dbQuery Page(emptyList(), 0, f.page, f.pageSize)
        val total = Products.selectAll().where(where).count()
        val order: Pair<Expression<*>, SortOrder> = when (f.sort) {
            "price_asc" -> Products.priceTnd to SortOrder.ASC
            "price_desc" -> Products.priceTnd to SortOrder.DESC
            else -> Products.createdAt to SortOrder.DESC
        }
        val rows = Products.selectAll().where(where)
            .orderBy(order, Products.id to SortOrder.DESC)
            .limit(f.pageSize)
            .offset(((f.page - 1) * f.pageSize).toLong())
            .toList()
        Page(toDtos(rows), total, f.page, f.pageSize)
    }

    suspend fun get(productId: Int): ProductDto? = dbQuery {
        Products.selectAll().where { Products.id eq productId }.singleOrNull()?.let { toDtos(listOf(it)).first() }
    }

    /** Public product page: available or sold, never hidden. */
    suspend fun getPublic(slug: String): ProductDto? = dbQuery {
        Products.selectAll()
            .where { (Products.slug eq slug) and (Products.status neq Status.HIDDEN) }
            .singleOrNull()
            ?.let { toDtos(listOf(it)).first() }
    }

    /** Brands of available products, for the storefront filters. */
    suspend fun brands(): List<String> = dbQuery {
        Products.select(Products.brand)
            .where { Products.status eq Status.AVAILABLE }
            .withDistinct()
            .orderBy(Products.brand to SortOrder.ASC)
            .map { it[Products.brand] }
    }

    // ---------- Write ----------

    suspend fun create(input: ProductInput): ProductDto {
        val i = normalize(input)
        val newId = dbQuery {
            checkCategory(i.categoryId)
            val id = Products.insertAndGetId {
                fill(it, i)
                it[Products.slug] = slugify("${i.brand} ${i.model}").take(200) + "-" + randomSuffix()
                if (i.status == Status.SOLD) it[Products.soldAt] = LocalDateTime.now()
            }
            saveUsed(id, i.used)
            id.value
        }
        return get(newId)!!
    }

    suspend fun update(productId: Int, input: ProductInput): ProductDto {
        val i = normalize(input)
        dbQuery {
            val current = Products.selectAll().where { Products.id eq productId }.singleOrNull()
                ?: throw NotFoundException("product not found")
            checkCategory(i.categoryId)
            val wasSold = current[Products.status] == Status.SOLD
            Products.update({ Products.id eq productId }) {
                fill(it, i)
                if (i.status == Status.SOLD && !wasSold) it[Products.soldAt] = LocalDateTime.now()
                if (i.status != Status.SOLD) it[Products.soldAt] = null
            }
            saveUsed(current[Products.id], i.used)
        }
        return get(productId)!!
    }

    suspend fun setStatus(productId: Int, newStatus: String): ProductDto {
        require(newStatus in Status.ALL) { "invalid status" }
        dbQuery {
            val current = Products.selectAll().where { Products.id eq productId }.singleOrNull()
                ?: throw NotFoundException("product not found")
            val wasSold = current[Products.status] == Status.SOLD
            Products.update({ Products.id eq productId }) {
                it[Products.status] = newStatus
                it[Products.updatedAt] = LocalDateTime.now()
                if (newStatus == Status.SOLD && !wasSold) it[Products.soldAt] = LocalDateTime.now()
                if (newStatus != Status.SOLD) it[Products.soldAt] = null
            }
        }
        return get(productId)!!
    }

    suspend fun delete(productId: Int) {
        val keys = dbQuery {
            val keys = ProductPhotos.select(ProductPhotos.objectKey)
                .where { ProductPhotos.productId eq productId }
                .map { it[ProductPhotos.objectKey] }
            val deleted = Products.deleteWhere { Products.id eq productId }
            if (deleted == 0) throw NotFoundException("product not found")
            keys
        }
        withContext(Dispatchers.IO) { keys.forEach(storage::delete) }
    }

    // ---------- Photos ----------

    suspend fun addPhotos(productId: Int, files: List<ByteArray>): ProductDto {
        val existing = dbQuery {
            if (Products.selectAll().where { Products.id eq productId }.empty()) {
                throw NotFoundException("product not found")
            }
            ProductPhotos.selectAll().where { ProductPhotos.productId eq productId }.count()
        }
        require(existing + files.size <= MAX_PHOTOS) { "maximum $MAX_PHOTOS photos per product" }

        val keys = withContext(Dispatchers.IO) { files.map { storage.upload("products/$productId", it) } }

        dbQuery {
            val maxPos = ProductPhotos.position.max()
            var pos = ProductPhotos.select(maxPos)
                .where { ProductPhotos.productId eq productId }
                .singleOrNull()?.get(maxPos) ?: -1
            keys.forEach { key ->
                pos++
                ProductPhotos.insert {
                    it[ProductPhotos.productId] = EntityID(productId, Products)
                    it[ProductPhotos.objectKey] = key
                    it[ProductPhotos.position] = pos
                }
            }
        }
        return get(productId)!!
    }

    /** photoIds in the new display order; the first one is the main photo. */
    suspend fun reorderPhotos(productId: Int, photoIds: List<Int>): ProductDto {
        dbQuery {
            photoIds.forEachIndexed { index, photoId ->
                ProductPhotos.update({ (ProductPhotos.id eq photoId) and (ProductPhotos.productId eq productId) }) {
                    it[ProductPhotos.position] = index
                }
            }
        }
        return get(productId) ?: throw NotFoundException("product not found")
    }

    suspend fun deletePhoto(productId: Int, photoId: Int): ProductDto {
        val key = dbQuery {
            val row = ProductPhotos.selectAll()
                .where { (ProductPhotos.id eq photoId) and (ProductPhotos.productId eq productId) }
                .singleOrNull() ?: throw NotFoundException("photo not found")
            ProductPhotos.deleteWhere { ProductPhotos.id eq photoId }
            row[ProductPhotos.objectKey]
        }
        withContext(Dispatchers.IO) { storage.delete(key) }
        return get(productId)!!
    }

    // ---------- Helpers ----------

    private fun normalize(i: ProductInput): ProductInput {
        require(i.brand.isNotBlank()) { "brand is required" }
        require(i.model.isNotBlank()) { "model is required" }
        require(i.priceTnd >= 0) { "price must be positive" }
        i.compareAtPriceTnd?.let { require(it > i.priceTnd) { "compare-at price must be greater than the price" } }
        require(i.status in Status.ALL) { "invalid status" }
        require(i.warrantyMonths >= 0) { "invalid warranty" }
        i.ramGb?.let { require(it > 0) { "invalid RAM" } }
        i.storageGb?.let { require(it > 0) { "invalid storage" } }
        if (i.isUsed) {
            val u = i.used ?: throw IllegalArgumentException("condition details are required for a used product")
            require(u.condition in Condition.ALL) { "invalid condition" }
            u.batteryHealth?.let { require(it in 0..100) { "battery health must be between 0 and 100" } }
        }
        return i.copy(
            brand = i.brand.trim(),
            model = i.model.trim(),
            quantity = if (i.isUsed) 1 else i.quantity.coerceAtLeast(0),
            used = if (i.isUsed) i.used?.let { u ->
                u.copy(defects = u.defects.cleanOrNull(), accessories = u.accessories.cleanOrNull())
            } else null,
        )
    }

    private fun checkCategory(categoryId: Int) {
        require(!Categories.selectAll().where { Categories.id eq categoryId }.empty()) { "category not found" }
    }

    private fun fill(s: UpdateBuilder<*>, i: ProductInput) {
        s[Products.categoryId] = i.categoryId
        s[Products.isUsed] = i.isUsed
        s[Products.brand] = i.brand
        s[Products.model] = i.model
        s[Products.processor] = i.processor.cleanOrNull()
        s[Products.ramGb] = i.ramGb
        s[Products.storageGb] = i.storageGb
        s[Products.storageType] = i.storageType.cleanOrNull()
        s[Products.gpu] = i.gpu.cleanOrNull()
        s[Products.screenInches] = i.screenInches?.toBigDecimal()?.setScale(1, RoundingMode.HALF_UP)
        s[Products.os] = i.os.cleanOrNull()
        s[Products.keyboard] = i.keyboard.cleanOrNull()
        s[Products.priceTnd] = i.priceTnd
        s[Products.compareAtPriceTnd] = i.compareAtPriceTnd
        s[Products.quantity] = i.quantity
        s[Products.status] = i.status
        s[Products.description] = i.description.cleanOrNull()
        s[Products.warrantyMonths] = i.warrantyMonths
        s[Products.featured] = i.featured
        s[Products.updatedAt] = LocalDateTime.now()
    }

    private fun saveUsed(productId: EntityID<Int>, used: UsedDetailsDto?) {
        UsedDetails.deleteWhere { UsedDetails.productId eq productId }
        if (used != null) {
            UsedDetails.insert {
                it[UsedDetails.productId] = productId
                it[UsedDetails.condition] = used.condition
                it[UsedDetails.batteryHealth] = used.batteryHealth
                it[UsedDetails.defects] = used.defects
                it[UsedDetails.accessories] = used.accessories
            }
        }
    }

    /** Returns null when the filter can't match anything (e.g. unknown category). */
    private fun buildWhere(f: ProductFilter, public: Boolean): Op<Boolean>? {
        val ops = mutableListOf<Op<Boolean>>()

        if (public) {
            ops += if (f.includeSold) Products.status inList listOf(Status.AVAILABLE, Status.SOLD)
            else Products.status eq Status.AVAILABLE
        } else {
            f.status?.let { ops += Products.status eq it }
        }

        f.categorySlug?.let { slug ->
            val ids = categoryIds(slug, public)
            if (ids.isEmpty()) return null
            ops += Products.categoryId inList ids
        }
        f.used?.let { ops += Products.isUsed eq it }
        if (f.conditions.isNotEmpty()) {
            ops += Products.id inSubQuery UsedDetails.select(UsedDetails.productId)
                .where { UsedDetails.condition inList f.conditions }
        }
        if (f.brands.isNotEmpty()) ops += Products.brand.lowerCase() inList f.brands.map { it.lowercase() }
        f.featured?.let { ops += Products.featured eq it }
        if (f.ramGb.isNotEmpty()) ops += Products.ramGb inList f.ramGb
        f.storageMinGb?.let { ops += Products.storageGb greaterEq it }
        f.priceMin?.let { ops += Products.priceTnd greaterEq it }
        f.priceMax?.let { ops += Products.priceTnd lessEq it }
        f.screenMin?.let { ops += Products.screenInches greaterEq it.toBigDecimal() }
        f.screenMax?.let { ops += Products.screenInches lessEq it.toBigDecimal() }

        f.query?.trim()?.takeIf { it.isNotEmpty() }?.let { q ->
            // Every word must match brand, model or processor.
            q.lowercase().split(Regex("\\s+")).forEach { word ->
                val pattern = "%${escapeLike(word)}%"
                ops += (Products.brand.lowerCase() like pattern) or
                    (Products.model.lowerCase() like pattern) or
                    (Products.processor.lowerCase() like pattern)
            }
        }

        return ops.reduceOrNull { a, b -> a and b } ?: Op.TRUE
    }

    /** A category plus its subcategories. */
    private fun categoryIds(slug: String, public: Boolean): List<Int> {
        val cat = Categories.selectAll().where { Categories.slug eq slug }.singleOrNull() ?: return emptyList()
        if (public && !cat[Categories.visible]) return emptyList()
        val id = cat[Categories.id].value
        val children = Categories.selectAll()
            .where { Categories.parentId eq id }
            .filter { !public || it[Categories.visible] }
            .map { it[Categories.id].value }
        return listOf(id) + children
    }

    private fun toDtos(rows: List<ResultRow>): List<ProductDto> {
        if (rows.isEmpty()) return emptyList()
        val ids = rows.map { it[Products.id] }

        val used = UsedDetails.selectAll()
            .where { UsedDetails.productId inList ids }
            .associate {
                it[UsedDetails.productId].value to UsedDetailsDto(
                    condition = it[UsedDetails.condition],
                    batteryHealth = it[UsedDetails.batteryHealth],
                    defects = it[UsedDetails.defects],
                    accessories = it[UsedDetails.accessories],
                )
            }

        val photos = ProductPhotos.selectAll()
            .where { ProductPhotos.productId inList ids }
            .orderBy(ProductPhotos.position to SortOrder.ASC, ProductPhotos.id to SortOrder.ASC)
            .groupBy({ it[ProductPhotos.productId].value }) {
                val key = it[ProductPhotos.objectKey]
                PhotoDto(
                    id = it[ProductPhotos.id].value,
                    position = it[ProductPhotos.position],
                    thumb = storage.url(key, "thumb"),
                    medium = storage.url(key, "md"),
                    large = storage.url(key, "lg"),
                )
            }

        return rows.map { r ->
            val id = r[Products.id].value
            ProductDto(
                id = id,
                slug = r[Products.slug],
                categoryId = r[Products.categoryId],
                isUsed = r[Products.isUsed],
                brand = r[Products.brand],
                model = r[Products.model],
                processor = r[Products.processor],
                ramGb = r[Products.ramGb],
                storageGb = r[Products.storageGb],
                storageType = r[Products.storageType],
                gpu = r[Products.gpu],
                screenInches = r[Products.screenInches]?.toDouble(),
                os = r[Products.os],
                keyboard = r[Products.keyboard],
                priceTnd = r[Products.priceTnd],
                compareAtPriceTnd = r[Products.compareAtPriceTnd],
                quantity = r[Products.quantity],
                status = r[Products.status],
                description = r[Products.description],
                warrantyMonths = r[Products.warrantyMonths],
                featured = r[Products.featured],
                used = used[id],
                photos = photos[id] ?: emptyList(),
                createdAt = r[Products.createdAt].toString(),
            )
        }
    }
}
