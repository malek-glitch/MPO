package tn.pcecom.routes

import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tn.pcecom.model.ContactClickInput
import tn.pcecom.plugins.CLICK_LIMIT
import tn.pcecom.service.*

/** Read-only API used by the storefront. */
fun Route.publicRoutes(
    categories: CategoryService,
    products: ProductService,
    settings: SettingsService,
    stats: StatsService,
) {
    get("/health") { call.respond(mapOf("status" to "ok")) }

    get("/store") { call.respond(settings.get()) }

    get("/categories") { call.respond(categories.tree(includeHidden = false)) }

    get("/brands") { call.respond(products.brands()) }

    get("/products") {
        call.respond(products.list(call.request.queryParameters.toProductFilter(), public = true))
    }

    get("/products/{slug}") {
        val slug = call.parameters["slug"] ?: throw BadRequestException("missing slug")
        call.respond(products.getPublic(slug) ?: throw NotFoundException("product not found"))
    }

    rateLimit(CLICK_LIMIT) {
        post("/contact-clicks") {
            stats.recordClick(call.receive<ContactClickInput>().productId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
