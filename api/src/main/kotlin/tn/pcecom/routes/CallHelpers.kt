package tn.pcecom.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import tn.pcecom.service.ProductFilter

fun ApplicationCall.intParam(name: String): Int =
    parameters[name]?.toIntOrNull() ?: throw BadRequestException("invalid $name")

fun ApplicationCall.adminId(): Int =
    principal<JWTPrincipal>()?.subject?.toIntOrNull() ?: throw BadRequestException("invalid token")

/** Accepts repeated params (?brand=HP&brand=Dell) and comma lists (?brand=HP,Dell). */
private fun Parameters.list(name: String): List<String> =
    getAll(name)?.flatMap { it.split(",") }?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

fun Parameters.toProductFilter() = ProductFilter(
    categorySlug = get("category"),
    used = get("used")?.toBooleanStrictOrNull(),
    conditions = list("condition"),
    brands = list("brand"),
    ramGb = list("ram").mapNotNull { it.toIntOrNull() },
    storageMinGb = get("storageMin")?.toIntOrNull(),
    priceMin = get("priceMin")?.toIntOrNull(),
    priceMax = get("priceMax")?.toIntOrNull(),
    screenMin = get("screenMin")?.toDoubleOrNull(),
    screenMax = get("screenMax")?.toDoubleOrNull(),
    query = get("q"),
    featured = get("featured")?.toBooleanStrictOrNull(),
    status = get("status"),
    includeSold = get("includeSold") == "true",
    sort = get("sort") ?: "newest",
    page = (get("page")?.toIntOrNull() ?: 1).coerceAtLeast(1),
    pageSize = (get("pageSize")?.toIntOrNull() ?: 24).coerceIn(1, 100),
)

/** Reads image files from a multipart request. */
suspend fun ApplicationCall.receiveImages(
    maxFiles: Int = 12,
    maxBytesPerFile: Long = 15L * 1024 * 1024,
): List<ByteArray> {
    val files = mutableListOf<ByteArray>()
    receiveMultipart(formFieldLimit = maxBytesPerFile).forEachPart { part ->
        try {
            if (part is PartData.FileItem) {
                val type = part.contentType
                if (type == null || !type.match(ContentType.Image.Any)) {
                    throw BadRequestException("only image files are allowed")
                }
                if (files.size >= maxFiles) throw BadRequestException("too many files (max $maxFiles)")
                files += part.provider().readRemaining().readByteArray()
            }
        } finally {
            part.dispose()
        }
    }
    if (files.isEmpty()) throw BadRequestException("no image received")
    return files
}
