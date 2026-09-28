package tn.pcecom.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

// Schema is created by Flyway (resources/db/migration). These objects only map it.

object AdminUsers : IntIdTable("admin_users") {
    val email = varchar("email", 255)
    val passwordHash = varchar("password_hash", 255)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

object Categories : IntIdTable("categories") {
    val name = varchar("name", 100)
    val slug = varchar("slug", 120)
    val parentId = integer("parent_id").nullable()
    val position = integer("position").default(0)
    val visible = bool("visible").default(true)
}

object Products : IntIdTable("products") {
    val slug = varchar("slug", 220)
    val categoryId = integer("category_id")
    val isUsed = bool("is_used").default(false)
    val brand = varchar("brand", 80)
    val model = varchar("model", 150)
    val processor = varchar("processor", 150).nullable()
    val ramGb = integer("ram_gb").nullable()
    val storageGb = integer("storage_gb").nullable()
    val storageType = varchar("storage_type", 20).nullable()
    val gpu = varchar("gpu", 150).nullable()
    val screenInches = decimal("screen_inches", 4, 1).nullable()
    val os = varchar("os", 80).nullable()
    val keyboard = varchar("keyboard", 30).nullable()
    val priceTnd = integer("price_tnd")
    val quantity = integer("quantity").default(1)
    val status = varchar("status", 20).default("available")
    val description = text("description").nullable()
    val warrantyMonths = integer("warranty_months").default(0)
    val compareAtPriceTnd = integer("compare_at_price_tnd").nullable()
    val featured = bool("featured").default(false)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    val soldAt = datetime("sold_at").nullable()
}

object UsedDetails : Table("used_details") {
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val condition = varchar("condition", 20)
    val batteryHealth = integer("battery_health").nullable()
    val defects = text("defects").nullable()
    val accessories = text("accessories").nullable()
    override val primaryKey = PrimaryKey(productId)
}

object ProductPhotos : IntIdTable("product_photos") {
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val objectKey = varchar("object_key", 255)
    val position = integer("position").default(0)
}

object StoreSettings : Table("store_settings") {
    val id = integer("id")
    val name = varchar("name", 120)
    val logoKey = varchar("logo_key", 255).nullable()
    val whatsappNumber = varchar("whatsapp_number", 30)
    val phone = varchar("phone", 30).nullable()
    val address = text("address").nullable()
    val facebookUrl = varchar("facebook_url", 255).nullable()
    val instagramUrl = varchar("instagram_url", 255).nullable()
    val tiktokUrl = varchar("tiktok_url", 255).nullable()
    val whatsappTemplate = text("whatsapp_template").nullable()
    val deliveryInfo = text("delivery_info").nullable()
    override val primaryKey = PrimaryKey(id)
}

object ContactClicks : LongIdTable("contact_clicks") {
    val productId = optReference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val clickedAt = datetime("clicked_at").defaultExpression(CurrentDateTime)
}
