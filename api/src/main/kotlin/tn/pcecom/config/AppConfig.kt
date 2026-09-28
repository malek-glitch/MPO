package tn.pcecom.config

import io.ktor.server.config.*

data class DbConfig(val url: String, val user: String, val password: String)

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val expiresHours: Long,
)

data class MinioConfig(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
    val publicUrl: String,
)

data class AppConfig(
    val db: DbConfig,
    val jwt: JwtConfig,
    val minio: MinioConfig,
    val adminEmail: String?,
    val adminPassword: String?,
    val corsOrigins: List<String>,
) {
    companion object {
        fun from(c: ApplicationConfig): AppConfig {
            fun str(path: String) = c.property(path).getString()
            fun opt(path: String) = c.propertyOrNull(path)?.getString()?.takeIf { it.isNotBlank() }

            return AppConfig(
                db = DbConfig(str("db.url"), str("db.user"), str("db.password")),
                jwt = JwtConfig(
                    secret = str("jwt.secret"),
                    issuer = str("jwt.issuer"),
                    audience = str("jwt.audience"),
                    expiresHours = str("jwt.expiresHours").toLong(),
                ),
                minio = MinioConfig(
                    endpoint = str("minio.endpoint"),
                    accessKey = str("minio.accessKey"),
                    secretKey = str("minio.secretKey"),
                    bucket = str("minio.bucket"),
                    publicUrl = str("minio.publicUrl"),
                ),
                adminEmail = opt("admin.email"),
                adminPassword = opt("admin.password"),
                corsOrigins = opt("cors.origins")
                    ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                    ?: emptyList(),
            )
        }
    }
}
