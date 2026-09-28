package tn.pcecom.service

import org.jetbrains.exposed.sql.selectAll
import org.slf4j.LoggerFactory
import tn.pcecom.db.Products
import tn.pcecom.db.dbQuery
import tn.pcecom.model.ProductInput
import tn.pcecom.model.Status
import tn.pcecom.model.StoreSettingsInput
import tn.pcecom.model.UsedDetailsDto

/**
 * Seeds a small, realistic demo catalog (and branded store settings) the first time the app
 * starts against an empty `products` table, so a freshly cloned/deployed install shows a
 * populated storefront instead of an empty one. No-op once any product exists.
 */
class SeedService(
    private val categories: CategoryService,
    private val products: ProductService,
    private val settings: SettingsService,
) {
    private val log = LoggerFactory.getLogger(SeedService::class.java)

    suspend fun seedIfEmpty() {
        val alreadySeeded = dbQuery { !Products.selectAll().empty() }
        if (alreadySeeded) return

        log.info("No products found — seeding demo catalog")
        val bySlug = categories.tree(includeHidden = true).flatMap { listOf(it) + it.children }.associateBy { it.slug }

        for (seed in DEMO_PRODUCTS) {
            val categoryId = bySlug[seed.categorySlug]?.id
                ?: error("seed category '${seed.categorySlug}' not found")
            val created = products.create(
                ProductInput(
                    categoryId = categoryId,
                    isUsed = seed.isUsed,
                    brand = seed.brand,
                    model = seed.model,
                    processor = seed.processor,
                    ramGb = seed.ramGb,
                    storageGb = seed.storageGb,
                    storageType = seed.storageType,
                    gpu = seed.gpu,
                    screenInches = seed.screenInches,
                    os = seed.os,
                    keyboard = seed.keyboard,
                    priceTnd = seed.priceTnd,
                    compareAtPriceTnd = seed.compareAtPriceTnd,
                    quantity = seed.quantity,
                    status = seed.status,
                    description = seed.description,
                    warrantyMonths = seed.warrantyMonths,
                    featured = seed.featured,
                    used = seed.used,
                ),
            )
            val photoBytes = seed.photos.map { resource ->
                javaClass.getResourceAsStream("/seed-photos/$resource")?.readBytes()
                    ?: error("seed photo resource not found: $resource")
            }
            if (photoBytes.isNotEmpty()) products.addPhotos(created.id, photoBytes)
        }

        settings.update(
            StoreSettingsInput(
                name = "Meilleurs Prix Ordinateur",
                whatsappNumber = "21650590137",
                phone = "+216 50 590 137",
                address = "34 Rue d'Inde, Tunis - LAFAYETTE",
                facebookUrl = "https://www.facebook.com/Meilleursprixordinateur",
                instagramUrl = "https://instagram.com",
                tiktokUrl = "https://tiktok.com",
                whatsappTemplate = "Bonjour Meilleurs Prix Ordinateur, je suis intéressé(e) par le {product} à {price} TND. Est-il toujours disponible ?",
                deliveryInfo = "Livraison gratuite sur toute la Tunisie",
            ),
        )
        log.info("Seeded {} demo products", DEMO_PRODUCTS.size)
    }
}

private data class SeedProduct(
    val categorySlug: String,
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
    val warrantyMonths: Int,
    val featured: Boolean = false,
    val used: UsedDetailsDto? = null,
    val photos: List<String>,
)

