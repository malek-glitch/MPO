package tn.pcecom.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.minutes
import tn.pcecom.config.JwtConfig
import tn.pcecom.model.ErrorResponse

const val ADMIN_AUTH = "admin"
val LOGIN_LIMIT = RateLimitName("login")
val CLICK_LIMIT = RateLimitName("clicks")

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
}

fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO
        filter { it.request.path().startsWith("/api") }
    }
}

fun Application.configureErrors() {
    install(StatusPages) {
        exception<BadRequestException> { call, e ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "bad request"))
        }
        exception<IllegalArgumentException> { call, e ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "invalid input"))
        }
        exception<NotFoundException> { call, e ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse(e.message ?: "not found"))
        }
        exception<ExposedSQLException> { call, e ->
            when (e.sqlState) {
                "23505" -> call.respond(HttpStatusCode.Conflict, ErrorResponse("already exists"))
                "23503" -> call.respond(HttpStatusCode.Conflict, ErrorResponse("still in use or invalid reference"))
                "23514" -> call.respond(HttpStatusCode.BadRequest, ErrorResponse("invalid value"))
                else -> {
                    call.application.environment.log.error("Database error", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("database error"))
                }
            }
        }
        exception<Throwable> { call, e ->
            call.application.environment.log.error("Unhandled error", e)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal error"))
        }
    }
}

fun Application.configureCors(origins: List<String>) {
    if (origins.isEmpty()) return
    install(CORS) {
        origins.forEach { allowHost(it, schemes = listOf("http", "https")) }
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }
}

fun Application.configureAuth(cfg: JwtConfig) {
    install(Authentication) {
        jwt(ADMIN_AUTH) {
            realm = "pc-ecom"
            verifier(
                JWT.require(Algorithm.HMAC256(cfg.secret))
                    .withIssuer(cfg.issuer)
                    .withAudience(cfg.audience)
                    .build()
            )
            validate { cred -> if (cred.payload.subject != null) JWTPrincipal(cred.payload) else null }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("unauthorized"))
            }
        }
    }
}

/** The API runs behind Caddy, so the client IP comes from X-Forwarded-For. */
fun Application.configureProxyAndRateLimit() {
    install(XForwardedHeaders)
    install(RateLimit) {
        register(LOGIN_LIMIT) {
            rateLimiter(limit = 10, refillPeriod = 5.minutes)
            requestKey { call -> call.request.origin.remoteHost }
        }
        register(CLICK_LIMIT) {
            rateLimiter(limit = 30, refillPeriod = 1.minutes)
            requestKey { call -> call.request.origin.remoteHost }
        }
    }
}
