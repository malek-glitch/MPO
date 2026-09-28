package tn.pcecom.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tn.pcecom.model.*
import tn.pcecom.plugins.ADMIN_AUTH
import tn.pcecom.plugins.LOGIN_LIMIT
import tn.pcecom.service.*

fun Route.adminRoutes(
    auth: AuthService,
    categories: CategoryService,
    products: ProductService,
    settings: SettingsService,
    stats: StatsService,
) {
    route("/admin") {

        rateLimit(LOGIN_LIMIT) {
            post("/login") {
                val req = call.receive<LoginRequest>()
                val token = auth.login(req.email, req.password)
                if (token == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("invalid email or password"))
                } else {
                    call.respond(LoginResponse(token))
                }
            }
        }

        authenticate(ADMIN_AUTH) {

            get("/me") {
                call.respond(auth.me(call.adminId()) ?: throw NotFoundException("admin not found"))
            }

            put("/password") {
                val req = call.receive<ChangePasswordRequest>()
                if (auth.changePassword(call.adminId(), req.currentPassword, req.newPassword)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("current password is wrong"))
                }
            }

            // ----- Products -----
            route("/products") {
                get {
                    call.respond(products.list(call.request.queryParameters.toProductFilter(), public = false))
                }
                post {
                    call.respond(HttpStatusCode.Created, products.create(call.receive<ProductInput>()))
                }
                route("/{id}") {
                    get {
                        call.respond(products.get(call.intParam("id")) ?: throw NotFoundException("product not found"))
                    }
                    put {
                        call.respond(products.update(call.intParam("id"), call.receive<ProductInput>()))
                    }
                    patch("/status") {
                        call.respond(products.setStatus(call.intParam("id"), call.receive<StatusInput>().status))
                    }
                    delete {
                        products.delete(call.intParam("id"))
                        call.respond(HttpStatusCode.NoContent)
                    }
                    post("/photos") {
                        val id = call.intParam("id")
                        call.respond(products.addPhotos(id, call.receiveImages(maxFiles = ProductService.MAX_PHOTOS)))
                    }
                    put("/photos/order") {
                        call.respond(products.reorderPhotos(call.intParam("id"), call.receive<PhotoOrderInput>().photoIds))
                    }
                    delete("/photos/{photoId}") {
                        call.respond(products.deletePhoto(call.intParam("id"), call.intParam("photoId")))
                    }
                }
            }

            // ----- Categories -----
            route("/categories") {
                get { call.respond(categories.tree(includeHidden = true)) }
                post {
                    call.respond(HttpStatusCode.Created, categories.create(call.receive<CategoryInput>()))
                }
                put("/{id}") {
                    call.respond(categories.update(call.intParam("id"), call.receive<CategoryInput>()))
                }
                delete("/{id}") {
                    categories.delete(call.intParam("id"))
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            // ----- Store settings -----
            route("/settings") {
                get { call.respond(settings.get()) }
                put { call.respond(settings.update(call.receive<StoreSettingsInput>())) }
                post("/logo") { call.respond(settings.setLogo(call.receiveImages(maxFiles = 1).first())) }
            }

            get("/stats") { call.respond(stats.stats()) }
        }
    }
}