private val DEMO_PRODUCTS = listOf(
    SeedProduct(
        categorySlug = "pc-portables-gaming",
        isUsed = true,
        brand = "MSI",
        model = "Katana GF76",
        processor = "Intel Core i7 (11ème gen)",
        ramGb = 24,
        storageGb = 512,
        storageType = "SSD NVMe",
        gpu = "RTX 3050 4 Go",
        screenInches = 17.3,
        os = "Windows 11",
        keyboard = "AZERTY",
        priceTnd = 2290,
        warrantyMonths = 3,
        description = "PC portable gamer MSI Katana GF76 d'occasion avec garantie. Disponible à Tunis Lafayette, livraison gratuite sur toute la Tunisie.",
        used = UsedDetailsDto(condition = "very_good", batteryHealth = null, defects = null, accessories = "Chargeur d'origine"),
        photos = listOf("p13-0.webp"),
    ),
    SeedProduct(
        categorySlug = "macbook-air",
        isUsed = false,
        brand = "Apple",
        model = "MacBook Air 15\" (2026)",
        processor = "Apple M5",
        ramGb = 16,
        storageGb = 512,
        storageType = "SSD",
        os = "macOS",
        keyboard = "AZERTY",
        priceTnd = 4990,
        warrantyMonths = 12,
        description = "MacBook Air 15\" M5 neuf, cacheté, garantie officielle Apple. Disponible à Tunis Lafayette, livraison gratuite sur toute la Tunisie.",
        photos = listOf("p14-0.webp"),
    ),
    SeedProduct(
        categorySlug = "pc-portables-standard",
        isUsed = false,
        brand = "Dell",
        model = "Vostro 15 3530",
        processor = "Intel Core i5-1334U (12 Mo de cache, jusqu'à 4,60 GHz, 10 cœurs)",
        ramGb = 16,
        storageGb = 512,
        storageType = "SSD NVMe",
        gpu = "Intel UHD",
        screenInches = 15.6,
        os = "Windows 11",
        keyboard = "AZERTY",
        priceTnd = 1290,
        quantity = 2,
        warrantyMonths = 12,
        description = "Dell Vostro 15 3530, écran 15,6\" IPS FHD. Article disponible avec garantie, livraison gratuite sur toute la Tunisie.",
        photos = listOf("p15-0.webp", "p15-1.webp"),
    ),
    SeedProduct(
        categorySlug = "pc-portables-gaming",
        isUsed = false,
        brand = "HP",
        model = "Victus",
        processor = "AMD Ryzen 7 7445HS",
        ramGb = 16,
        storageGb = 512,
        storageType = "SSD NVMe",
        gpu = "RTX 3050 6 Go",
        screenInches = 15.6,
        os = "Windows 11",
        keyboard = "AZERTY",
        priceTnd = 2690,
        compareAtPriceTnd = 2990,
        warrantyMonths = 12,
        featured = true,
        description = "HP Victus Gaming Power, écran FHD 144Hz. Article disponible avec garantie, livraison gratuite sur toute la Tunisie.",
        photos = listOf("p16-0.webp"),
    ),
    SeedProduct(
        categorySlug = "macbook-pro",
        isUsed = true,
        brand = "Apple",
        model = "MacBook Pro 14\" M1 Max",
        processor = "Apple M1 Max",
        ramGb = 64,
        storageGb = 512,
        storageType = "SSD",
        os = "macOS",
        keyboard = "AZERTY",
        priceTnd = 3990,
        warrantyMonths = 3,
        description = "MacBook Pro 14\" M1 Max d'occasion, cycle batterie 80, capacité 100%. Article disponible avec garantie, livraison gratuite sur toute la Tunisie.",
        used = UsedDetailsDto(condition = "like_new", batteryHealth = 100, defects = null, accessories = "Chargeur d'origine"),
        photos = listOf("p17-0.webp"),
    ),
    SeedProduct(
        categorySlug = "macbook-pro",
        isUsed = true,
        brand = "Apple",
        model = "MacBook Pro 14\" M3 Pro",
        processor = "Apple M3 Pro",
        ramGb = 18,
        storageGb = 512,
        storageType = "SSD",
        os = "macOS",
        keyboard = "AZERTY",
        priceTnd = 4290,
        warrantyMonths = 3,
        description = "MacBook Pro 14\" M3 Pro d'occasion, cycle batterie 155, capacité 95%. Article disponible avec garantie, livraison gratuite sur toute la Tunisie.",
        used = UsedDetailsDto(condition = "like_new", batteryHealth = 95, defects = null, accessories = "Chargeur d'origine"),
        photos = listOf("p18-0.webp"),
    ),
    SeedProduct(
        categorySlug = "pc-portables-gaming",
        isUsed = false,
        brand = "MSI",
        model = "Raider 18 HX AI",
        processor = "Intel Core Ultra 9 285HX",
        ramGb = 64,
        storageGb = 2048,
        storageType = "2x SSD NVMe",
        gpu = "RTX 5090 24 Go",
        screenInches = 18.0,
        os = "Windows 11",
        keyboard = "AZERTY",
        priceTnd = 10790,
        warrantyMonths = 12,
        status = Status.SOLD,
        description = "MSI Raider 18 HX AI — le futur du gaming. Écran 18\" QHD+ (2560x1600) @ 240Hz. Combine intelligence artificielle et puissance brute pour des performances hors normes. Article disponible avec garantie, livraison gratuite sur toute la Tunisie.",
        photos = listOf("p19-0.webp"),
    ),
)
