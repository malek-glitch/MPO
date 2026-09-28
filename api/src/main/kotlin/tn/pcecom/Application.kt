package tn.pcecom

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.routing.*
import tn.pcecom.config.AppConfig
import tn.pcecom.db.DatabaseFactory
import tn.pcecom.plugins.*
import tn.pcecom.routes.adminRoutes
import tn.pcecom.routes.publicRoutes
import tn.pcecom.service.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val config = AppConfig.from(environment.config)

    DatabaseFactory.init(config.db)

    val storage = PhotoStorage(config.minio)
    val auth = AuthService(config.jwt)
    val categories = CategoryService()
    val products = ProductService(storage)
    val settings = SettingsService(storage)
    val stats = StatsService()

    auth.bootstrapAdmin(config.adminEmail, config.adminPassword)

    configureSerialization()
    configureLogging()
    configureProxyAndRateLimit()
    configureErrors()
    configureCors(config.corsOrigins)
    configureAuth(config.jwt)

    routing {
        route("/api") {
            publicRoutes(categories, products, settings, stats)
            adminRoutes(auth, categories, products, settings, stats)
        }
    }
}
